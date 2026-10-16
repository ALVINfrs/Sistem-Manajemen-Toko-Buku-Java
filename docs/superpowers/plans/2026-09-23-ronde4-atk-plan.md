# Ronde 4 ATK + Hapus Member Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Hapus Member total, tambah ATK (kolom jenis+satuan), diskon persen+nominal di POS, rapikan 7 report tersisa.

**Architecture:** Pola ronde 1-3 dipertahankan (view→dao→Koneksi; loader reflektif — FormAtk otomatis ditemukan; hapus file = hapus referensi; filter Jenis di Java, bukan SQL).

**Tech Stack:** Java 11, FlatLaf 3.4, Ikonli 12.3.1, mysql-connector-j 8.4.0, JasperReports 6.21.3, LGoodDatePicker 11.2.1, poi-ooxml, jfreechart 1.0.19.

**Spec:** docs/superpowers/specs/2026-09-23-ronde4-atk-design.md (argues from docs/prd.md + docs/design.md — baca ketiganya).

## Global Constraints

- Java 11, UTF-8. DILARANG tambah dependency.
- `jenis` NILAI TEPAT `Buku|ATK` (default `Buku`). Satuan opsi TEPAT `pcs, pak, lusin, box, rim` (combo); NULL untuk buku (tampil `-`).
- Diskon: `total = (subtotal − persen%) − nominal`; `0<=persen<=100`; `0<=nominal<=sisa`; bebas semua role. `diskon` DB = gabungan rupiah.
- Kasir: tanpa master/User/Pembelian; combo laporan = Penjualan, Retur (Terlaris/Stok hilang).
- Neobrutalism + Ikonli-javap seperti biasa. Probe prefix TEST-R4 + cleanup + assert COUNT 0. DB ber-seed, JANGAN DROP.

---

## File Map ronde 4

```
Modify: database/schema.sql (-member, -id_member, +jenis, +satuan)
Create: database/migrasi_ronde4.sql
Modify: database/seed_demo.sql (-member, penjualan NULL, +kategori Alat Tulis + 6 ATK)
Delete: model/Member.java, dao/MemberDAO.java, dao/MemberDAOImpl.java, view/FormMember.java
Modify: model/Penjualan.java (-idMember/-kodeMember), model/Buku.java (+jenis,+satuan),
        model/LapPenjualan.java (-member), model/LapStok/LapPendapatan/LapLabaKotor (+jenis bila ada)
        dao/PenjualanDAOImpl.java (tanpa id_member; +jenis di bean item),
        dao/BukuDAO.java (+getAllByJenis,+searchByJenis), dao/BukuDAOImpl.java (+jenis,+satuan di map/insert/update)
Create: view/FormAtk.java
Modify: view/MenuUtama.java (Member→ATK), view/FormBuku.java (filter Buku),
        view/FormPenjualan.java (-member +diskon persen +kolom/satuan),
        view/StrukDialog.java (tanpa member? TETAP param, selalu "- (Non-Member)"),
        view/FormPembelian.java (+kolom Jenis), view/FormLaporan.java (-2 jenis +filter Jenis),
        view/FormKategori.java? TIDAK (sudah ada)
Delete: src/main/resources/reports/{lap_terlaris.jrxml, lap_stok.jrxml}
Modify: 7 jrxml sisa (+kolom Jenis di berbasis-buku; -kolom Member di penjualan)
Modify: src/test/.../ReportCompilationTest.java (9→7), README.md, TESTING.md
```

---

### Task 17: Drop Member + diskon persen

**Files:**
- Delete: `model/Member.java`, `dao/MemberDAO.java`, `dao/MemberDAOImpl.java`, `view/FormMember.java`
- Modify: `database/schema.sql`, `database/seed_demo.sql`, `model/Penjualan.java`, `model/LapPenjualan.java`, `dao/PenjualanDAOImpl.java`, `view/MenuUtama.java`, `view/FormPenjualan.java`, `view/StrukDialog.java`

**Interfaces:**
- Consumes: `saveWithDetail` (tetap signature; header tanpa id_member)
- Produces: `Penjualan` tanpa member; POS Stock `hitungTotal(subtotal,persen,nominal)` + `validasiDiskon(persen,nominal,sisa)` (public static agar probe-able, pola `hitungDiskonMember` lama)

- [ ] **Step 1: SQL.** `migrasi_ronde4.sql` (header run-book; idempoten: DROP FK bila ada — MariaDB `ALTER TABLE penjualan DROP FOREIGN KEY IF EXISTS fk_penjualan_member`? Nama FK aktual dicek dulu via `SHOW CREATE TABLE penjualan`; bila nama tak pasti, pola aman: `SET @fk = (SELECT CONSTRAINT_NAME FROM ...)` terlalu rumit — perintah: cek nama FK, tulis DROP eksplisit + catat; bila ragu, dua varian dengan komentar. JANGAN tebak buta) + `ALTER TABLE penjualan DROP COLUMN id_member` + `DROP TABLE IF EXISTS member`. Jalankan ke DB dev. Update `schema.sql` (hapus blok member + kolom id_member). Update `seed_demo.sql` (hapus INSERT member; INSERT penjualan tanpa id_member).

- [ ] **Step 2: Hapus file Member** (4 file di atas via `git rm`).

- [ ] **Step 3: Model+DAO.** `Penjualan`: hapus `idMember/kodeMember` (field+ctor+getter/setter; ctor lama yang dipakai pemanggil — perbarui SEMUA pemanggil: FormPenjualan, FormRiwayat, probe internal, DAO map). `LapPenjualan`: hapus `member` (bean + DAO map + SELECT tanpa JOIN member). `PenjualanDAOImpl`: INSERT/UPDATE/SELECT tanpa `id_member`; `lapPenjualan/ByUser` tanpa member (kasir label ganti `username`? LapPenjualan punya `kasir` — tampilkan kasir saja).

- [ ] **Step 4: POS.** Hapus combo member + `hitungDiskonMember` + konstanta syarat (cek sisa pakai via grep; hapus bila yatim). Tambah field Diskon % (default "0") + label status dihapus. `hitungTotal(subtotal,persen,nominal) = round(subtotal*(100-persen)/100) - nominal`; `validasiDiskon` return null-bila-OK else "Diskon persen 0-100" / "Potongan melebihi sisa". Simpan: `diskon = round(subtotal*persen/100)+nominal`; StrukDialog: diskonMember → nilai persen-computed, potongan → nominal (ctor TETAP 7-arg, arti param disesuaikan: `diskonMember` = rupiah hasil persen). Reset: persen+potongan 0.

- [ ] **Step 5: MenuUtama.** Tombol Member → tombol ATK (ikon verifikasi, FQN `view.FormAtk`, gating Admin-only ikut blok master). NOL perubahan lain.

- [ ] **Step 6: Verifikasi.** compile; probe (member table gone? `SHOW TABLES LIKE 'member'` = 0 rows; POS math: persen 0/50/100/101, nominal pas/lebih; save nota TEST-R3 + SELECT tanpa member; FormMember ClassNotFound DIHARAPKAN (buktikan loader fallback "belum tersedia", bukan crash); MenuUtama instantiate (tombol ATK ada, Member tak ada); cleanup; PROBE-T17-OK.

- [ ] **Step 7: Commit + laporan.** Commit `feat(r4): hapus member, diskon persen+nominal` + laporan ke `.../ronde4-atk-pplan/task-17-report.md` — path BENAR: `E:\Sistem-Informasi-Manajemen-Toko-Buku\.superpowers\sdd\2026-09-23-ronde4-atk-plan\task-17-report.md` + kontrak singkat.

### Task 18: Kolom jenis+satuan + FormAtk + POS campur + seed

**Files:**
- Modify: `database/schema.sql`, `database/seed_demo.sql`, `model/Buku.java`, `dao/BukuDAO.java`, `dao/BukuDAOImpl.java`, `view/FormBuku.java`, `view/FormPenjualan.java`, `view/FormPembelian.java`
- Create: `view/FormAtk.java`

**Interfaces:**
- Consumes: Task 17 (POS tanpa member)
- Produces: `BukuDAO.getAllByJenis(String):List<Buku>`, `searchByJenis(String,String):List<Buku>`; `Buku(jenis,satuan)`; `FormAtk` (JPanel no-arg)

- [ ] **Step 1: SQL.** migrasi tambah: `ALTER TABLE buku ADD COLUMN IF NOT EXISTS jenis ENUM('Buku','ATK') NOT NULL DEFAULT 'Buku'` + `ADD COLUMN IF NOT EXISTS satuan VARCHAR(20) NULL`. schema.sql + posisi (jenis+satuan setelah stok). seed: kategori "Alat Tulis" + 6 ATK (pulpen gel 0.5 / BK-ATK-001? Kode: `ATK-001..ATK-006`; nama: Pulpen Gel Hitam, Buku Tulis Sidu 38, Pensil 2B, Penghapus, Penggaris 30cm, Spidol Whiteboard; harga_beli 2500-15000; jual +40-60%; stok 30-100; satuan pcs/pak/lusin/box; kategori Alat Tulis; penulis+id_penerbit NULL).

- [ ] **Step 2: Model+DAO.** `Buku` + `jenis` (default "Buku" di ctor kosong), +`satuan` (nullable). DAO map/insert/update eksplisit kolom. `getAllByJenis` (`WHERE jenis=? ORDER BY id_buku`), `searchByJenis` (`AND (kode LIKE? OR judul LIKE?)` — ATK tak cari penulis).

- [ ] **Step 3: FormBuku filter.** `loadTable/cari` → `getAllByJenis("Buku")`/`searchByJenis("Buku",k)`. NOL ubah lain.

- [ ] **Step 4: FormAtk** (pola FormBuku): field Kode, Nama Barang, Kategori combo, Satuan combo [pcs,pak,lusin,box,rim] (wajib), Harga Beli/Jual, Stok, Deskripsi opsional. Simpan: `jenis="ATK"`, penulis=null, idPenerbit=null. Tabel kolom: ID, Kode, Nama, Kategori, Satuan, H. Beli, H. Jual, Stok. Unik kode, confirm hapus, search live (`searchByJenis("ATK",k)`), klik-baris edit.

- [ ] **Step 5: POS campur + Pembelian.** POS search → `BukuDAO.search` (semua jenis); tabel hasil + keranjang + kolom Jenis; baris item `"2 pak x 15.000"` (satuan null → tanpa satuan); struk item ikut satuan. Pembelian: search campur + kolom Jenis di tabel item; hint + "berlaku untuk Buku & ATK".

- [ ] **Step 6: Verifikasi.** compile; probe (ATK CRUD + satuan wajib (tolak null), Buku form hanya Buku (insert ATK via DAO → tidak muncul di getAllByJenis Buku), POS campur, beli ATK + stok, cleanup; PROBE-T18-OK.

- [ ] **Step 7: Commit + laporan** (`feat(r4): atk jenis+satuan, form atk, pos campur`).

### Task 19: Report hapus 2 menu + kolom/filter Jenis

**Files:**
- Delete: `lap_terlaris.jrxml`, `lap_stok.jrxml`
- Modify: `view/FormLaporan.java`, 7 jrxml sisa, `src/test/.../ReportCompilationTest.java` (cek path aktual dulu)

**Interfaces:** Consumes: bean item + DAO lap* (tambah `jenis` di bean+map Task ini untuk: LapStok? (dipakai dashboard — TAMBAH field jenis juga, aman), LapPendapatan, LapLabaKotor/LapTerlaris? (Terlaris DAO tetap untuk dashboard — tambah jenis juga), LapDataBuku? (bean = LapStok). Prinsip: SEMUA bean baris-buku dapat `jenis` dari `b.jenis`.

- [ ] **Step 1: Bean+DAO.** Tambah `jenis:String` (+getter/setter) ke bean item-buku yang dipakai laporan; DAO map `jenis=rs.getString("jenis")` (SELECT tambah `b.jenis`).
- [ ] **Step 2: FormLaporan.** Hapus JENIS_TERLARIS + JENIS_STOK (konstanta, combo items admin, mapping data/jrxml/excel/grafik, date-hide rule untuk STOK). Tambah dropdown filter Jenis (Semua/Buku/ATK) visible hanya untuk laporan berbasis-buku (Data Buku, Pendapatan, Laba Kotor); filter list hasil DAO di Java (`jenis.equals`). Kasir combo: Penjualan, Retur (hapus Terlaris).
- [ ] **Step 3: jrxml.** 7 sisa: Data Buku/Pendapatan/Laba Kotor (+kolom Jenis setelah Judul/Kategori — geser kolom kanan? Minimal: tambah kolom Jenis LEBAR 60 dengan mengecilkan kolom Judul 20. Catat geometri per file di laporan). Penjualan: hapus kolom Member → tetap (Metode, Diskon) — CEK: apakah kolom Member masih ada di jrxml? Hapus bila ya, ganti header Diskon bila belum. Pembelian/Supplier/Retur: tanpa jenis (bukan item-buku) — hanya pastikan kompilasi.
- [ ] **Step 4: Test file.** Update ekspektasi 9→7 (baca file test aktual dulu).
- [ ] **Step 5: Verifikasi.** compile + ReportCompilationTest hijau; probe 7 jrxml fill + filter Jenis (Buku-only ⊂ full; ATK-only dari seed) + FormLaporan instantiate; DB utuh; PROBE-T19-OK.
- [ ] **Step 6: Commit + laporan** (`feat(r4): rapikan report, kolom/filter jenis`).

### Task 20: E2E ronde 4 + package + docs + review

**Files:** Modify `README.md`, `TESTING.md`. Probe luar repo, TEST-R4=0.

- [ ] **Step 1: E2E** (skenario lama minus member + baru): ATK CRUD, POS campur (buku+ATK, persen 10 + nominal, validasi batas), beli ATK, retur item ATK, 7 laporan + filter Jenis + Excel 1 jenis, dashboard terlaris/stok tetap, profil, riwayat. `PROBE-R4-OK`.
- [ ] **Step 2: package + jar alive 15s + shaded-jar exportPdf + exportXlsx-dari-jar.**
- [ ] **Step 3: README/TESTING** (tanpa member; ATK; persen+nominal; 7 laporan + filter; akun).
- [ ] **Step 4: Commit + laporan** (`test(r4): e2e + packaging` + docs).

## Self-Review

1. Spec coverage: §1→T17; §2→T17S4; §3→T18; §4→T19; §5→T20. NOL gap.
2. Placeholder: tidak ada; SQL/metode/kolom/nilai konkret; filter-Jenis Java-side eksplisit; LapGrafik reuse (count sudah ada).
3. Type consistency: `getAllByJenis/searchByJenis(String[,String])` konsisten T18; `hitungTotal/validasiDiskon` T17; bean `jenis` T19; StrukDialog ctor TETAP (ubah isi arti param, bukan signature — konsisten T17S4).
