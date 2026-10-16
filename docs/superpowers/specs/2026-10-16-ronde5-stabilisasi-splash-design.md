# Spec Addendum Ronde 5 — Stabilisasi penamaan + splash startup

Status: disetujui user via chat (1A, 2C, 3A, 4B). Induk: `docs/superpowers/specs/2026-09-23-ronde4-atk-design.md`
(ronde 4 sudah merge di `impl/ronde4-atk`: commit `a560583`).

## 1. Fix error `modelPreview is null` di Laporan (jawaban 1A: repro konsisten tiap klik Laporan)

- Gejala: dialog "Gagal memuat data laporan: Cannot invoke
  `DefaultTableModel.setDataVector` because `this.modelPreview` is null" — dari
  `view/FormLaporan.java:407` (`muatData` catch). Panel lama (Dashboard) tetap tampil
  sampai dialog di-OK.
- Analisis: satu-satunya pemicu adalah `muatData()`/`filterTabelLive()` jalan saat
  `modelPreview`/`tblPreview` belum dibangun (`buildCenterTabs`, baris ~322).
  Pemicu exact belum ketemu (urutan ctor normal aman; probe T19 bersih) — fix menutup
  KELAS defect, bukan satu pemicu: guard null di kedua method + flag `sedangInit`
  selama konstruksi sehingga event listener prematur jadi no-op. Setelah ctor selesai,
  `muatData()` baris ~147 memuat normal — state akhir selalu benar.
- Non-goal: tidak ada perubahan perilaku bila komponen sudah lengkap.

## 2. Rename gaya C (jawaban 2C: judul besar "Barang", detail spesifik)

Aturan: setiap label/judul/kolom yang datanya campur Buku+ATK tidak boleh lagi sebut
"Buku" saja. Konteks murni-buku (FormBuku, sidebar "Buku"/"ATK", seed) TIDAK diubah.

| Lokasi | Lama | Baru |
|---|---|---|
| `FormLaporan.JENIS_BUKU` (combo) | Data Master Buku | Data Master Barang |
| `FormPenjualan` label cari | Cari Buku | Cari Barang |
| `FormPembelian` label cari | Cari Buku di Katalog: | Cari Barang di Katalog: |
| Preview FormLaporan (4 kolom) | Judul Buku | Nama Barang |
| Preview FormLaporan (retur, master) | Kode Buku | Kode |
| KPI strip | TOTAL VOLUME BUKU | TOTAL VOLUME BARANG |
| `lap_data_buku.jrxml` judul | LAPORAN MASTER DATA BUKU | LAPORAN MASTER DATA BARANG |
| `lap_data_buku.jrxml` kolom | Judul Buku | Nama Barang |
| `lap_data_buku.jrxml` summary | TOTAL MASTER BUKU: | TOTAL MASTER BARANG: |
| `lap_pendapatan/laba_kotor.jrxml` kolom | Judul Buku | Nama Barang |
| `lap_retur.jrxml` judul | LAPORAN RETUR PENJUALAN BUKU | LAPORAN RETUR PENJUALAN |
| `lap_retur.jrxml` kolom | Kode Buku / Judul Buku | Kode / Nama Barang |
| `FormRetur` tabel (2 tempat) | Kode Buku / Judul Buku | Kode / Nama Barang |
| Komentar chart FormLaporan | Data Master Buku (Distribusi…) | Data Master Barang (Distribusi…) |

Excel ikut otomatis (export = header tabel). `field` jrxml (`judul`, `kodeBuku`) TIDAK diubah
(bean tak berubah).

## 3. Report tetap gabung (jawaban 3A)

Struktur Task 19 dipertahankan (7 laporan + kolom Jenis + filter Semua/Buku/ATK);
hanya rename §2. Tanpa report baru, tanpa pisah.

## 4. Splash startup detail per tahap (jawaban 4B)

- `view/SplashScreen.java` BARU (JWindow, neobrutalism: `NeoShadowBorder`, surface `#FFFFFF`,
  bg `#FFFBEA`; logo `util.LogoUtil.icon(120)`; `JProgressBar` + `lblTahap` + `lblStatus`).
- Tahap berurutan di `SwingWorker`: (1) "Menyiapkan tampilan…" (jeda visual 300ms;
  `NeoBrutalTheme.apply()` tetap di `Login.main` SEBELUM splash, karena LAF harus
  diterapkan sebelum komponen dibuat); (2) "Menghubungkan database…"
  (`koneksi.Koneksi.getConnection()` try-with-resources + `SELECT 1`);
  (3) "Memuat data master…" (`KategoriDAO.getAll().size()`); (4) "Menyiapkan ikon…"
  (satu `FontIcon.of` dummy); (5) "Siap." → jeda 300ms → dispose + `onDone.run()`.
- DB gagal: `lblStatus` merah "Database tidak terhubung. Hidupkan MySQL lalu ulangi.",
  progress stop, tombol "Tutup" muncul (klik → `System.exit(1)`). Tanpa retry (YAGNI).
- `Login.main`: `NeoBrutalTheme.apply();` tetap → `new SplashScreen(() ->
  SwingUtilities.invokeLater(() -> new Login().setVisible(true)))`.
- Constructor `public SplashScreen(Runnable onDone)`; non-null; timeout internal tidak ada
  (tahap cepat; worker selalu selesai/gagal eksplisit).

## 5. Testing & DoD ronde 5

- Probe per task (prefix TEST-R5, cleanup + COUNT 0): guard NPE (refleksi `modelPreview=null`
  + `muatData` → tanpa dialog), rename (combo + kolom + jrxml fill 7/7), splash
  (`onDone` terpanggil + disposed ≤15 dtk).
- Task 24 (pengganti Task 20 ronde 4 yang belum jalan): E2E POS campur + diskon,
  Pembelian ATK, Retur, dashboard, `mvn package`, `README.md`/`TESTING.md`, review.

## Self-review spec

1. Placeholder: tidak ada (string exact di tabel; API `LogoUtil.icon(int)`,
   `KategoriDAO.getAll()`, `Koneksi.getConnection()` terverifikasi di kode).
2. Konsistensi: bean/field jrxml tak berubah; hanya teks tampil.
3. Scope: 4 task sekuensial (21→24).
