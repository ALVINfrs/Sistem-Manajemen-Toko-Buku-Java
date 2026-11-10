# Report Testing - Sistem Informasi Toko Buku (CV. Almira Jaya Abadi)

Versi app: 1.0.0 * Tanggal testing: 2026-11-10 * DB: MySQL lokal `db_toko_buku`
(skema final 10 tabel + `seed_demo.sql` + `migrasi_ronde4.sql`).

Hasil otomatis (mesin, hari yang sama): **PROBE-R7-FINAL-OK** (splash → login
admin → 12 modul internal → logout → login baru → cleanup `TEST-R7` COUNT 0),
**`mvn test` 20/20** + **`mvn package` BUILD SUCCESS** + **SMOKE-JAR-OK**
(window `CV. Almira Jaya Abadi - Login`, 1080×680, tampil < 15 detik).
Screenshot final Ronde 7: `shell.png`, `dashboard.png`, `master-buku.png`,
`pos.png`, `laporan.png`, `laporan-chart.png`, `login.png`.
File ini = panduan testing manual untuk Gus.

## 1. Akun + Cara Masuk

| Username | Password | Role  | Asal                                             |
|----------|----------|-------|--------------------------------------------------|
| admin    | admin123 | Admin | schema.sql (login perlu hash dari seed_demo.sql) |
| kasir    | kasir123 | Kasir | seed_demo.sql                                    |

Akun admin berasal dari schema.sql, kasir dari seed_demo.sql.
`schema.sql` men-seed admin dengan password plain sedangkan aplikasi
membandingkan hash SHA-256, jadi DB skema-saja tidak bisa login;
import `seed_demo.sql` (normalisasi ke `SHA2('admin123', 256)`) agar
`admin/admin123` bisa login.

Login: jalankan app (`view.Login`), isi username + password, klik **MASUK**
(atau tekan Enter). Salah satu salah -> label merah inline
"Username atau password salah" + field bergetar, tetap di layar Login.

Register: di layar Login klik **DAFTAR AKUN** -> halaman Register penuh
terbuka di window yang sama (curtain wipe). Isi username, nama, password +
konfirmasi -> **BUAT AKUN** -> akun baru otomatis Kasir -> kembali ke Login
dengan info hijau "Akun berhasil dibuat. Silakan masuk." Tombol **KEMBALI**
membatalkan tanpa menyimpan.

## 2. Isi Seed Demo (setelah import seed_demo.sql + migrasi_ronde4.sql)

users 2, kategori 6 (Fiksi, Non-Fiksi, Anak, Pendidikan, Komik, Alat
Tulis), penerbit 3, supplier 3, buku 18 (BK-001..BK-012 + ATK-001..ATK-006:
pulpen, buku tulis, pensil, penghapus, penggaris, spidol),
penjualan 5, pembelian 3, retur 3.
Stok contoh: BK-001 = 38, BK-002 = 19, BK-003 = 149, BK-004 = 40,
ATK-001 = 100, ATK-002 = 40.
Tanpa tabel member (dihapus ronde 4); kolom `buku.jenis` (Buku/ATK) +
`buku.satuan` (pcs/pak/lusin/box/rim, wajib untuk ATK).

## 3. Skenario Admin (login sebagai admin/admin123)

1. Jalankan app: splash 900×560 tampil (logo + wordmark CV. Almira Jaya
   Abadi + ticker berjalan + 5 blok progress + shape drift) dengan tahap
   tampilan → database → data master → ikon → Siap; lalu curtain wipe ke
   atas membuka layar Login.
2. Login -> hasil yang diharapkan: Menu Utama terbuka dengan sidebar maroon
   dan top bar cream; menu aktif Dashboard berwarna teal + border hitam +
   hard shadow. Semua tombol modul tampil (Dashboard, Buku, Kategori,
   Penerbit, Supplier, ATK, User, Penjualan, Pembelian, Retur, Riwayat,
   Laporan, Logout).
3. Tiap master (Buku, Kategori, Penerbit, Supplier, ATK): tambah 1 data
   -> muncul di tabel; ubah -> tersimpan; hapus -> hilang dari tabel.
   ATK wajib isi satuan (pcs/pak/lusin/box/rim); kode unik.
4. POS: Penjualan -> cari "pulpen" (ATK ikut muncul) -> BK-001 qty 2 +
   ATK-002 qty 2 -> diskon 10% + potongan 5000 -> total =
   round(subtotal*90/100)-5000 -> bayar tunai -> Simpan -> nota format
   `NJ-YYYYMMDD-NNNN`; stok keduanya berkurang 2; struk tampil
   "2 pak x ..." dan 2 baris diskon.
   Coba qty melebihi stok -> ditolak ("Stok tidak mencukupi"), stok tetap.
   Coba persen 101 -> ditolak ("Diskon persen 0-100"); nominal melebihi
   sisa-setelah-persen -> ditolak ("Potongan melebihi sisa").
5. Retur: pilih nota POS tadi + BK-001 qty 1 alasan bebas -> Simpan ->
   stok BK-001 bertambah 1.
6. Pembelian: supplier 1 + ATK-001 qty 10 -> Simpan -> stok ATK-001
   bertambah 10.
7. Laporan (periode mencakup 3 hari terakhir): tampilkan ke-7 jenis
   (Data Master Barang, Penjualan, Laba Kotor / Margin, Pendapatan,
   Pembelian, Pengadaan Supplier, Retur Penjualan) -> viewer terbuka
   berisi data; untuk Data Master Barang coba filter Jenis ATK ->
   hanya 6 baris ATK; Export PDF/Excel tiap jenis -> file tersimpan
   dan bisa dibuka (size > 0). Buka-tutup menu Laporan berulang ->
   TIDAK boleh ada popup error.
8. FormUser: tambah user kasir baru -> logout -> login dengan user baru
   (harus bisa, menu terbatas seperti Kasir).
9. Logout -> kembali ke layar Login.

Hapus semua data uji yang dibuat di atas setelah selesai (agar DB kembali
ke isi seed).

## 4. Skenario Kasir (login sebagai kasir/kasir123)

1. Login -> Menu Utama HANYA menampilkan: Dashboard, Penjualan, Retur,
   Riwayat, Laporan, Logout. BUKTI visible/hidden: tombol Buku, Kategori,
   Penerbit, Supplier, ATK, User, Pembelian TIDAK ada.
2. POS: jual BK-003 qty 1 -> nota tersimpan, stok berkurang 1.
3. Retur: retur 1 atas nota tadi -> stok kembali.
4. Laporan -> jenis terkunci "Penjualan" dan "Retur" saja; Penjualan
   hanya menampilkan nota milik kasir ini (nota admin tidak muncul).
5. Register akun kasir baru dari layar login (logout dulu) -> login
   dengan akun baru -> menu sama terbatasnya.

## 5. Catatan Bug yang Diperbaiki

- Border/shadow hitam menutupi tombol (bayangan digambar di dalam batas
  komponen) -> fix: `util/NeoShadowBorder.java` hanya menggambar strip
  inset (shadow tidak lagi menutup tombol). Terverifikasi visual.
- Dialog "Gagal memuat data laporan … modelPreview is null" tiap buka
  menu Laporan -> akar: `JComboBox.setSelectedItem` tetap fire event
  walau nilai sama, sehingga `muatData()` jalan sebelum tabel preview
  dibangun -> fix: guard null di `muatData`/`filterTabelLive`
  (`view/FormLaporan.java`).
- Label "Buku" di konteks campur (Cari Buku, Judul Buku, LAPORAN MASTER
  DATA BUKU) -> rename gaya campur: judul besar "Barang", detail ikut
  kolom Jenis (ronde 5).
- Ticker Login/Register tampak transparan/ghost -> `TickerPanel` turunan
  `JComponent` tidak otomatis mengecat background; fix: isi background
  eksplisit di `paintComponent` sebelum teks (ronde 6).
- Wordmark lama "ALMIRA JAYA ABADI" -> disamakan menjadi
  **"CV. Almira Jaya Abadi"** di splash, Login, Register, curtain, dan ticker
  (ronde 6).

## 6. Skenario UI Auth (Ronde 6)

Jalankan aplikasi (JAR atau `view.Login`), lalu periksa:

### Splash
1. Splash 900×560 muncul: logo toko utuh di kartu cream, wordmark
   **CV. Almira Jaya Abadi**, ticker teal berjalan, lima blok progress,
   dan shape drift halus.
2. Tunggu kelima tahap selesai -> curtain wipe ke atas -> layar Login
   tampil tanpa jeda blank.
3. Matikan MySQL lalu jalankan ulang: status merah
   "Database tidak terhubung..." + tombol **TUTUP** (tanpa retry, tidak hang).

### Login
1. Klik **MASUK** dengan field kosong -> label merah
   "Username dan password wajib diisi" + field bergetar (tanpa popup).
2. Isi username benar + password salah -> label merah
   "Username atau password salah", tetap di Login.
3. Klik ikon mata -> password terlihat; klik lagi -> tersembunyi.
4. Tekan **Enter** di field -> aksi login berjalan (default button).
5. Klik **DAFTAR AKUN** -> transisi curtain ke Register di window yang sama
   (tidak ada window baru/flicker).

### Register
1. Kosongkan username/nama -> "Username dan nama lengkap wajib diisi".
2. Password < 4 karakter -> "Password minimal 4 karakter".
3. Konfirmasi berbeda -> "Password dan konfirmasi tidak sama".
4. Pakai username yang sudah ada -> "Username sudah dipakai".
5. Isi valid -> **BUAT AKUN** -> kembali ke Login + info hijau
   "Akun berhasil dibuat. Silakan masuk."; akun baru berperan Kasir.
6. Klik **KEMBALI** -> kembali ke Login tanpa menyimpan.
7. Ulangi Login↔Register 3× cepat -> semua transisi selesai, tidak stuck.

Catatan: animasi berjalan di EDT dengan durasi pendek; input tetap responsif
dan validasi auth tidak memakai `JOptionPane`.

## 7. Skenario UI Internal (Ronde 7)

### Shell & Navigasi
1. Login admin -> sidebar maroon 240px, kartu brand cream, label section
   **MASTER / TRANSAKSI / LAPORAN** monospace, top bar cream.
2. Menu aktif berwarna teal + border hitam + hard shadow; item lain tetap
   maroon; tombol Logout coral di bawah sidebar.
3. Klik cepat Dashboard -> Buku -> Penjualan -> Laporan -> Dashboard.
   Transisi fade + slide 160ms harus selesai, konten tidak kosong, dan tidak
   ada dialog error.
4. Login kasir -> sidebar hanya berisi Dashboard, Penjualan, Retur, Riwayat,
   Laporan, dan Logout. Menu master + Pembelian harus tidak ada.

### Dashboard
1. Hero **Omzet Hari Ini** maroon dengan aksen kuning tampil paling atas.
2. Tiga KPI (Transaksi Hari Ini, Item Terjual, Stok Menipis) memakai warna
   teal/kuning/coral dan angka terbaca jelas.
3. Dua panel chart (**Omzet 7 Hari**, **Top 5 Buku**) memakai axis hitam tebal,
   bar flat + outline, grid tipis, dan hard shadow. Saat data kosong, panel
   tetap tampil dengan subtitle empty-state (bukan error).
4. Tabel **Stok Menipis** memakai header hitam dan baris cream/putih.
5. Window tetap 1050×700; seluruh isi dashboard muat tanpa card tumpang tindih.

### Form Master & Transaksi
1. Setiap halaman punya header seragam: kicker mono, judul display, subtitle
   (mis. Data Buku, Penjualan (POS), Pembelian, Retur, Riwayat).
2. Card putih + border hitam; field, combo, date picker, dan tombol memakai
   skin shared; tombol memberi feedback tekan.
3. Tabel memakai header hitam, tinggi baris 30px, dan baris selang-seling
   cream/putih. Tabel Buku (9 kolom) dan ATK (8 kolom) menyediakan horizontal
   scroll tanpa memotong kolom.
4. POS: katalog langsung terisi saat form dibuka, pencarian, keranjang,
   diskon/potongan, total, bayar, dan tombol Simpan tetap berfungsi.
5. Dialog struk, bukti retur, faktur pembelian, dan Profil Saya memakai kertas
   putih + border hitam + tombol shared; isi teks dan aksi lama tidak berubah.

### FormLaporan (visual-only)
1. Struktur tetap: filter jenis/periode, 4 KPI, tab **Tabel Data Preview** dan
   **Kurva & Grafik Visual**, tombol Segarkan/Pratinjau PDF/Export PDF/Excel.
2. Visual disamakan dengan halaman lain; chart tab memakai judul display dan
   plot penuh. Periode tanpa data menampilkan tabel/chart kosong, bukan error.
3. Query, filter role, dan proses export tidak berubah pada Ronde 7.

## 8. Batasan yang Diketahui

- Database harus MySQL lokal (`localhost:3306`, user `root`, tanpa
  password, DB `db_toko_buku`); ganti di `koneksi/Koneksi.java` bila beda.
- Laporan butuh data pada periode yang dipilih; periode kosong -> viewer/
  PDF tanpa baris data (bukan error).
- Dibangun untuk Java 11 target (lolos build di toolchain yang tersedia).
