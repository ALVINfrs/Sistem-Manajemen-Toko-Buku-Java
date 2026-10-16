# Report Testing - Sistem Informasi Toko Buku (CV. Almira Jaya Abadi)

Versi app: 1.0.0 * Tanggal testing: 2026-10-16 * DB: MySQL lokal `db_toko_buku`
(skema final 10 tabel + `seed_demo.sql` + `migrasi_ronde4.sql`).

Hasil otomatis (mesin, hari yang sama): **PROBE-T24-OK (32 checks:
POS campur + diskon, retur, pembelian ATK, 7 laporan + filter, dashboard,
splash, DB bersih)** + **`mvn test` 7/7** + **`mvn package` BUILD SUCCESS**
+ **APP-ALIVE-15S:True** (JAR `sistem-toko-buku-1.0.0.jar`).
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

Login: jalankan app (`view.Login`), isi username + password, klik Login.
Salah satu salah -> popup "Username atau password salah", tetap di login.

Register: di layar login klik "Daftar Akun Kasir" -> isi username, nama,
password + konfirmasi -> akun baru otomatis Kasir -> tutup dialog -> login
dengan akun tersebut.

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

1. Jalankan app: splash bertahap tampil (tampilan -> database ->
   data master -> ikon -> Siap) -> layar Login.
2. Login -> hasil yang diharapkan: Menu Utama terbuka, semua tombol
   modul tampil (Buku, Kategori, Penerbit, Supplier, ATK, User,
   Penjualan, Pembelian, Retur, Riwayat, Laporan, Logout).
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

1. Login -> Menu Utama HANYA menampilkan: Penjualan, Retur, Laporan,
   Logout. BUKTI visible/hidden: tombol Buku, Kategori, Penerbit,
   Supplier, ATK, User, Pembelian TIDAK ada.
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

## 6. Batasan yang Diketahui

- Foto login: bila `images/login_bg.jpg` hilang dari classpath, panel kiri
  memakai fallback ikon (bukan error).
- Database harus MySQL lokal (`localhost:3306`, user `root`, tanpa
  password, DB `db_toko_buku`); ganti di `koneksi/Koneksi.java` bila beda.
- Laporan butuh data pada periode yang dipilih; periode kosong -> viewer/
  PDF tanpa baris data (bukan error).
- Dibangun untuk Java 11 target (lolos build di toolchain yang tersedia).
