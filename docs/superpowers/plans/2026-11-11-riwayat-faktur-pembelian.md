# Riwayat Faktur Pembelian Implementation Plan

> **For agentic workers:** Execute this plan inline (no subagents — user instruction).
> Use checkbox (`- [x]`) syntax and update the **Status Ledger** after every task.
> If the context window is compacted, read this file top-to-bottom, then continue
> from the first unchecked task.

**Goal:** Tambahkan tab **Riwayat Faktur** di `FormPembelian` dengan filter tanggal +
pencarian, tabel 5 kolom, dan dialog **faktur pembelian formal** yang bisa dicetak dan
diekspor ke PDF. Perbaiki teks mojibake dan header tabel kanan yang terpotong. Alur
input/simpan/stok yang ada tidak boleh berubah.

**Architecture:**
- `FormPembelian` menjadi `JTabbedPane` 2 tab: `Input Pembelian` (UI sekarang, utuh)
  dan `Riwayat Faktur` (baru).
- Riwayat memakai `PembelianDAO.lapPembelian(dari, sampai)` yang sudah ada, difilter
  live di memori untuk no. faktur/nama supplier.
- Detail faktur memakai method baru `PembelianDAO.getDetailByPembelian(int)`.
- Dialog baru `FakturPembelianFormalDialog` (bukan `FakturPembelianDialog` pasca-simpan,
  bukan `StrukDialog` penjualan). Cetak/PDF memakai JasperReports yang sudah ada.
- Template baru `src/main/resources/reports/faktur_pembelian.jrxml`.

**Tech Stack:** Java 11 target (runtime JDK 25), Swing, Maven 3.9.9, MySQL/MariaDB
XAMPP, JasperReports 6.21.3, JUnit 5.10.2, LGoodDatePicker.

**Design approved by cuii (2026-11-11):**
- Riwayat = tab di `FormPembelian`, bukan form/menu sidebar baru.
- Faktur lama dibuka di **dialog faktur formal baru**; nota penjualan tidak disentuh.
- Filter tanggal + cari; default 30 hari terakhir; kolom: No. Faktur, Tanggal,
  Supplier, Penerima, Total.
- Faktur bisa **Cetak** dan **Export PDF**.
- Bug teks: arrow `â†’`, emoji alamat/telp rusak, header tabel kanan terpotong.
- Data: list `lapPembelian`; detail method DAO baru; supplier via `SupplierDAO.getById`.
- `saveWithDetail`, stok, validasi, `StrukDialog`, `FakturPembelianDialog`, dan
  `FormRiwayat` **tidak diubah perilakunya**.

---

## Definition of Done (DoD)

Fitur dianggap selesai hanya jika semua poin ini terbukti:

1. `FormPembelian` punya 2 tab: `Input Pembelian` dan `Riwayat Faktur`; tab input
   memuat UI lama tanpa perubahan alur (pilih supplier → cari buku → qty/harga →
   tambah → simpan → stok bertambah → dialog bukti pasca-simpan).
2. Tab riwayat hanya bisa diakses lewat form Pembelian (admin-only), default periode
   30 hari terakhir, tanggal Dari/Sampai bisa diubah, pencarian no. faktur/nama
   supplier bekerja live, urutan terbaru di atas.
3. Tabel riwayat tepat 5 kolom: `No. Faktur`, `Tanggal`, `Supplier`, `Penerima`,
   `Total`; total terformat Rupiah; ringkasan jumlah faktur + total nilai tampil.
4. Double-click, Enter, atau tombol `Buka Faktur` membuka
   `FakturPembelianFormalDialog` berisi:
   - header toko + logo + tulisan `FAKTUR PEMBELIAN`;
   - No. Faktur, Tanggal, Penerima;
   - blok supplier (nama, alamat, telp) dan blok toko penerima;
   - tabel item (No, Kode, Judul, Qty, Harga Beli, Subtotal);
   - ringkasan total jenis/qty/biaya dan area tanda tangan.
5. Tombol **Cetak** memanggil Jasper print dialog (bukan `JTextArea.print`);
   tombol **Export PDF** menyimpan file PDF valid > 0 byte dengan nama default
   `Faktur_<noFaktur>.pdf`.
6. `faktur_pembelian.jrxml` berhasil dikompilasi dan di-fill (test Jasper), termasuk
   saat data kosong dan saat data nyata.
7. Mojibake `â†’` hilang (menjadi `→`), emoji alamat/telp diganti label teks biasa,
   dan seluruh header tabel kanan (`Stok Awal`, `Qty Beli`, `Stok Akhir`, `Harga`,
   `Subtotal`) tidak terpotong.
8. Unit test baru lulus; seluruh test lama tetap lulus; `mvn clean package` sukses.
9. E2E probe membuktikan: tab riwayat tampil berisi data seed, faktur terbuka,
   PDF terbentuk, simpan pembelian menambah stok + baris riwayat, lalu data uji
   dibersihkan dan stok kembali seperti semula.
10. Screenshot `pembelian-riwayat.png` dan `pembelian-faktur-formal.png` sudah
    diperiksa secara visual (tidak ada teks terpotong/mojibake).
11. `StrukDialog.java`, `FormRiwayat.java`, dan `FakturPembelianDialog.java` tidak
    berubah; `git diff` tidak menyentuh `saveWithDetail`.
12. Commit dibuat di branch fitur; `git status` bersih; **tidak push** kecuali cuii
    meminta.

---

## Global Constraints

- Jangan dispatch subagent. Kerjakan dan verifikasi sendiri.
- Jangan tambah dependency Maven baru.
- Jangan ubah schema database.
- Jangan ubah `StrukDialog`, `FormRiwayat`, `FakturPembelianDialog` pasca-simpan,
  atau transaksi `saveWithDetail`.
- Jangan push.
- Gunakan Bahasa Indonesia untuk teks UI dan laporan akhir; sapa user `cuii`.
- Source encoding UTF-8 (sudah di `pom.xml`). File yang diedit harus disimpan UTF-8.
- Maven: `C:\Users\alvin\tools\apache-maven-3.9.9\bin\mvn.cmd`.
- MySQL: `C:\xampp\mysql\bin\mysql.exe`, user `root`, password kosong, DB
  `db_toko_buku`.
- Screenshot/probe sementara di `C:\Users\alvin\AppData\Local\Temp\opencode`;
  jangan commit file probe.
- Branch saat ini: `feat/seed-data-almira`, HEAD `9701f9a`, tree bersih.
  Buat branch fitur baru sebelum mengubah kode:
  `git switch -c feat/riwayat-faktur-pembelian`.

---

## Execution Order

| Task | Isi | Estimasi |
|------|-----|----------|
| 0 | Baseline branch, test, dan recon akhir | 5 menit |
| 1 | DAO `getDetailByPembelian` | 10 menit |
| 2 | `ReportHelper.print` | 5 menit |
| 3 | Template Jasper + test kompilasi | 20 menit |
| 4 | Dialog faktur formal + unit test | 40 menit |
| 5 | Tab riwayat di `FormPembelian` + unit test + bug fix | 45 menit |
| 6 | E2E probe, PDF, simpan/stok/cleanup, screenshot | 30 menit |
| 7 | Docs, verifikasi akhir, commit, ledger | 15 menit |

---

## Status Ledger

Update kolom Status menjadi `DONE`/`BLOCKED` + catatan singkat setelah task selesai.
Ini titik pemulihan setelah compaction.

| Task | Status | Bukti / Catatan |
|------|--------|-----------------|
| 0 Baseline | DONE | Branch `feat/riwayat-faktur-pembelian`; 21 test lulus; DB baseline 36 pembelian / 79 detail. |
| 1 DAO detail | DONE | `getDetailByPembelian` ditambahkan; query seed tervalidasi (faktur 1 punya 2 detail); compile sukses. |
| 2 Report print | DONE | `ReportHelper.print` ditambahkan memakai `JasperPrintManager.printReport(..., true)`; compile sukses. |
| 3 JRXML + test | DONE | `faktur_pembelian.jrxml` dibuat; `ReportCompilationTest` 8/8 lulus; tanpa mojibake. |
| 4 Dialog formal | DONE | `FakturPembelianFormalDialog` + 4 unit test lulus; helper params/rows/format headless-safe. |
| 5 FormPembelian | DONE | 2 tab, filter/cari, dialog formal, mojibake & header fix; 3 test riwayat lulus; total suite 29/29 lulus. |
| 6 E2E + cleanup | DONE | Probe `PROBE-R8-PEMBELIAN-OK`: 2 tab, 23 faktur seed, dialog formal, PDF `%PDF` 1.058.299 B, simpan menambah stok 219→220 + baris riwayat, cleanup 36/79 & stok 219. Screenshot: `r8-shots/pembelian-{input,riwayat,faktur-formal}.png`. DB `verify_seed_almira.sql` semua OK. Bug `TGL_CETAK` null ditemukan & diperbaiki. |
| 7 Docs + commit | DONE | `TESTING.md` skenario 6b ditambahkan; `mvn clean package` 29/29 lulus + JAR; diff bersih; `StrukDialog`/`FormRiwayat`/`FakturPembelianDialog` & `saveWithDetail` tidak berubah; commit feature ini. Tidak push. |

---

### Task 0: Baseline dan branch fitur

**Files:** tidak ada perubahan kode.

**Interfaces:**
- Consumes: branch `feat/seed-data-almira`, HEAD `9701f9a`.
- Produces: branch `feat/riwayat-faktur-pembelian`, baseline test lulus.

- [x] **Step 1: Pastikan tree bersih dan buat branch**

```powershell
git status --short
git branch --show-current
git log -1 --oneline
git switch -c feat/riwayat-faktur-pembelian
```

Expected: tree bersih; branch baru aktif; HEAD tetap `9701f9a`.

- [x] **Step 2: Jalankan baseline test**

```powershell
& 'C:\Users\alvin\tools\apache-maven-3.9.9\bin\mvn.cmd' -q test
```

Expected: 21 test lulus (7 kelas test), BUILD SUCCESS.

- [x] **Step 3: Catat baseline DB**

```powershell
& 'C:\xampp\mysql\bin\mysql.exe' -u root -N -e "SELECT COUNT(*) FROM db_toko_buku.pembelian; SELECT COUNT(*) FROM db_toko_buku.detail_pembelian;"
```

Expected: `36` pembelian dan jumlah detail sesuai seed. Catat angkanya di ledger.

- [x] **Step 4: Update ledger Task 0**

---

### Task 1: DAO detail pembelian

**Files:**
- Modify: `src/main/java/dao/PembelianDAO.java`
- Modify: `src/main/java/dao/PembelianDAOImpl.java`

**Interfaces:**
- Produces: `List<DetailPembelian> getDetailByPembelian(int idPembelian)`.
- Query join `buku` agar `kodeBuku` dan `judul` terisi; urut `d.id_detail`.
- Tidak mengubah `saveWithDetail`, `lapPembelian`, atau method lain.

- [x] **Step 1: Tambahkan signature di interface**

Di `PembelianDAO`, setelah `saveWithDetail`:

```java
List<DetailPembelian> getDetailByPembelian(int idPembelian);
```

- [x] **Step 2: Implementasi di `PembelianDAOImpl`**

Tambahkan method (pola sama dengan `PenjualanDAOImpl.getDetailByPenjualan`):

```java
@Override
public List<DetailPembelian> getDetailByPembelian(int idPembelian) {
    String sql = "SELECT d.id_detail, d.id_pembelian, d.id_buku, b.kode_buku, b.judul, "
            + "d.qty, d.harga_beli, d.subtotal "
            + "FROM detail_pembelian d "
            + "JOIN buku b ON d.id_buku = b.id_buku "
            + "WHERE d.id_pembelian=? ORDER BY d.id_detail";
    List<DetailPembelian> list = new ArrayList<>();
    try (Connection c = Koneksi.getConnection();
         PreparedStatement ps = c.prepareStatement(sql)) {
        ps.setInt(1, idPembelian);
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                DetailPembelian d = new DetailPembelian();
                d.setIdDetail(rs.getInt("id_detail"));
                d.setIdPembelian(rs.getInt("id_pembelian"));
                d.setIdBuku(rs.getInt("id_buku"));
                d.setKodeBuku(rs.getString("kode_buku"));
                d.setJudul(rs.getString("judul"));
                d.setQty(rs.getInt("qty"));
                d.setHargaBeli(rs.getDouble("harga_beli"));
                d.setSubtotal(rs.getDouble("subtotal"));
                list.add(d);
            }
        }
        return list;
    } catch (SQLException e) {
        throw new RuntimeException("Gagal ambil detail pembelian: " + e.getMessage(), e);
    }
}
```

- [x] **Step 3: Verifikasi kompilasi**

```powershell
& 'C:\Users\alvin\tools\apache-maven-3.9.9\bin\mvn.cmd' -q -DskipTests compile
```

Expected: BUILD SUCCESS.

- [x] **Step 4: Verifikasi query terhadap seed (bukti DAO)**

Gunakan `mysql.exe` untuk memastikan faktur pertama punya detail join yang benar:

```sql
SELECT p.id_pembelian, p.no_faktur, COUNT(d.id_detail) AS jml_detail
FROM pembelian p
LEFT JOIN detail_pembelian d ON d.id_pembelian = p.id_pembelian
GROUP BY p.id_pembelian, p.no_faktur
ORDER BY p.id_pembelian
LIMIT 3;
```

Expected: setiap faktur seed punya `jml_detail >= 1`.

Verifikasi Java method lewat probe kecil nanti di Task 6; jangan buat test JDBC
yang bergantung DB di suite unit.

- [x] **Step 5: Update ledger Task 1**

---

### Task 2: `ReportHelper.print`

**Files:**
- Modify: `src/main/java/report/ReportHelper.java`

**Interfaces:**
- Produces: `public static void print(String jrxmlPath, List<?> data, Map<String,Object> params) throws JRException`.
- Memakai `JasperPrintManager.printReport(fill(...), true)` agar dialog printer muncul.
- `show` dan `exportPdf` tidak berubah.

- [x] **Step 1: Tambah import**

```java
import net.sf.jasperreports.engine.JasperPrintManager;
```

- [x] **Step 2: Tambah method setelah `show`**

```java
public static void print(String jrxmlPath, List<?> data, Map<String, Object> params) throws JRException {
    JasperPrint jp = fill(jrxmlPath, data, params);
    JasperPrintManager.printReport(jp, true);
}
```

- [x] **Step 3: Kompilasi**

```powershell
& 'C:\Users\alvin\tools\apache-maven-3.9.9\bin\mvn.cmd' -q -DskipTests compile
```

Expected: BUILD SUCCESS.

- [x] **Step 4: Update ledger Task 2**

---

### Task 3: Template `faktur_pembelian.jrxml` (TDD)

**Files:**
- Modify: `src/test/java/report/ReportCompilationTest.java`
- Create: `src/main/resources/reports/faktur_pembelian.jrxml`

**Interfaces:**
- Consumes: list `DetailPembelian` (field: `kodeBuku`, `judul`, `qty`, `hargaBeli`, `subtotal`).
- Consumes params: `APP_NAME`, `NO_FAKTUR`, `TANGGAL`, `SUPPLIER`, `ALAMAT_SUPPLIER`,
  `TELP_SUPPLIER`, `PENERIMA`, `TOTAL`, `TOTAL_QTY`, `TOTAL_JENIS`, `KOTA`,
  `TGL_CETAK`, `PETUGAS`, `ALAMAT_TOKO`, `TELP_TOKO`.
- Produces: template A4 portrait formal invoice, bukan struk teks.

- [x] **Step 1 (RED): Tambah path ke test kompilasi**

Tambahkan `"/reports/faktur_pembelian.jrxml"` ke `@ValueSource` di
`ReportCompilationTest`. Tambahkan juga param yang dipakai template:

```java
params.put("NO_FAKTUR", "FB-20261111-0001");
params.put("TANGGAL", "11 November 2026 09:30");
params.put("SUPPLIER", "PT Supplier Test");
params.put("ALAMAT_SUPPLIER", "Jl. Supplier No. 1");
params.put("TELP_SUPPLIER", "021-123456");
params.put("PENERIMA", "Admin Test");
params.put("TOTAL", 0.0);
params.put("TOTAL_QTY", 0);
params.put("TOTAL_JENIS", 0);
params.put("ALAMAT_TOKO", "Jl. Toko No. 1");
params.put("TELP_TOKO", "021-999999");
```

Run:

```powershell
& 'C:\Users\alvin\tools\apache-maven-3.9.9\bin\mvn.cmd' -q -Dtest=ReportCompilationTest test
```

Expected: FAIL karena template belum ada
(`Template report tidak ditemukan di classpath: /reports/faktur_pembelian.jrxml`).

- [x] **Step 2 (GREEN): Buat template Jasper**

Buat `src/main/resources/reports/faktur_pembelian.jrxml` dengan struktur:

```text
pageWidth=595, pageHeight=842, margins=30, columnWidth=535
<parameter> APP_NAME, NO_FAKTUR, TANGGAL, SUPPLIER, ALAMAT_SUPPLIER,
            TELP_SUPPLIER, PENERIMA, TOTAL (Double), TOTAL_QTY (Integer),
            TOTAL_JENIS (Integer), KOTA, TGL_CETAK, PETUGAS,
            ALAMAT_TOKO, TELP_TOKO
<field> kodeBuku, judul, qty (Integer), hargaBeli (Double), subtotal (Double)
<variable name="NO"> calculation="Count" -> nomor baris 1..n
```

Layout wajib:

1. **Title band**
   - Logo `images/logo_ajb_transparent.png` 56×56 di kiri.
   - `APP_NAME` font 16 bold, `ALAMAT_TOKO`, `TELP_TOKO` font 8.
   - Dua garis horizontal.
   - `FAKTUR PEMBELIAN` font 14 bold, center.
   - Blok kanan: `No. Faktur: $P{NO_FAKTUR}`, `Tanggal: $P{TANGGAL}`.
   - Blok kiri `SUPPLIER`: nama, alamat, telp.
   - Blok kanan `KEPADA`: `APP_NAME` + alamat toko.
2. **Column header** (tinggi 24, background `#1E293B`, teks putih):
   `No` 30, `Kode` 60, `Judul Buku` 220, `Qty` 45, `Harga Beli` 85,
   `Subtotal` 95 → total 535.
3. **Detail band** (tinggi 22): nomor `$V{NO}`, field kode/judul, qty rata kanan,
   harga & subtotal pattern `#,##0` rata kanan.
4. **Summary**
   - Garis atas.
   - `Total Jenis Buku: $P{TOTAL_JENIS} judul`
   - `Total Qty Masuk: $P{TOTAL_QTY} pcs`
   - `TOTAL BIAYA: $P{TOTAL}` (bold, pattern `#,##0`).
   - Tanda tangan kiri `Penerima,` + `$P{PENERIMA}`; kanan
     `$P{KOTA} + ", " + $P{TGL_CETAK}` + `Menyetujui,` + `Kepala Toko`.
5. **Page footer**: `Halaman $V{PAGE_NUMBER} dari $V{PAGE_COUNT}`.

Pastikan semua `textField` string memakai `isBlankWhenNull="true"` bila nilai bisa
null. Jangan memakai scriptlet; cukup ekspresi CDATA.

- [x] **Step 3 (GREEN): Jalankan test kompilasi**

```powershell
& 'C:\Users\alvin\tools\apache-maven-3.9.9\bin\mvn.cmd' -q -Dtest=ReportCompilationTest test
```

Expected: 8 report lulus (7 lama + 1 baru), BUILD SUCCESS.

- [x] **Step 4: Cek tidak ada mojibake di template**

```powershell
Select-String -Path src\main\resources\reports\faktur_pembelian.jrxml -Pattern 'â|ðŸ|Ã|Â'
```

Expected: tidak ada match.

- [x] **Step 5: Update ledger Task 3**

---

### Task 4: Dialog faktur formal (TDD)

**Files:**
- Create: `src/test/java/view/FakturPembelianFormalDialogTest.java`
- Create: `src/main/java/view/FakturPembelianFormalDialog.java`

**Interfaces:**
- Constructor: `FakturPembelianFormalDialog(Frame owner, Pembelian pembelian,
  List<DetailPembelian> items, Supplier supplier, String penerima)`.
- Static helper yang diuji tanpa membuat dialog (headless-safe):
  - `static Map<String, Object> buildReportParams(Pembelian h, Supplier s,
    List<DetailPembelian> items, String penerima)`
  - `static Object[][] buildItemRows(List<DetailPembelian> items)`
  - `static String formatTanggal(LocalDateTime tgl)`
  - `static String safe(String value)`
- Konstanta: `static final String TEMPLATE = "/reports/faktur_pembelian.jrxml"`.
- Produces UI: paper putih, header toko + logo, blok supplier/penerima, tabel item,
  ringkasan, tanda tangan, tombol `Cetak`, `Export PDF`, `Tutup`.

- [x] **Step 1 (RED): Tulis unit test helper**

Buat `src/test/java/view/FakturPembelianFormalDialogTest.java`:

```java
package view;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import model.DetailPembelian;
import model.Pembelian;
import model.Supplier;
import org.junit.jupiter.api.Test;

class FakturPembelianFormalDialogTest {

    private DetailPembelian detail(String kode, String judul, int qty, double harga) {
        DetailPembelian d = new DetailPembelian();
        d.setKodeBuku(kode);
        d.setJudul(judul);
        d.setQty(qty);
        d.setHargaBeli(harga);
        d.setSubtotal(qty * harga);
        return d;
    }

    @Test
    void buildReportParamsMengisiHeaderTotalDanToko() {
        Pembelian h = new Pembelian();
        h.setNoFaktur("FB-20261111-0001");
        h.setTanggal(LocalDateTime.of(2026, 11, 11, 9, 30));
        h.setNamaSupplier("PT Fallback");
        h.setTotal(30000);

        Supplier s = new Supplier(1, "PT Supplier", "Jl. Supplier 1", "021-123");
        List<DetailPembelian> items = Arrays.asList(
                detail("BK-001", "Buku A", 2, 10000),
                detail("BK-002", "Buku B", 1, 10000));

        Map<String, Object> p = FakturPembelianFormalDialog
                .buildReportParams(h, s, items, "Admin Test");

        assertEquals("FB-20261111-0001", p.get("NO_FAKTUR"));
        assertEquals("11 November 2026 09:30", p.get("TANGGAL"));
        assertEquals("PT Supplier", p.get("SUPPLIER"));
        assertEquals("Jl. Supplier 1", p.get("ALAMAT_SUPPLIER"));
        assertEquals("021-123", p.get("TELP_SUPPLIER"));
        assertEquals("Admin Test", p.get("PENERIMA"));
        assertEquals(30000.0, (Double) p.get("TOTAL"), 0.001);
        assertEquals(3, p.get("TOTAL_QTY"));
        assertEquals(2, p.get("TOTAL_JENIS"));
        assertEquals("Bekasi", p.get("KOTA"));
    }

    @Test
    void buildReportParamsMemakaiFallbackSupplierDanTotalDetail() {
        Pembelian h = new Pembelian();
        h.setNoFaktur("FB-2");
        h.setNamaSupplier("Supplier Header");
        h.setTotal(0);

        List<DetailPembelian> items = Collections.singletonList(
                detail("BK-003", "Buku C", 2, 15000));

        Map<String, Object> p = FakturPembelianFormalDialog
                .buildReportParams(h, null, items, null);

        assertEquals("Supplier Header", p.get("SUPPLIER"));
        assertEquals("-", p.get("ALAMAT_SUPPLIER"));
        assertEquals(30000.0, (Double) p.get("TOTAL"), 0.001);
        assertEquals("-", p.get("PENERIMA"));
    }

    @Test
    void buildItemRowsMenomoriDanMemformatRupiah() {
        Object[][] rows = FakturPembelianFormalDialog.buildItemRows(Arrays.asList(
                detail("BK-001", "Buku A", 2, 10000),
                detail("BK-002", "Buku B", 1, 5000)));

        assertEquals(2, rows.length);
        assertArrayEquals(new Object[]{1, "BK-001", "Buku A", 2,
                "Rp 10.000", "Rp 20.000"}, rows[0]);
        assertArrayEquals(new Object[]{2, "BK-002", "Buku B", 1,
                "Rp 5.000", "Rp 5.000"}, rows[1]);
    }

    @Test
    void formatTanggalDanSafeMenanganiNull() {
        assertEquals("-", FakturPembelianFormalDialog.formatTanggal(null));
        assertEquals("-", FakturPembelianFormalDialog.safe(null));
        assertEquals("-", FakturPembelianFormalDialog.safe("  "));
        assertEquals("Admin", FakturPembelianFormalDialog.safe(" Admin "));
        assertEquals("11 November 2026 09:05",
                FakturPembelianFormalDialog.formatTanggal(
                        LocalDateTime.of(2026, 11, 11, 9, 5)));
    }
}
```

Run:

```powershell
& 'C:\Users\alvin\tools\apache-maven-3.9.9\bin\mvn.cmd' -q -Dtest=FakturPembelianFormalDialogTest test
```

Expected: FAIL — class `FakturPembelianFormalDialog` belum ada (compile error).

- [x] **Step 2 (GREEN): Implementasi helper + UI dialog**

Buat `src/main/java/view/FakturPembelianFormalDialog.java` dengan ketentuan:

**Field & konstanta**

```java
static final String TEMPLATE = "/reports/faktur_pembelian.jrxml";
private static final NumberFormat FMT_RP =
        NumberFormat.getInstance(new Locale("id", "ID"));
private static final DateTimeFormatter FMT_TGL =
        DateTimeFormatter.ofPattern("dd MMMM yyyy HH:mm", new Locale("id", "ID"));
private final Pembelian pembelian;
private final List<DetailPembelian> items;
private final Map<String, Object> reportParams;
```

**Helper (package-private static, headless-safe)**

```java
static String safe(String value) {
    return (value == null || value.isBlank()) ? "-" : value.trim();
}

static String formatTanggal(LocalDateTime tgl) {
    return tgl == null ? "-" : tgl.format(FMT_TGL);
}

static String formatRp(double value) {
    return "Rp " + FMT_RP.format((long) Math.round(value));
}

static Object[][] buildItemRows(List<DetailPembelian> items) {
    if (items == null) return new Object[0][0];
    Object[][] rows = new Object[items.size()][6];
    for (int i = 0; i < items.size(); i++) {
        DetailPembelian d = items.get(i);
        rows[i] = new Object[]{
            i + 1,
            safe(d.getKodeBuku()),
            safe(d.getJudul()),
            d.getQty(),
            formatRp(d.getHargaBeli()),
            formatRp(d.getSubtotal())
        };
    }
    return rows;
}

static Map<String, Object> buildReportParams(Pembelian h, Supplier s,
        List<DetailPembelian> items, String penerima) {
    Map<String, Object> p = new HashMap<>();
    double total = (h != null) ? h.getTotal() : 0.0;
    int totalQty = 0;
    int totalJenis = (items != null) ? items.size() : 0;
    if (items != null) {
        double sum = 0.0;
        for (DetailPembelian d : items) {
            totalQty += d.getQty();
            sum += d.getSubtotal();
        }
        if (h == null || total <= 0.0) total = sum;
    }
    p.put("NO_FAKTUR", safe(h != null ? h.getNoFaktur() : null));
    p.put("TANGGAL", formatTanggal(h != null ? h.getTanggal() : null));
    p.put("SUPPLIER", safe(s != null ? s.getNamaSupplier()
            : (h != null ? h.getNamaSupplier() : null)));
    p.put("ALAMAT_SUPPLIER", safe(s != null ? s.getAlamat() : null));
    p.put("TELP_SUPPLIER", safe(s != null ? s.getNoTelp() : null));
    p.put("PENERIMA", safe(penerima));
    p.put("TOTAL", total);
    p.put("TOTAL_QTY", totalQty);
    p.put("TOTAL_JENIS", totalJenis);
    p.put("KOTA", "Bekasi");
    p.put("PETUGAS", safe(penerima));
    p.put("ALAMAT_TOKO", util.AppConfig.ALAMAT_TOKO);
    p.put("TELP_TOKO", util.AppConfig.NO_TELP_TOKO);
    return p;
}
```

**Konstruktor & layout**

- `super(owner, "Faktur Pembelian - " + noFaktur, true)`, `DISPOSE_ON_CLOSE`.
- Background `NeoBrutalTheme.BG`, border empty 12.
- Simpan field `pembelian`, `items`, `reportParams`.
- `JScrollPane` berisi `paper` putih (`NeoShadowBorder` + padding 24).
- Paper header:
  - kiri: `new JLabel(util.LogoUtil.icon(56))`;
  - tengah: `AppConfig.APP_NAME` (display 20), alamat + telp (body 10, muted);
  - kanan: `FAKTUR PEMBELIAN` (display 16), `No. Faktur: ...`,
    `Tanggal: ...`, `Penerima: ...`.
  - garis tebal + tipis (`JSeparator` atau `JPanel` tinggi 2/1).
- Blok dua kolom (`GridLayout(1,2,16,0)`):
  - `SUPPLIER`: nama, alamat, telp dari `Supplier`; fallback `pembelian.getNamaSupplier()`.
  - `KEPADA`: `AppConfig.APP_NAME`, `AppConfig.ALAMAT_TOKO`,
    `AppConfig.NO_TELP_TOKO`.
- Tabel item:
  - model read-only kolom `{"No", "Kode", "Judul", "Qty", "Harga Beli", "Subtotal"}`;
  - isi dari `buildItemRows(items)`;
  - `NeoTableStyle.apply`, `setAutoResizeMode(AUTO_RESIZE_OFF)`,
    lebar `{36, 70, 300, 45, 95, 105}` → total 651;
  - tinggi area tabel minimal 220, scroll horizontal bila perlu.
- Ringkasan kanan (`GridLayout(3,2)`): `Total Jenis Buku`, nilai;
  `Total Qty Masuk`, nilai; `TOTAL BIAYA`, `formatRp(pembelian.getTotal())`
  (bold, maroon).
- Tanda tangan (`GridLayout(1,2,24,0)`):
  - kiri `Penerima,` + ruang + `( <penerima> )`;
  - kanan `Bekasi, <tanggal cetak>` + `Menyetujui,` + `Kepala Toko` +
    `( Kepala Toko Almira )`.
- Panel tombol bawah (di luar paper):
  - `Cetak` (primary, ikon printer) → `doCetak()`;
  - `Export PDF` (secondary/success, ikon download) → `doExportPdf()`;
  - `Tutup` (secondary) → `dispose()`.
  - Beri name: `btnCetakFaktur`, `btnExportPdfFaktur`, `btnTutupFaktur`.
- Ukuran `setSize(960, 800)`, `setMinimumSize(840, 640)`,
  `setLocationRelativeTo(owner)`.

**Aksi cetak/PDF**

```java
private void doCetak() {
    try {
        ReportHelper.print(TEMPLATE, items, reportParams);
    } catch (JRException ex) {
        JOptionPane.showMessageDialog(this,
                "Gagal mencetak faktur: " + ex.getMessage(),
                "Cetak Faktur", JOptionPane.ERROR_MESSAGE);
    }
}

private void doExportPdf() {
    JFileChooser fc = new JFileChooser();
    fc.setDialogTitle("Export Faktur Pembelian ke PDF");
    fc.setFileFilter(new FileNameExtensionFilter("PDF Documents (*.pdf)", "pdf"));
    String no = safe(pembelian != null ? pembelian.getNoFaktur() : "faktur");
    fc.setSelectedFile(new File("Faktur_" + no.replaceAll("[^A-Za-z0-9_-]", "_") + ".pdf"));
    if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
    File out = fc.getSelectedFile();
    if (!out.getName().toLowerCase(Locale.ROOT).endsWith(".pdf")) {
        out = new File(out.getParentFile(), out.getName() + ".pdf");
    }
    try {
        ReportHelper.exportPdf(TEMPLATE, items, reportParams, out);
        JOptionPane.showMessageDialog(this,
                "Faktur berhasil diekspor:\n" + out.getAbsolutePath(),
                "Export PDF", JOptionPane.INFORMATION_MESSAGE);
    } catch (JRException ex) {
        JOptionPane.showMessageDialog(this,
                "Gagal export PDF: " + ex.getMessage(),
                "Export PDF", JOptionPane.ERROR_MESSAGE);
    }
}
```

- [x] **Step 3 (GREEN): Jalankan unit test dialog**

```powershell
& 'C:\Users\alvin\tools\apache-maven-3.9.9\bin\mvn.cmd' -q -Dtest=FakturPembelianFormalDialogTest test
```

Expected: 4 test lulus.

- [x] **Step 4: Cek headless-safe**

Pastikan test tidak memanggil konstruktor `JDialog`; hanya static helper.
Jika muncul `HeadlessException`, pindahkan logika ke helper static (bukan membuat
dialog di test).

- [x] **Step 5: Update ledger Task 4**

---

### Task 5: Tab riwayat di `FormPembelian` + bug fix (TDD)

**Files:**
- Create: `src/test/java/view/FormPembelianHistoryTest.java`
- Modify: `src/main/java/view/FormPembelian.java`

**Interfaces:**
- Produces static helper (headless-safe, dipakai test):
  - `static List<LapPembelian> filterRiwayat(List<LapPembelian> data, String keyword)`
  - `static Object[] riwayatRow(LapPembelian lap)`
- Produces public `void loadRiwayat()` untuk probe/E2E.
- Field baru: `JTabbedPane tabbedPane`, `DatePicker dpDari/dpSampai`,
  `JTextField txtCariRiwayat`, `JTable tblRiwayat`, `DefaultTableModel modelRiwayat`,
  `JLabel lblRiwayatSummary`, `List<LapPembelian> cacheRiwayat`.
- Nama komponen untuk probe: `tabPembelian`, `tglDariPembelian`,
  `tglSampaiPembelian`, `cari_riwayat_pembelian`, `tblRiwayatPembelian`,
  `btnBukaFaktur`, `btnSegarkanRiwayat`, `lblFakturDraft`.
- Tidak mengubah `buildKiri`, `buildKanan`, `tambahItem`, `hapusItem`, `doResetKeranjang`,
  `refreshItem`, atau logika `doSimpan` selain menambah refresh riwayat.

- [x] **Step 1 (RED): Tulis unit test filter & row**

Buat `src/test/java/view/FormPembelianHistoryTest.java`:

```java
package view;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import model.LapPembelian;
import org.junit.jupiter.api.Test;

class FormPembelianHistoryTest {

    private LapPembelian lap(String no, String supplier, String kasir, double total) {
        return new LapPembelian(no, LocalDateTime.of(2026, 11, 11, 9, 30),
                supplier, kasir, total);
    }

    @Test
    void filterRiwayatMencocokkanNoFakturDanSupplierTanpaBedaHurufBesar() {
        List<LapPembelian> data = Arrays.asList(
                lap("FB-20261111-0001", "PT Alpha", "Admin", 10000),
                lap("FB-20261110-0002", "CV Beta", "Kasir", 20000));

        assertEquals(1, FormPembelian.filterRiwayat(data, "fb-20261111").size());
        assertEquals("FB-20261111-0001",
                FormPembelian.filterRiwayat(data, "fb-20261111").get(0).getNoFaktur());
        assertEquals(1, FormPembelian.filterRiwayat(data, "beta").size());
        assertEquals(2, FormPembelian.filterRiwayat(data, "  ").size());
        assertEquals(0, FormPembelian.filterRiwayat(data, "tidak-ada").size());
    }

    @Test
    void filterRiwayatAmanUntukListNull() {
        assertTrue(FormPembelian.filterRiwayat(null, "apa").isEmpty());
    }

    @Test
    void riwayatRowMenghasilkanLimaKolomTerformat() {
        Object[] row = FormPembelian.riwayatRow(
                lap("FB-20261111-0001", "PT Alpha", "Admin Almira", 1500000));

        assertArrayEquals(new Object[]{
                "FB-20261111-0001",
                "11-11-2026 09:30",
                "PT Alpha",
                "Admin Almira",
                "Rp 1.500.000"
        }, row);
    }
}
```

Run:

```powershell
& 'C:\Users\alvin\tools\apache-maven-3.9.9\bin\mvn.cmd' -q -Dtest=FormPembelianHistoryTest test
```

Expected: FAIL — method `filterRiwayat`/`riwayatRow` belum ada.

- [x] **Step 2 (GREEN): Refactor constructor ke tab**

Ganti isi konstruktor setelah `pnlNorth` dibuat:

```java
add(pnlNorth, BorderLayout.NORTH);

tabbedPane = new JTabbedPane();
tabbedPane.setName("tabPembelian");
tabbedPane.setFont(NeoBrutalTheme.display(12));
tabbedPane.setBackground(NeoBrutalTheme.BG);
tabbedPane.setForeground(NeoBrutalTheme.INK);
tabbedPane.addTab("Input Pembelian",
        FontIcon.of(MaterialDesignC.CART_ARROW_DOWN, 16, NeoBrutalTheme.INK),
        buildTabInput());
tabbedPane.addTab("Riwayat Faktur",
        FontIcon.of(MaterialDesignH.HISTORY, 16, NeoBrutalTheme.INK),
        buildTabRiwayat());
tabbedPane.addChangeListener(e -> {
    boolean input = tabbedPane.getSelectedIndex() == 0;
    lblFakturDraft.setVisible(input);
    if (!input) loadRiwayat();
});
add(tabbedPane, BorderLayout.CENTER);

reloadSupplier();
cariBuku("");
updateDraftFaktur();
loadRiwayat();
```

Beri `lblFakturDraft.setName("lblFakturDraft")`.

Tambahkan:

```java
private JPanel buildTabInput() {
    JPanel pnl = new JPanel(new GridLayout(1, 2, 12, 0));
    pnl.setBackground(NeoBrutalTheme.BG);
    pnl.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
    pnl.add(buildKiri());
    pnl.add(buildKanan());
    return pnl;
}
```

- [x] **Step 3 (GREEN): Buat `buildTabRiwayat()`**

Struktur:

```java
private JPanel buildTabRiwayat() {
    JPanel pnl = new JPanel(new BorderLayout(10, 10));
    pnl.setBackground(NeoBrutalTheme.BG);
    pnl.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

    JPanel card = NeoFormStyle.card(new BorderLayout(8, 8));

    JPanel pnlHeader = new JPanel(new BorderLayout(6, 6));
    pnlHeader.setOpaque(false);
    pnlHeader.add(NeoFormStyle.section("Riwayat Faktur Pembelian"), BorderLayout.NORTH);

    JPanel pnlFilter = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
    pnlFilter.setOpaque(false);
    pnlFilter.add(NeoFormStyle.label("Dari"));
    dpDari = new DatePicker();
    dpDari.setName("tglDariPembelian");
    dpDari.setDate(LocalDate.now().minusDays(30));
    dpDari.addDateChangeListener(e -> loadRiwayat());
    pnlFilter.add(dpDari);

    pnlFilter.add(NeoFormStyle.label("Sampai"));
    dpSampai = new DatePicker();
    dpSampai.setName("tglSampaiPembelian");
    dpSampai.setDateToToday();
    dpSampai.addDateChangeListener(e -> loadRiwayat());
    pnlFilter.add(dpSampai);

    pnlFilter.add(NeoFormStyle.label("Cari"));
    txtCariRiwayat = NeoFormStyle.field("cari_riwayat_pembelian");
    txtCariRiwayat.setColumns(18);
    txtCariRiwayat.getDocument().addDocumentListener(new DocumentListener() {
        @Override public void insertUpdate(DocumentEvent e) { applyFilterRiwayat(); }
        @Override public void removeUpdate(DocumentEvent e) { applyFilterRiwayat(); }
        @Override public void changedUpdate(DocumentEvent e) { applyFilterRiwayat(); }
    });
    pnlFilter.add(txtCariRiwayat);
    pnlHeader.add(pnlFilter, BorderLayout.SOUTH);
    card.add(pnlHeader, BorderLayout.NORTH);

    modelRiwayat = new DefaultTableModel(
            new String[]{"No. Faktur", "Tanggal", "Supplier", "Penerima", "Total"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    tblRiwayat = new JTable(modelRiwayat);
    tblRiwayat.setName("tblRiwayatPembelian");
    tblRiwayat.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    NeoTableStyle.apply(tblRiwayat);
    tblRiwayat.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
    tblRiwayat.addMouseListener(new MouseAdapter() {
        @Override public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 2) bukaFaktur();
        }
    });
    tblRiwayat.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "bukaFaktur");
    tblRiwayat.getActionMap().put("bukaFaktur", new AbstractAction() {
        @Override public void actionPerformed(ActionEvent e) { bukaFaktur(); }
    });
    card.add(NeoTableStyle.wrap(tblRiwayat), BorderLayout.CENTER);

    JPanel pnlBottom = new JPanel(new BorderLayout(8, 0));
    pnlBottom.setOpaque(false);
    lblRiwayatSummary = new JLabel("Memuat riwayat faktur...");
    lblRiwayatSummary.setFont(NeoBrutalTheme.body(Font.PLAIN, 11));
    lblRiwayatSummary.setForeground(NeoBrutalTheme.TEXT_MUTED);
    pnlBottom.add(lblRiwayatSummary, BorderLayout.WEST);

    JPanel pnlBtn = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
    pnlBtn.setOpaque(false);
    JButton btnSegarkan = styledButton("Segarkan",
            FontIcon.of(MaterialDesignR.REFRESH, 16, NeoBrutalTheme.INK),
            NeoBrutalTheme.SECONDARY);
    btnSegarkan.setName("btnSegarkanRiwayat");
    btnSegarkan.addActionListener(e -> loadRiwayat());
    JButton btnBuka = styledButton("Buka Faktur",
            FontIcon.of(MaterialDesignF.FILE_DOCUMENT, 16, NeoBrutalTheme.INK),
            NeoBrutalTheme.PRIMARY);
    btnBuka.setName("btnBukaFaktur");
    btnBuka.addActionListener(e -> bukaFaktur());
    pnlBtn.add(btnSegarkan);
    pnlBtn.add(btnBuka);
    pnlBottom.add(pnlBtn, BorderLayout.EAST);
    card.add(pnlBottom, BorderLayout.SOUTH);

    pnl.add(card, BorderLayout.CENTER);
    return pnl;
}
```

Import tambahan yang dibutuhkan:

```java
import com.github.lgooddatepicker.components.DatePicker;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import model.LapPembelian;
import org.kordamp.ikonli.materialdesign2.MaterialDesignF;
import org.kordamp.ikonli.materialdesign2.MaterialDesignH;
import org.kordamp.ikonli.materialdesign2.MaterialDesignR;
```

Catatan: `MaterialDesignF.FILE_DOCUMENT` harus ada di pack ikon; jika tidak,
pakai `MaterialDesignF.FILE` atau `MaterialDesignP.PRINTER`. Cek compile.

- [x] **Step 4 (GREEN): Logika load/filter/buka**

Tambahkan:

```java
private static final DateTimeFormatter FMT_TGL_RIWAYAT =
        DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

static List<LapPembelian> filterRiwayat(List<LapPembelian> data, String keyword) {
    List<LapPembelian> hasil = new ArrayList<>();
    if (data == null) return hasil;
    String key = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
    for (LapPembelian l : data) {
        if (l == null) continue;
        boolean cocok = key.isEmpty()
                || (l.getNoFaktur() != null
                    && l.getNoFaktur().toLowerCase(Locale.ROOT).contains(key))
                || (l.getSupplier() != null
                    && l.getSupplier().toLowerCase(Locale.ROOT).contains(key));
        if (cocok) hasil.add(l);
    }
    return hasil;
}

static Object[] riwayatRow(LapPembelian l) {
    String tgl = l.getTanggal() != null ? l.getTanggal().format(FMT_TGL_RIWAYAT) : "-";
    return new Object[]{
        l.getNoFaktur() != null ? l.getNoFaktur() : "-",
        tgl,
        l.getSupplier() != null ? l.getSupplier() : "-",
        l.getKasir() != null ? l.getKasir() : "-",
        "Rp " + FMT_RP.format((long) Math.round(l.getTotal()))
    };
}

public void loadRiwayat() {
    if (dpDari == null || dpSampai == null || modelRiwayat == null) return;
    LocalDate a = dpDari.getDate();
    LocalDate b = dpSampai.getDate();
    if (a == null || b == null) return;
    try {
        cacheRiwayat = pembelianDAO.lapPembelian(a, b);
        cacheRiwayat.sort(Comparator.comparing(
                LapPembelian::getTanggal,
                Comparator.nullsLast(Comparator.naturalOrder())).reversed());
        applyFilterRiwayat();
    } catch (Exception ex) {
        JOptionPane.showMessageDialog(this,
                "Gagal memuat riwayat faktur: " + ex.getMessage(),
                "Gagal", JOptionPane.ERROR_MESSAGE);
    }
}

private void applyFilterRiwayat() {
    if (modelRiwayat == null) return;
    List<LapPembelian> tampil = filterRiwayat(cacheRiwayat,
            txtCariRiwayat != null ? txtCariRiwayat.getText() : "");
    modelRiwayat.setRowCount(0);
    double total = 0;
    for (LapPembelian l : tampil) {
        modelRiwayat.addRow(riwayatRow(l));
        total += l.getTotal();
    }
    lblRiwayatSummary.setText(tampil.size() + " faktur | Total "
            + formatRp(total));
}

private void bukaFaktur() {
    int row = tblRiwayat.getSelectedRow();
    if (row < 0 || row >= modelRiwayat.getRowCount()) {
        JOptionPane.showMessageDialog(this, "Pilih baris faktur dulu",
                "Validasi", JOptionPane.WARNING_MESSAGE);
        return;
    }
    String noFaktur = String.valueOf(modelRiwayat.getValueAt(row, 0));
    String penerima = String.valueOf(modelRiwayat.getValueAt(row, 3));
    try {
        Pembelian h = null;
        for (Pembelian p : pembelianDAO.search(noFaktur)) {
            if (p.getNoFaktur() != null
                    && p.getNoFaktur().equalsIgnoreCase(noFaktur)) {
                h = pembelianDAO.getById(p.getIdPembelian());
                break;
            }
        }
        if (h == null) {
            JOptionPane.showMessageDialog(this,
                    "Faktur " + noFaktur + " tidak ditemukan",
                    "Tidak Ditemukan", JOptionPane.ERROR_MESSAGE);
            return;
        }
        List<DetailPembelian> detail = pembelianDAO
                .getDetailByPembelian(h.getIdPembelian());
        Supplier supplier = supplierDAO.getById(h.getIdSupplier());
        Frame owner = (Frame) SwingUtilities.getWindowAncestor(this);
        new FakturPembelianFormalDialog(owner, h, detail, supplier, penerima)
                .setVisible(true);
    } catch (Exception ex) {
        JOptionPane.showMessageDialog(this,
                "Gagal buka faktur: " + ex.getMessage(),
                "Gagal", JOptionPane.ERROR_MESSAGE);
    }
}
```

- [x] **Step 5: Refresh riwayat setelah simpan**

Di `doSimpan`, setelah `updateDraftFaktur();` (baris terakhir blok try), tambahkan:

```java
loadRiwayat();
```

Jangan ubah urutan dialog pasca-simpan, `items.clear()`, `refreshItem()`,
`cariBuku(...)`, atau `updateDraftFaktur()`.

- [x] **Step 6: Perbaiki mojibake**

Ganti baris 95:

```java
"Pilih supplier → isi qty & harga beli → tambah → simpan faktur.", pnlDraft),
```

Ganti baris 291:

```java
lblSupplierInfo.setText("<html><b>Alamat:</b> " + almt
        + " &nbsp;|&nbsp; <b>Telp:</b> " + telp + "</html>");
```

- [x] **Step 7: Perbaiki header tabel kanan**

Di `buildKanan`, ubah header model dan lebar kolom:

```java
new String[]{"Kode", "Judul", "Jenis", "Stok Awal", "Qty Beli",
             "Stok Akhir", "Harga", "Subtotal"}
```

```java
int[] lebar = {50, 110, 48, 72, 66, 78, 64, 64}; // total 552
```

Tetap `AUTO_RESIZE_OFF`. Alasan: lebar minimum per header dihitung dari font mono 10
+ padding 16px; `Stok Akhir` butuh 78px, `Stok Awal` 72px, `Qty Beli` 66px.
Jangan mengecilkan di bawah nilai ini.

- [x] **Step 8 (GREEN): Jalankan unit test**

```powershell
& 'C:\Users\alvin\tools\apache-maven-3.9.9\bin\mvn.cmd' -q -Dtest=FormPembelianHistoryTest,FakturPembelianFormalDialogTest test
```

Expected: 7 test lulus (3 + 4).

- [x] **Step 9: Cek mojibake**

```powershell
Select-String -Path src\main\java\view\FormPembelian.java -Pattern 'â|ðŸ|Ã|Â'
```

Expected: tidak ada match.

- [x] **Step 10: Update ledger Task 5**

---

### Task 6: E2E probe, PDF, simpan/stok, screenshot

**Files:**
- Create (scratch, tidak commit):
  `C:\Users\alvin\AppData\Local\Temp\opencode\ProbeR8Pembelian.java`
- Create (scratch): screenshot `...\r8-shots\pembelian-riwayat.png`,
  `pembelian-faktur-formal.png`.
- Read: `database/verify_seed_almira.sql`.

**Interfaces:**
- Consumes: `FormPembelian`, `FakturPembelianFormalDialog`, `ReportHelper`,
  `PembelianDAO`, `SupplierDAO`, `Koneksi`, `Sesi`.
- Produces: `PROBE-R8-PEMBELIAN-OK`, PDF valid, screenshot, DB bersih.

- [x] **Step 1: Build classpath probe**

```powershell
& 'C:\Users\alvin\tools\apache-maven-3.9.9\bin\mvn.cmd' -q -DskipTests package
& 'C:\Users\alvin\tools\apache-maven-3.9.9\bin\mvn.cmd' -q dependency:build-classpath "-Dmdep.outputFile=$env:TEMP\opencode\r8-cp.txt"
```

Expected: JAR + `r8-cp.txt` terbentuk.

- [x] **Step 2: Tulis probe**

Probe harus melakukan, berurutan:

1. `NeoBrutalTheme.apply()`; set `Sesi.userLogin = new User(1, "admin", "",
   "Administrator", "admin")`; `Sesi.role = "admin"`.
2. Buka `MenuUtama`, klik `btnPembelian`, tunggu 800 ms.
3. Cari `JTabbedPane` bernama `tabPembelian`; assert:
   - `getTabCount() == 2`;
   - judul tab 0 `Input Pembelian`, tab 1 `Riwayat Faktur`.
4. Pilih tab 1; tunggu; cari `JTable` bernama `tblRiwayatPembelian`; assert:
   - 5 kolom; header persis `No. Faktur`, `Tanggal`, `Supplier`, `Penerima`, `Total`;
   - `getRowCount() > 0` (seed 36 pembelian, periode 30 hari);
   - `lblFakturDraft` (`getName()` sama) `isVisible() == false`.
5. Screenshot window penuh ke `r8-shots\pembelian-riwayat.png` via `Robot`.
6. Pilih baris 0; baca `noFaktur` dan `penerima`; query header + detail via DAO;
   assert detail tidak kosong dan `subtotal` sama dengan `qty * hargaBeli`.
7. Buka `FakturPembelianFormalDialog`:
   - buat `JFrame` owner dummy;
   - `new FakturPembelianFormalDialog(owner, h, detail, supplier, penerima)`;
   - jadwalkan `javax.swing.Timer(1500, e -> { Robot capture dialog bounds ->
     `pembelian-faktur-formal.png`; dialog.dispose(); })` sekali;
   - `dialog.setVisible(true)` dari EDT.
   Assert file screenshot > 10 KB.
8. Export PDF nyata:
   ```java
   Map<String,Object> params = FakturPembelianFormalDialog
           .buildReportParams(h, supplier, detail, penerima);
   File pdf = new File(shotDir, "faktur-e2e.pdf");
   ReportHelper.exportPdf(FakturPembelianFormalDialog.TEMPLATE, detail, params, pdf);
   ```
   Assert `pdf.length() > 2000`; baca 4 byte pertama = `%PDF`.
9. Uji alur simpan + stok + refresh riwayat (dengan cleanup `finally`):
   - Kembali ke tab 0.
   - Catat `max(id_pembelian)`, `COUNT(*) pembelian`, `stok` buku pada baris
     katalog pertama.
   - Pilih supplier index 1 pada `JComboBox` pertama.
   - Pilih baris 0 pada `JTable` katalog (model 4 kolom: Kode, Judul, Harga Beli,
     Stok Toko). Pastikan `txtHargaBeli` terisi oleh listener.
   - Set `txtQty` = `1`.
   - Klik `JButton` teks `Tambah ke Faktur`; assert tabel item (model 8 kolom)
     `getRowCount() == 1`.
   - Jadwalkan `Timer(1500)` yang mencari `FakturPembelianDialog` di
     `Window.getWindows()` lalu `dispose()`.
   - Klik `JButton` teks `Simpan`. Setelah modal tertutup, tunggu 1000 ms.
   - Query DB:
     - `id_pembelian` baru > baseline;
     - `COUNT(detail_pembelian)` untuk id baru = 1;
     - `stok` buku = baseline + 1;
     - `pembelian.no_faktur` baru muncul di `lapPembelian` 30 hari dan di tabel
       riwayat setelah pilih tab 1 + `loadRiwayat()`.
   - **Cleanup di `finally`** (JDBC transaction):
     ```sql
     DELETE FROM detail_pembelian WHERE id_pembelian = ?;
     DELETE FROM pembelian WHERE id_pembelian = ?;
     UPDATE buku SET stok = stok - ? WHERE id_buku = ?;
     ```
     Verifikasi `COUNT(*) pembelian` dan stok kembali ke baseline.
10. Cek method `ReportHelper.print(String, List, Map)` ada via reflection
    (jangan buka dialog printer sungguhan).
11. Print `PROBE-R8-PEMBELIAN-OK` dan `System.exit(0)`.

Catatan: jika klik `Simpan` gagal karena validasi (`JOptionPane` modal), probe harus
mendeteksi `Window.getWindows()` yang berisi `JOptionPane`/`JDialog` tak terduga,
tutup, dan gagal dengan pesan jelas. Jangan biarkan DB kotor: cleanup `finally`
selalu dijalankan untuk id baru bila ada.

- [x] **Step 3: Compile & jalankan probe**

```powershell
$cp = "target\classes;" + (Get-Content "$env:TEMP\opencode\r8-cp.txt" -Raw).Trim()
javac -encoding UTF-8 -cp $cp -d "$env:TEMP\opencode" "$env:TEMP\opencode\ProbeR8Pembelian.java"
java -cp "$env:TEMP\opencode;$cp" ProbeR8Pembelian
```

Expected: `PROBE-R8-PEMBELIAN-OK`; tidak ada exception; PDF > 2 KB; 2 screenshot
terbentuk.

- [x] **Step 4: Baca screenshot secara visual**

Baca:
- `C:\Users\alvin\AppData\Local\Temp\opencode\r8-shots\pembelian-riwayat.png`
- `C:\Users\alvin\AppData\Local\Temp\opencode\r8-shots\pembelian-faktur-formal.png`

Periksa:
- tidak ada teks mojibake/emoji rusak;
- header tabel kanan tidak terpotong;
- tab riwayat menampilkan 5 kolom dan data seed;
- dialog faktur formal jelas berbeda dari struk penjualan (layout dokumen, tabel,
  blok supplier/penerima, tanda tangan);
- tidak ada komponen tumpang tindih/terpotong.

Jika ada masalah visual, perbaiki dan ulangi Task 6 Step 3–4.

- [x] **Step 5: Verifikasi DB kembali bersih**

```powershell
& 'C:\xampp\mysql\bin\mysql.exe' -u root -N -e "SELECT COUNT(*) FROM db_toko_buku.pembelian; SELECT COUNT(*) FROM db_toko_buku.detail_pembelian;"
& 'C:\xampp\mysql\bin\mysql.exe' -u root db_toko_buku < database\verify_seed_almira.sql
```

Expected: jumlah pembelian/detail sama dengan baseline Task 0; semua status
verifikasi seed `OK` (tidak ada pelanggaran).

- [x] **Step 6: Update ledger Task 6**

---

### Task 7: Dokumentasi, verifikasi akhir, commit

**Files:**
- Modify: `TESTING.md`
- Commit: semua file fitur + plan ini.

- [x] **Step 1: Tambah skenario pengujian di `TESTING.md`**

Di bagian skenario Admin, setelah langkah Pembelian lama, tambahkan:

```markdown
6b. Pembelian → tab **Riwayat Faktur** → default 30 hari terakhir menampilkan
    faktur seed. Ubah Dari/Sampai dan cari no. faktur/nama supplier → tabel
    terfilter. Klik 2x / Enter / **Buka Faktur** → dialog **Faktur Pembelian
    formal** (bukan struk) berisi header toko, supplier, penerima, item, total,
    tanda tangan. **Export PDF** → file `Faktur_<no>.pdf` tersimpan dan bisa
    dibuka. **Cetak** → dialog printer muncul. Kembali ke tab Input → simpan
    pembelian baru → stok bertambah dan faktur baru muncul di riwayat.
```

- [x] **Step 2: Full build & test**

```powershell
& 'C:\Users\alvin\tools\apache-maven-3.9.9\bin\mvn.cmd' -q clean package
```

Expected: seluruh test lulus (21 lama + 4 dialog + 3 form + 1 report baru = 29);
shaded JAR terbentuk; BUILD SUCCESS.

- [x] **Step 3: Self-review diff**

```powershell
git status --short
git diff --stat
git diff --check
git diff -- src/main/java/view/StrukDialog.java src/main/java/view/FormRiwayat.java src/main/java/view/FakturPembelianDialog.java
```

Expected:
- `StrukDialog`, `FormRiwayat`, `FakturPembelianDialog` **tidak berubah** (diff kosong).
- `saveWithDetail` tidak berubah.
- Tidak ada whitespace error.
- File baru: `FakturPembelianFormalDialog.java`, `faktur_pembelian.jrxml`,
  2 test baru, plan ini.
- File ubah: `FormPembelian.java`, `PembelianDAO.java`, `PembelianDAOImpl.java`,
  `ReportHelper.java`, `ReportCompilationTest.java`, `TESTING.md`.

- [x] **Step 4: Cek mojibake seluruh area yang disentuh**

```powershell
Select-String -Path src\main\java\view\FormPembelian.java,src\main\java\view\FakturPembelianFormalDialog.java,src\main\resources\reports\faktur_pembelian.jrxml,src\test\java\view\FormPembelianHistoryTest.java,src\test\java\view\FakturPembelianFormalDialogTest.java -Pattern 'â|ðŸ|Ã|Â'
```

Expected: tidak ada match.

- [x] **Step 5: Commit**

```powershell
git add src/main/java/view/FormPembelian.java `
        src/main/java/view/FakturPembelianFormalDialog.java `
        src/main/java/dao/PembelianDAO.java `
        src/main/java/dao/PembelianDAOImpl.java `
        src/main/java/report/ReportHelper.java `
        src/main/resources/reports/faktur_pembelian.jrxml `
        src/test/java/report/ReportCompilationTest.java `
        src/test/java/view/FormPembelianHistoryTest.java `
        src/test/java/view/FakturPembelianFormalDialogTest.java `
        TESTING.md `
        docs/superpowers/plans/2026-11-11-riwayat-faktur-pembelian.md
git commit -m "feat(pembelian): tambah riwayat faktur dan faktur formal"
git status --short
```

Expected: commit sukses; tree bersih; tidak push.

- [x] **Step 6: Update ledger Task 7 + laporan akhir**

Laporan akhir harus memuat:
- ringkasan fitur dan file yang berubah;
- hasil unit test (jumlah lulus) dan `mvn clean package`;
- hasil E2E probe + path screenshot + path PDF;
- bukti DB bersih setelah cleanup;
- commit hash;
- catatan bahwa `StrukDialog`/`FormRiwayat`/`FakturPembelianDialog` tidak disentuh;
- catatan **belum push**.

---

## Self-Review Checklist (isi sebelum commit)

- [x] `FormPembelian` masih bisa dipakai persis seperti sebelumnya di tab input.
- [x] `doSimpan` masih memanggil `saveWithDetail`, dialog pasca-simpan, clear cart,
      refresh katalog, dan `updateDraftFaktur`; hanya ditambah `loadRiwayat()`.
- [x] Draft No. Faktur terlihat di tab input, tersembunyi di tab riwayat.
- [x] Filter tanggal memakai `LocalDate` dari `DatePicker`; default
      `now().minusDays(30)` s/d hari ini.
- [x] Pencarian mencocokkan no. faktur **dan** nama supplier, case-insensitive.
- [x] Tabel riwayat terurut terbaru di atas.
- [x] `bukaFaktur` menangani faktur tidak ditemukan, detail kosong, dan supplier null.
- [x] Dialog formal memakai `Supplier` untuk alamat/telp; fallback ke
      `Pembelian.getNamaSupplier()`.
- [x] Cetak memakai `ReportHelper.print` (Jasper), bukan `JTextArea.print`.
- [x] Export PDF memakai `ReportHelper.exportPdf`, menambahkan `.pdf` bila perlu.
- [x] JRXML tidak memakai scriptlet dan bisa dikompilasi saat data kosong.
- [x] Semua static helper yang diuji tidak menyentuh Swing/DB saat dipanggil.
- [x] Header tabel kanan minimal 64px dan label tidak terpotong.
- [x] Tidak ada emoji/mojibake baru.
- [x] Tidak ada dependency baru.
- [x] Test lama tetap lulus.
- [x] E2E cleanup mengembalikan jumlah pembelian, detail, dan stok ke baseline.

---

## Known Risks & Mitigations

| Risiko | Mitigasi |
|--------|----------|
| `JDialog` modal membuat probe macet | Tutup lewat `javax.swing.Timer` di EDT; `dispose()`; timeout 1,5 detik. |
| `JOptionPane` validasi tak terduga memblokir probe | Isi qty/harga valid, pilih supplier & buku; deteksi window tak terduga dan tutup. |
| Export PDF gagal karena font/param null | Test kompilasi + param lengkap; `isBlankWhenNull`; cek 4 byte `%PDF`. |
| Printer tidak tersedia | Hanya verifikasi method/button; jangan panggil printer fisik di probe. |
| DB kotor setelah E2E | Cleanup JDBC di `finally` + verifikasi count/stok + `verify_seed_almira.sql`. |
| Mojibake kembali saat edit | Simpan UTF-8; jalankan `Select-String` di Task 5/7. |
| Header tetap terpotong | Lebar minimum dihitung dari font mono 10 + padding 16px; screenshot diperiksa. |
| Compaction kehilangan konteks | Plan ini + Status Ledger + `git status`; lanjut dari task unchecked pertama. |

---

## Context Recovery (setelah compaction)

1. Baca file ini dari atas.
2. Cek **Status Ledger**.
3. Jalankan `git status --short` dan `git log -1 --oneline`.
4. Lanjut dari task pertama yang belum `DONE`.
5. Jangan mengulang task yang sudah `DONE` kecuali ada regresi.
6. Jangan push.
