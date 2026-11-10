# Seed Data Realistis Toko Almira Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [x]`) syntax for tracking.

**Goal:** Mengisi `db_toko_buku` dengan data master dan transaksi Toko Almira yang realistis untuk periode 15 Sep–11 Nov 2026, tanpa mengubah kode aplikasi.

**Architecture:** Script generator Python (scratch, tidak di-commit) membangun katalog dan transaksi deterministik, menyimulasikan stok kronologis, lalu menulis satu file SQL idempotent. File SQL mereset tabel, mengisi master, penjualan, pembelian, detail, retur, dan stok akhir. File verifikasi SQL memeriksa konsistensi setelah import.

**Tech Stack:** Python 3.14 (standard library), MySQL/MariaDB XAMPP, Java 11, Maven 3.9.9.

**Spec:** `docs/superpowers/specs/2026-11-10-seed-data-almira-design.md`

## Global Constraints

- Jangan ubah `src/main/java/**`, `src/test/java/**`, `pom.xml`, atau `database/schema.sql`.
- Jangan tambah dependency baru.
- Semua tanggal transaksi harus di 2026-09-15 s/d 2026-11-11 dan bukan hari Minggu.
- Gunakan `C:\xampp\mysql\bin\mysql.exe` dengan user `root`, password kosong.
- Gunakan `C:\Users\alvin\tools\apache-maven-3.9.9\bin\mvn.cmd` untuk Maven.
- Password user di seed memakai `SHA2(..., 256)` (kompatibel dengan `HashUtil`).
- Seed harus deterministik: ulang generate menghasilkan output yang sama.
- File SQL final harus UTF-8, tanpa mojibake.

## Execution Result (2026-11-11)

- `MASTER-OK books=56 atk=24 total=80`
- `SEED-OK sales=785 details=2152 bulk=55 purchases=36 returns=20 omzet=516661500 pembelian=447580000 stok_min=24`
- DB terimport ke `db_toko_buku`; `database/verify_seed_almira.sql` semua status OK.
- `mvn clean package` 21/21 lulus; probe `PROBE-R7-FULLSCREEN-OK`.
- Omzet Rp516.661.500; pembelian Rp447.580.000; refund retur Rp1.680.000;
  pembayaran Tunai 568 / QRIS 141 / Transfer 76.

## Review Focus

- Total nota harus sama dengan jumlah detail dikurangi diskon; pembulatan diskon 5% sering bikin selisih.
- Stok tidak boleh negatif setelah penjualan borongan qty puluhan.
- Retur tidak boleh melebihi qty terjual pada nota dan barang yang sama.
- Tidak boleh ada transaksi hari Minggu atau di luar rentang periode.
- Nomor nota/faktur/retur harus unik dan mengikuti format aplikasi.
- Judul produk berisi karakter non-ASCII (mis. “Negeri 5 Menara”) harus tersimpan UTF-8.

---

### Task 1: Generator master data & katalog 80 produk

**Files:**
- Create (scratch): `C:\Users\alvin\AppData\Local\Temp\opencode\generate_seed_almira.py`
- Create (nanti): `database/seed_almira_real.sql`

**Interfaces:**
- Produces: konstanta `PENERBIT`, `SUPPLIER`, `KATEGORI`, `BUKU`, `ATK`; class/dict `Product` dengan field `id, kode, judul, penulis, id_penerbit, id_kategori, harga_beli, harga_jual, stok_awal, jenis, satuan`.
- Produces: CLI `python generate_seed_almira.py --check-master`.

- [x] **Step 1: Tulis generator katalog**

Buat script Python dengan `random.Random(20260915)`, 11 penerbit, 6 supplier, 9 kategori, 56 buku, dan 24 ATK sesuai spec. ATK wajib punya `satuan`, `id_penerbit=None`, `id_kategori` = Alat Tulis. Harga jual harus lebih besar dari harga beli dan kelipatan 500/1000.

- [x] **Step 2: Tambah assertion master**

Assertion yang harus gagal keras bila salah:
- `len(BUKU) == 56`, `len(ATK) == 24`.
- Semua `kode` unik.
- Semua `id_penerbit`/`id_kategori` valid.
- Semua ATK punya `satuan` dan `id_penerbit is None`.
- Margin `harga_jual` 10–60% dari `harga_beli`.
- Harga jual dan beli > 0.

- [x] **Step 3: Jalankan cek master**

Run:
```
python C:\Users\alvin\AppData\Local\Temp\opencode\generate_seed_almira.py --check-master
```
Expected: `MASTER-OK books=56 atk=24 total=80`.

- [x] **Step 4: Perbaiki sampai lulus**

Jika assertion gagal, perbaiki data katalog. Tidak ada commit di task ini karena SQL final belum ditulis.

---

### Task 2: Generator penjualan perorangan + borongan sekolah

**Files:**
- Modify (scratch): `C:\Users\alvin\AppData\Local\Temp\opencode\generate_seed_almira.py`

**Interfaces:**
- Consumes: `Product` dan katalog dari Task 1.
- Produces: `build_calendar() -> list[date]` berisi 50 hari operasional.
- Produces: `build_sales(products, calendar) -> list[Sale]`; `Sale` punya `no_nota, tanggal, id_user, items, subtotal, diskon, total, bayar, kembalian, metode`.
- Produces: CLI `--check-sales`.

- [x] **Step 1: Buat kalender operasional**

Generate tanggal 2026-09-15 s/d 2026-11-11, buang hari Minggu. Assertion: 50 hari, tidak ada Sunday, semua dalam rentang.

- [x] **Step 2: Generate penjualan perorangan**

Untuk setiap hari operasional:
- Senin–Jumat: 10–16 nota perorangan.
- Sabtu: 12–18 nota perorangan.
- Pilih 1–4 produk unik; qty 1–3 (ATK boleh 1–5).
- Distribusi kategori: ATK 35%, novel 15%, komik 10%, buku pelajaran 30%, LKS 7%, UTBK 3%.
- Jam acak 08.15–19.45.

- [x] **Step 3: Generate penjualan borongan sekolah**

- Senin–Jumat: 1–2 nota borongan; Sabtu: 0–1.
- Pilih 3–6 judul LKS/buku paket; qty 20–45 per judul.
- Diskon 5% untuk ±50% nota borongan; selain itu 0.
- Target total 40–60 nota borongan.

- [x] **Step 4: Hitung total, bayar, kembalian**

- `subtotal = sum(qty * harga_jual)`.
- `diskon` dibulatkan ke kelipatan 500.
- `total = subtotal - diskon`.
- Tunai 70%, QRIS 20%, Transfer 10%.
- Tunai: `bayar = ceil(total / 5000) * 5000`, boleh sesekali 10.000/20.000; `kembalian = bayar - total`.
- QRIS/Transfer: `bayar = total`, `kembalian = 0`.
- `no_nota = NJ-YYYYMMDD-####` dengan urutan per hari.
- id_user: admin ±20%, kasir ±80%.

- [x] **Step 5: Tambah assertion penjualan**

Assertion:
- 650–800 nota, 2.000+ detail.
- 40–60 nota borongan (total >= Rp1.000.000).
- Semua `total == subtotal - diskon`.
- Semua detail `subtotal == qty * harga_jual`.
- Metode hanya Tunai/QRIS/Transfer.
- Tunai `bayar >= total` dan `kembalian == bayar - total`.
- Non-tunai `bayar == total` dan `kembalian == 0`.
- Semua tanggal bukan Minggu dan dalam rentang.
- Semua `no_nota` unik.

- [x] **Step 6: Jalankan cek penjualan**

Run:
```
python C:\Users\alvin\AppData\Local\Temp\opencode\generate_seed_almira.py --check-sales
```
Expected: `SALES-OK sales=<650-800> details=<2000+> bulk=<40-60> omzet=<positif>`.

---

### Task 3: Simulasi stok, pembelian, retur, dan emitter SQL

**Files:**
- Modify (scratch): `C:\Users\alvin\AppData\Local\Temp\opencode\generate_seed_almira.py`
- Create: `database/seed_almira_real.sql`

**Interfaces:**
- Consumes: `Sale` dan katalog dari Task 2.
- Produces: `Purchase` dengan `no_faktur, tanggal, id_supplier, id_user, items, total`.
- Produces: `Retur` dengan `no_retur, tanggal, id_penjualan, id_buku, qty, alasan`.
- Produces: `write_sql(path)` yang menulis file SQL final.
- Produces: CLI `--check-all` dan `--write`.

- [x] **Step 1: Simulasi stok kronologis**

Urutkan semua penjualan per tanggal. Mulai dari `stok_awal`:
- Buku: stok awal 60–120; reorder point 20; target 80–140.
- ATK: stok awal 100–250; reorder point 30; target 150–250.
- Setiap pagi, cek produk di bawah reorder point; buat reorder ke supplier yang sesuai.
- Gabungkan reorder per supplier menjadi satu faktur pembelian per supplier per hari.
- Sebelum penjualan, pastikan stok cukup; kurangi stok sesuai qty.
- Retur menambah stok kembali.
- Assertion: stok tidak pernah negatif; stok akhir >= 5.

- [x] **Step 2: Generate pembelian**

- 15–40 faktur, tanggal Senin–Sabtu, jam 09.00–11.00.
- Supplier ditentukan dari penerbit (buku) atau supplier ATK.
- 4–10 judul per faktur, qty mengikuti kebutuhan reorder.
- `harga_beli` = harga beli produk; `total = sum(qty * harga_beli)`.
- `no_faktur = FB-YYYYMMDD-####`; id_user = admin.
- Assertion: total benar, tidak ada hari Minggu, nomor unik.

- [x] **Step 3: Generate retur penjualan**

- Pilih 2–3% nota penjualan (target 15–25 retur).
- Pilih satu detail dengan qty >= 2; qty retur 1–3 tetapi <= qty terjual.
- Tanggal retur = tanggal jual + 1–5 hari, skip Minggu, maksimal 2026-11-11.
- Alasan dari daftar: `Salah beli`, `Buku rusak`, `Tidak sesuai pesanan`, `Kelebihan kirim`, `Halaman cacat`.
- `no_retur = RT-YYYYMMDD-####`.
- Assertion: total retur per nota+barang <= qty terjual; tidak ada hari Minggu.

- [x] **Step 4: Hitung stok akhir & tulis SQL**

- `stok_akhir = stok_awal + total_pembelian - total_penjualan + total_retur`.
- Tulis `database/seed_almira_real.sql`:
  1. Header + `SET NAMES utf8mb4`.
  2. `SET FOREIGN_KEY_CHECKS=0`.
  3. `TRUNCATE` semua tabel transaksi lalu master (urutan aman).
  4. `SET FOREIGN_KEY_CHECKS=1`.
  5. `START TRANSACTION`.
  6. Insert users (hash SHA2), kategori, penerbit, supplier, buku (stok akhir).
  7. Insert penjualan, detail_penjualan, pembelian, detail_pembelian, retur.
  8. `COMMIT`.
- Gunakan ID eksplisit dan escape string dengan benar.

- [x] **Step 5: Jalankan cek penuh & tulis SQL**

Run:
```
python C:\Users\alvin\AppData\Local\Temp\opencode\generate_seed_almira.py --check-all
python C:\Users\alvin\AppData\Local\Temp\opencode\generate_seed_almira.py --write
```
Expected: `SEED-OK sales=<650-800> purchases=<15-40> returns=<15-25> details=<2000+> stok_min>=5`, lalu `SQL-WRITTEN database/seed_almira_real.sql`.

- [x] **Step 6: Cek struktur file SQL**

Run:
```
Get-Content database\seed_almira_real.sql -TotalCount 20
(Select-String -Path database\seed_almira_real.sql -Pattern '^INSERT INTO ').Count
```
Expected: header/TRUNCATE benar; jumlah blok INSERT sesuai tabel yang diisi; file > 100 KB.

---

### Task 4: File verifikasi, backup, dan import database

**Files:**
- Create: `database/verify_seed_almira.sql`
- Read: `database/seed_almira_real.sql`

**Interfaces:**
- Consumes: SQL final dari Task 3.
- Produces: laporan verifikasi dengan semua kolom pelanggaran = 0.

- [x] **Step 1: Tulis query verifikasi**

Isi `verify_seed_almira.sql`:
- Hitung baris per tabel.
- Rentang tanggal penjualan/pembelian/retur.
- `SELECT COUNT(*)` transaksi hari Minggu (harus 0).
- Penjualan yang `total != subtotal - diskon` (harus 0).
- Pembelian yang `total != subtotal` (harus 0).
- Stok negatif (harus 0).
- Retur melebihi qty terjual (harus 0).
- Detail/retur dengan FK yatim (harus 0).
- Jumlah nota borongan (total >= 1.000.000).
- Omzet, total pembelian, total retur.

- [x] **Step 2: Backup database lama**

Run:
```
& 'C:\xampp\mysql\bin\mysqldump.exe' -u root db_toko_buku > "$env:TEMP\opencode\backup_db_toko_buku_pre_seed.sql"
```
Expected: file ada dan ukurannya > 0.

- [x] **Step 3: Import seed**

Run:
```
& 'C:\xampp\mysql\bin\mysql.exe' -u root db_toko_buku < database\seed_almira_real.sql
```
Expected: tidak ada error; exit code 0.

- [x] **Step 4: Jalankan verifikasi**

Run:
```
& 'C:\xampp\mysql\bin\mysql.exe' -u root db_toko_buku < database\verify_seed_almira.sql
```
Expected:
- users 2, kategori 9, penerbit 11, supplier 6, buku 80 (56 Buku + 24 ATK).
- penjualan 650–800, detail_penjualan 2000+.
- pembelian 15–40, retur 15–25.
- semua query pelanggaran = 0.
- omzet dan total pembelian positif.

- [x] **Step 5: Spot-check data**

Run query:
```
SELECT b.jenis, COUNT(*) FROM buku b GROUP BY b.jenis;
SELECT DATE(p.tanggal) tgl, COUNT(*) nota, SUM(p.total) omzet FROM penjualan p GROUP BY DATE(p.tanggal) ORDER BY tgl LIMIT 10;
SELECT b.judul, SUM(d.qty) qty FROM detail_penjualan d JOIN buku b ON b.id_buku=d.id_buku GROUP BY b.id_buku ORDER BY qty DESC LIMIT 10;
```
Expected: distribusi masuk akal; tidak ada tanggal Minggu; top produk didominasi LKS/buku paket/ATK.

---

### Task 5: Smoke test aplikasi, dokumentasi, dan commit

**Files:**
- Modify: `README.md`
- Modify: `TESTING.md`
- Commit: `database/seed_almira_real.sql`, `database/verify_seed_almira.sql`, `README.md`, `TESTING.md`, `docs/superpowers/specs/2026-11-10-seed-data-almira-design.md`, `docs/superpowers/plans/2026-11-10-seed-data-almira-real.md`

**Interfaces:**
- Consumes: database hasil Task 4.
- Produces: bukti aplikasi menampilkan data + dokumentasi import.

- [x] **Step 1: Build & test Java**

Run:
```
& 'C:\Users\alvin\tools\apache-maven-3.9.9\bin\mvn.cmd' -q clean package
```
Expected: 21/21 test lulus; JAR terbentuk.

- [x] **Step 2: Jalankan probe screenshot**

Run probe `%TEMP%\opencode\ProbeR7Fullscreen.java` dengan classpath `target/classes` + dependency.
Expected: `PROBE-R7-FULLSCREEN-OK`; screenshot Dashboard dan Laporan terisi data, chart kurva punya titik harian.

- [x] **Step 3: Cek visual screenshot**

Baca `full-dashboard.png`, `full-laporan-table.png`, `full-laporan-chart.png`.
Expected:
- Dashboard menampilkan omzet/transaksi hari ini.
- Laporan default 30 hari berisi data.
- Chart kurva tidak kosong dan tidak melar.

- [x] **Step 4: Update dokumentasi**

Tambahkan di `README.md` dan `TESTING.md`:
- Cara import `database/seed_almira_real.sql`.
- Isi seed: 80 produk, periode, jumlah transaksi.
- Akun `admin/admin123`, `kasir/kasir123`.
- Cara menjalankan `database/verify_seed_almira.sql`.

- [x] **Step 5: Commit**

Run:
```
git add database/seed_almira_real.sql database/verify_seed_almira.sql README.md TESTING.md docs/superpowers/specs/2026-11-10-seed-data-almira-design.md docs/superpowers/plans/2026-11-10-seed-data-almira-real.md
git commit -m "feat(db): seed realistic Almira dataset"
```
Expected: commit sukses; `git status --short` bersih.

- [x] **Step 6: Laporkan hasil**

Laporkan jumlah baris, omzet, total pembelian, jumlah retur, hasil test, dan lokasi screenshot. Jangan push kecuali diminta.

