# Ronde 3 POS + Laporan Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** POS seperti kasir beneran (3 metode bayar, diskon member bersyarat, potongan nominal, struk cetak, riwayat nota), menu berprofil, dashboard, 10 laporan cantik + chart + export Excel.

**Architecture:** Pola ronde 1-2 dipertahankan (view→dao→Koneksi; JRBeanCollectionDataSource; loader reflektif MenuUtama — form baru NOL edit MenuUtama kecuali Task 14 yang eksplisit). Schema bertambah 3 kolom via ALTER idempoten; seed/seed-docs mengikuti.

**Tech Stack:** Java 11, FlatLaf 3.4, Ikonli 12.3.1, mysql-connector-j 8.4.0, JasperReports 6.21.3, LGoodDatePicker 11.2.1, BARU: poi-ooxml 5.2.5 + jfreechart eksplisit (versi ikut dependency-tree JasperReports bila beda, catat).

**Spec:** docs/superpowers/specs/2026-09-23-ronde3-pos-laporan-design.md (argues from docs/prd.md + docs/design.md — baca ketiganya).

## Global Constraints

- Java 11, UTF-8. Dependency baru HANYA poi-ooxml:5.2.5 + jfreechart eksplisit (versi final catat di laporan). DILARANG dep lain.
- Schema final = 11 tabel + 3 kolom baru: `penjualan.metode_bayar VARCHAR(20) NOT NULL DEFAULT 'Tunai'`, `penjualan.diskon DECIMAL(12,2) NOT NULL DEFAULT 0`, `kategori.deskripsi VARCHAR(255) NULL`. `database/schema.sql` + `database/migrasi_ronde3.sql` + `database/seed_demo.sql` konsisten.
- `metode_bayar` NILAI TEPAT: `Tunai|Transfer|QRIS`. `diskon` = gabungan (member + manual), rincian hanya di struk/laporan.
- Syarat diskon member: total-qty >= 50 ATAU subtotal >= 750000 → 5% (konstanta `util/AppConfig.java`: `SYARAT_DISKON_QTY=50`, `SYARAT_DISKON_NOMINAL=750000`, `DISKON_MEMBER_PCT=5`). Potongan manual: `0 <= potongan <= (subtotal - diskon_member)`, bebas semua role.
- Register tetap Kasir-only. FormUser Admin-only. Kasir: +Dashboard (global) +Riwayat (miliknya). Menu terlarang `setVisible(false)`.
- Neobrutalism DESIGN.md tetap (arc 0, shadow strip, palet, Ikonli — konstanta baru wajib javap).
- Testing = probe luar repo + hapus + DB assert COUNT akhir (prefix TEST-R3, cleanup FK-aman). DILARANG sisa data uji.
- Context7 dulu bila API POI/JFreeChart/Jasper-chart diragukan; catat query di laporan.

---

## File Map ronde 3

```
Modify: pom.xml (2 dep baru)
Modify: database/schema.sql (+3 kolom), database/seed_demo.sql (ikut)
Create: database/migrasi_ronde3.sql
Modify: model/Penjualan.java (+metodeBayar, +diskon), model/Kategori.java (+deskripsi),
        model/LapPenjualan.java (+metodeBayar, +diskon)
Create: model/{LapGrafik.java, LapMember.java, LapRetur.java, LapLabaKategori.java, LapOmzetKasir.java}
Modify: dao/PenjualanDAO.java (+lapOmzetKasir, +omzetPerHari, +labaPerHari, +itemTerjual)
        dao/PenjualanDAOImpl.java (saveWithDetail +2 kolom, map +2 kolom, 4 query baru)
        dao/KategoriDAO.java (+getAllWithCount? TIDAK — count via BukuDAO.countByKategori)
        dao/BukuDAO.java (+countByKategori():List<LapGrafik>, +lapLabaKategori(a,b))
        dao/MemberDAO.java (+lapMember(a,b))
        dao/ReturDAO.java (+lapRetur(a,b))
Modify: view/FormPenjualan.java (rewrite panel ringkasan + validasi), view/StrukDialog.java (ctor baru + Cetak),
        view/FormKategori.java (+deskripsi +kolom jumlah), view/FormMember.java (+kolom transaksi),
        view/FormPembelian.java (+hint), view/FormLaporan.java (10 jenis + excel + grafik-params)
Create: view/{FormRiwayat.java, FormDashboard.java, ProfilSaya.java}
Modify: view/MenuUtama.java (seksi + highlight + kartu profil + jam + tombol Dashboard/Riwayat)
Create: util/ExcelExporter.java
Modify: src/main/resources/reports/*.jrxml (restyle 6) + 4 jrxml baru
Modify: README.md, TESTING.md (Task 16)
```

---

### Task 12: ALTER + POS beneran + struk cetak + riwayat

**Files:**
- Create: `database/migrasi_ronde3.sql`, `view/FormRiwayat.java`
- Modify: `database/schema.sql`, `database/seed_demo.sql`, `model/Penjualan.java`, `model/LapPenjualan.java`, `util/AppConfig.java`, `dao/PenjualanDAO.java`, `dao/PenjualanDAOImpl.java`, `view/FormPenjualan.java`, `view/StrukDialog.java`

**Interfaces:**
- Consumes: `saveWithDetail` lama, `NotaGenerator`, `BukuDAO.getById`, `MemberDAO.getAll`, `Sesi`
- Produces: `PenjualanDAO.saveWithDetail` (tetap signature, kini tulis `metode_bayar,diskon`); `LapPenjualan(metodeBayar,diskon)`; `FormRiwayat` (JPanel no-arg, FQN `view.FormRiwayat`); `StrukDialog(Frame,Penjualan,List<DetailPenjualan>,kasir,member,diskonMember,potongan)` (CTOR BARU — ctor lama HAPUS, hanya 1 pemanggil lama + 1 baru); `AppConfig.SYARAT_DISKON_QTY/_NOMINAL/_PCT`

- [ ] **Step 1: SQL.** `migrasi_ronde3.sql` (idempoten MariaDB, header run-book):

```sql
ALTER TABLE penjualan ADD COLUMN IF NOT EXISTS metode_bayar VARCHAR(20) NOT NULL DEFAULT 'Tunai';
ALTER TABLE penjualan ADD COLUMN IF NOT EXISTS diskon DECIMAL(12,2) NOT NULL DEFAULT 0;
ALTER TABLE kategori ADD COLUMN IF NOT EXISTS deskripsi VARCHAR(255) NULL;
```

Jalankan ke db dev. Update `schema.sql`: tambah 2 kolom ke CREATE TABLE penjualan + 1 ke kategori (posisi: metode_bayar+diskon setelah kembalian; deskripsi setelah nama_kategori). Update `seed_demo.sql`: INSERT penjualan eksplisit kolom + metode/diskon (nota-0001 Tunai diskon 0? hitung: seed lama tanpa diskon → diskon=0, metode Tunai), 0002 Tunai 0.

- [ ] **Step 2: Model + AppConfig.** `Penjualan` + `String metodeBayar; double diskon;` + getter/setter + ctor penuh update. `LapPenjualan` + keduanya. `AppConfig`: `SYARAT_DISKON_QTY=50`, `SYARAT_DISKON_NOMINAL=750000`, `DISKON_MEMBER_PCT=5`.

- [ ] **Step 3: DAO.** `saveWithDetail` INSERT header + `metode_bayar,diskon`; semua SELECT penjualan (`BASE_SELECT`) + kedua kolom; `map()` set keduanya. `lapPenjualan`/`lapPenjualanByUser` SELECT + map keduanya.

- [ ] **Step 4: FormPenjualan rewrite ringkasan.** Combo metode (Tunai/Transfer/QRIS) → listener: non-tunai → `txtBayar.setText(fmt(total)); txtBayar.setEnabled(false)`; Tunai → enabled + kosong. Label status diskon: `syaratOk = member!=null && (qtyTotal>=50 || subtotal>=750000)` → `diskonMember = syaratOk ? 5%*subtotal : 0`; teks "Diskon member aktif" vs "Belanja RpX / Y buku lagi". Field potongan (default 0, angka, `<= subtotal-diskonMember` else error "Potongan melebihi sisa"). Total = subtotal-diskonMember-potongan. Simpan: validasi metode (Tunai: bayar angka & >=total else "Uang bayar kurang"; non-tunai: bayar=total); cek stok ulang; `NotaGenerator NJ`; `Penjualan(metode, diskon=diskonMember+potongan)` → save → `new StrukDialog(owner,h,items,kasir,member,diskonMember,potongan).setVisible(true)` → reset (metode Tunai, potongan 0).

- [ ] **Step 5: StrukDialog baru.** Isi §3 spec (kasir WAJIB tampil). Tombol Cetak (`primary`?) → `PrinterJob`: `job.setPrintable((g,pf,idx)->{ if(idx>0) return NO_SUCH_PAGE; panel.print(g); return PAGE_EXISTS; })` → `printDialog()` → `print()`; `PrinterException` → dialog error. Tombol Tutup. CTOR tanpa setVisible.

- [ ] **Step 6: FormRiwayat.** Tabel penjualan terbaru-dulu (kolom: Nota, Tanggal, Kasir, Member, Metode, Total) + 2 DatePicker filter + search nota live; Kasir → `lapPenjualanByUser` + filter; Admin → `lapPenjualan`. Double-klik/Enter baris → `getByNoNota` + `getDetailByPenjualan` → StrukDialog (diskonMember dipecah? simpan hanya total gabungan — tampilkan "Diskon: X" satu baris + catatan; potongan terpisah TIDAK tersimpan → struk riwayat tampil Diskon total. Dinyatakan eksplisit, bukan bug).

- [ ] **Step 7: Verifikasi.** mvn compile; probe TEST-R3: syarat batas (qty 49 vs 50; subtotal 749999 vs 750000 via harga mainan), 3 metode (non-tunai auto), tolak potongan > sisa, tolak bayar kurang, save → kolom metode/diskon terbaca balik, riwayat instantiate + filter, struk instantiate; DB cleanup + COUNT 0. Commit `feat(r3-pos): metode bayar, diskon, struk cetak, riwayat`.

### Task 13: Retur + Member + Pembelian + Kategori

**Files:**
- Modify: `view/FormRetur.java`, `view/FormMember.java`, `view/FormPembelian.java`, `view/FormKategori.java`, `dao/MemberDAO.java`, `dao/MemberDAOImpl.java`, `dao/BukuDAO.java`, `dao/BukuDAOImpl.java`, `model/Kategori.java`, `dao/KategoriDAO.java`, `dao/KategoriDAOImpl.java`

**Interfaces:**
- Consumes: Task 12 (metode/diskon tidak dipakai di sini); `BukuDAO.countByKategori(): List<LapGrafik>` (label=namaKategori, nilai=count — BARU); `MemberDAO.lapMember` BELUM (Task 15) — Task 13 hanya COUNT transaksi: tambah `MemberDAO.countTransaksi(): List<LapGrafik>` (label=nama, nilai=count)
- Produces: `Kategori(deskripsi)` + DAO map/insert/update; `BukuDAO.countByKategori`; `MemberDAO.countTransaksi`

- [ ] **Step 1: Kategori deskripsi.** DAO+model+`FormKategori` field deskripsi (opsional) + kolom tabel (ID, Nama, Deskripsi, Jml Buku via `countByKategori` di-join di `loadTable`, 0 bila tak ada).
- [ ] **Step 2: Retur daftar-langsung.** `loadTable()` = 50 nota terbaru (`PenjualanDAO` — TAMBAH `listTerbaru(int limit): List<Penjualan>` di Task ini, eksplisit) + search live filter nota; pilih baris → isi item seperti sekarang.
- [ ] **Step 3: Member kolom transaksi** via `countTransaksi` + hint cara pakai ("Pilih member di POS untuk diskon...").
- [ ] **Step 4: Pembelian hint panel** 3 langkah + validasi pesan (kode ada, pola Task 4).
- [ ] **Step 5: Verifikasi** (compile; probe: kategori CRUD deskripsi, count>0 untuk Fiksi, retur list 50 + filter, member count, pembelian instantiate; cleanup). Commit `feat(r3-master): retur list, kategori deskripsi, hint`.

### Task 14: Menu profil/jam + Dashboard

**Files:**
- Create: `view/FormDashboard.java`, `view/ProfilSaya.java`
- Modify: `view/MenuUtama.java`, `dao/PenjualanDAO.java` (+`omzetPerHari(a,b):List<LapGrafik>`, `+itemTerjual(a,b):int`), `dao/PenjualanDAOImpl.java`, `pom.xml` (+jfreechart eksplisit — versi ikut `mvn dependency:tree` JasperReports, catat)

**Interfaces:**
- Consumes: `lapTerlaris`, `getStokMenipis`, `Sesi`, `AppConfig.STOK_MENIPIS`
- Produces: `FormDashboard` (JPanel no-arg, FQN); `omzetPerHari` SQL `SELECT DATE(tanggal) d, SUM(total) s FROM penjualan WHERE DATE(tanggal) BETWEEN ? AND ? GROUP BY d ORDER BY d`; `itemTerjual` SQL `SELECT COALESCE(SUM(qty),0) FROM detail_penjualan d JOIN penjualan p ... WHERE DATE(p.tanggal) BETWEEN ? AND ?`

- [ ] **Step 1: MenuUtama.** Label seksi ("MASTER", "TRANSAKSI", "LAPORAN") di sidebar; highlight tombol aktif (simpan referensi tombol-terakhir, kembalikan style + set aktif pada `bukaModul` — refactor kecil terkonsentrasi di 1 method + helper); kartu profil bawah (avatar `ACCOUNT_CIRCLE` verified, nama, role, `JLabel jam` via `javax.swing.Timer(1000)` format HH:mm:ss + tanggal `dd MMM yyyy` locale `new Locale("id")`); klik kartu → `ProfilSaya`; tombol Dashboard (ikon `VIEW_DASHBOARD` verified, mode: konten default saat MenuUtama dibuka) + Riwayat (ikon `HISTORY` verified); gating Kasir: Dashboard+Riwayat visible, master-User-Pembelian hidden (existing + User).
- [ ] **Step 2: ProfilSaya** (JDialog modal, owner MenuUtama): label username (kunci) + field nama + password-baru + konfirmasi (kosong = tidak ganti) → validasi (nama wajib; password bila diisi ≥4 + sama) → `UserDAO.update` (password lama bila kosong) → refresh kartu profil (callback `Runnable onSaved` ctor param) + `Sesi.userLogin` update.
- [ ] **Step 3: Dashboard.** 4 kartu angka (omzet/transaksi/item hari ini via `omzetPerHari(today)` sum / `lapPenjualan(today).size()` / `itemTerjual(today)`; stok menipis `getStokMenipis().size()`); bar chart omzet 7 hari (`ChartFactory.createBarChart`, `ChartPanel`, data `omzetPerHari(now-6,now)`); bar top-5 buku bulan ini (`lapTerlaris(firstOfMonth,now)` take 5 — bila >5); list stok menipis (tabel mini kode/judul/stok, double-klik → info "buka Form Buku" saja, TIDAK navigasi lintas-form).
- [ ] **Step 4: Verifikasi** (compile; probe: instantiate Dashboard (angka bertipe benar), ProfilSaya, MenuUtama + kartu profil terisi (Sesi dummy? Sesi null-safe terbukti — set Sesi admin manual di probe lalu cek label, lalu clear); jfreechart di tree; cleanup). Commit `feat(r3-menu): profil, jam, dashboard`.

### Task 15: 10 laporan cantik + chart + Excel

**Files:**
- Create: `model/{LapMember.java(kodeMember,nama,transaksi,belanja), LapRetur.java(noRetur,tanggal,noNota,judul,qty,alasan), LapLabaKategori.java(kategori,qty,omzet,laba), LapOmzetKasir.java(kasir,transaksi,omzet)}`, `util/ExcelExporter.java`, `reports/{lap_member,lap_retur,lap_laba_kategori,lap_omzet_kasir}.jrxml`
- Modify: `pom.xml` (+poi-ooxml:5.2.5), `dao/{MemberDAO,ReturDAO,BukuDAO,PenjualanDAO}` (+lapMember/lapRetur/lapLabaKategori/lapOmzetKasir(a,b)), `view/FormLaporan.java` (10 jenis + excel + grafik-params), 6 jrxml lama (restyle + chart di 3)

**Interfaces:**
- Consumes: semua lap* Task 2 + Task 12 (LapPenjualan metode/diskon tampil di jrxml penjualan)
- Produces: `ExcelExporter.export(String sheet, String[] header, List<List<Object>> rows, boolean withTotal, File out)` (XSSF: header bold fill hitam font putih, border tipis, Double `#,##0`, autosize, baris TOTAL)

- [ ] **Step 1: Dep + bean + query.** Tambah poi-ooxml. Bean 4 baru (ctor+getter/setter). Query: `lapMember` (member LEFT JOIN penjualan GROUP BY member; transaksi COUNT, belanja SUM, filter tanggal di JOIN); `lapRetur` (retur JOIN penjualan+buku, BETWEEN); `lapLabaKategori` (detail JOIN penjualan JOIN buku JOIN kategori GROUP BY kategori; laba=(hj-hb)*qty di Java); `lapOmzetKasir` (penjualan JOIN users GROUP BY user; transaksi COUNT, omzet SUM). Context7 bila ragu.
- [ ] **Step 2: ExcelExporter** (verbatim pola):

```java
package util;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
public class ExcelExporter {
    public static void export(String sheet, String[] header, List<List<Object>> rows, boolean withTotal, File out) throws Exception {
        try (Workbook wb = new XSSFWorkbook(); FileOutputStream fo = new FileOutputStream(out)) {
            Sheet sh = wb.createSheet(sheet);
            CellStyle hs = wb.createCellStyle();
            Font hf = wb.createFont(); hf.setBold(true); hf.setColor(IndexedColors.WHITE.getIndex());
            hs.setFont(hf); hs.setFillForegroundColor(IndexedColors.BLACK.getIndex());
            hs.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            CellStyle bs = wb.createCellStyle(); bs.setBorderTop(BorderStyle.THIN); bs.setBorderBottom(BorderStyle.THIN);
            bs.setBorderLeft(BorderStyle.THIN); bs.setBorderRight(BorderStyle.THIN);
            CellStyle ns = wb.createCellStyle(); ns.cloneStyleFrom(bs);
            ns.setDataFormat(wb.createDataFormat().getFormat("#,##0"));
            int r = 0; Row hr = sh.createRow(r++);
            for (int i = 0; i < header.length; i++) { Cell c = hr.createCell(i); c.setCellValue(header[i]); c.setCellStyle(hs); }
            for (List<Object> row : rows) {
                Row rr = sh.createRow(r++);
                for (int i = 0; i < row.size(); i++) {
                    Cell c = rr.createCell(i); Object v = row.get(i);
                    if (v instanceof Number) { c.setCellValue(((Number) v).doubleValue()); c.setCellStyle(ns); }
                    else { c.setCellValue(v == null ? "" : v.toString()); c.setCellStyle(bs); }
                }
            }
            if (withTotal) { /* baris TOTAL: label di kolom pertama, SUM formula kolom angka terakhir */ }
            for (int i = 0; i < header.length; i++) sh.autoSizeColumn(i);
            wb.write(fo);
        }
    }
}
```

Baris TOTAL: label "TOTAL" di sel pertama + formula `SUM(colAwal:colAkhir)` di kolom angka (1 kolom terakhir bila banyak). Bila API POI beda → Context7, catat.

- [ ] **Step 3: FormLaporan 10 jenis.** Combo Admin 10 / Kasir 1 (Penjualan). Mapping jenis→(jrxml, dao-call, excel-mapper, grafik-param bila ada): Penjualan/Pendapatan/Terlaris/LabaKategori/OmzetKasir dapat `DATASET_GRAFIK` (JRBeanCollectionDataSource dari `omzetPerHari/labaPerHari/top10-dari-lapTerlaris/lapLabaKategori/lapOmzetKasir`). Tombol Export Excel (success) → mapper jenis→(header,rows) → save `.xlsx`. CETAK_PADA param (now, format).
- [ ] **Step 4: jrxml.** Restyle 6 lama (template: APP_NAME 18 + judul 14 + PERIODE + CETAK_PADA + header hitam/putih + belang `$V{REPORT_COUNT}%2==0` + summary box + footer) + 4 baru ikut template. Chart (barChart, categoryDataset dari `$P{DATASET_GRAFIK}`, field `label`(S)+`nilai`(D)) di summary: penjualan, pendapatan, terlaris, laba_kategori, omzet_kasir. Laporan penjualan: kolom Metode + Diskon.
- [ ] **Step 5: Verifikasi** (compile; probe: 10 jrxml compile+fill DAO; 10 xlsx export → baca balik via POI assert header+1 baris; grafik-params non-null; FormLaporan instantiate; cleanup). Commit `feat(r3-laporan): 10 laporan, chart, excel`.

### Task 16: E2E ronde 3 + package + docs + review

**Files:** Modify `README.md`, `TESTING.md`. Bersih: probe luar repo, TEST-R3 0 sisa.

- [ ] **Step 1: E2E probe** (skenario ronde-1 ringkas + baru): 3 metode (tunai kurang ditolak/pas; transfer/qris auto), syarat diskon (49 vs 50; 700rb vs 750rb), potongan batas, struk field kasir+metode, riwayat filter + buka ulang, retur list-50, kategori deskripsi + count, profil update + relogin, dashboard angka = query, 10 jrxml fill, 10 xlsx round-trip, viewer-code ada. `PROBE-R3-OK`.
- [ ] **Step 2: package + jar alive 15s + shaded-jar exportPdf + exportXlsx-dari-jar** (keduanya WAJIB dari jar saja).
- [ ] **Step 3: README/TESTING update** (fitur baru, ALTER/migrasi note, akun, skenario baru).
- [ ] **Step 4: Commit + laporan** (`test(r3): e2e + packaging` + docs). DoD Task 16 = gerbang final review.

## Self-Review

1. Spec coverage: §1→T12S1; §2→T12S4; §3→T12S5-6; §4→T13; §5→T14S1-2; §6→T15S4 (+query T15S1); §7→T14S3; §8→T15S2-3; §9→T12-14 gating + T16; §10→T16. NOL gap.
2. Placeholder: tidak ada TBD; semua SQL/kelas/metode/signatur konkret; chart dataset via DATASET_GRAFIK eksplisit; Excel pola penuh.
3. Type consistency: LapPenjualan(+metodeBayar,+diskon) dipakai T12 jrxml + T15; LapGrafik(label,nilai) dipakai countByKategori/countTransaksi/omzetPerHari; StrukDialog ctor baru konsisten T12S5→T12S6; listTerbaru (T13) vs riwayat (lapPenjualan + filter di form — riwayat TIDAK butuh listTerbaru; retur pakai listTerbaru. Konsisten).
