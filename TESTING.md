# Report Testing — Sistem Informasi Toko Buku (CV Almira Jaya Abadi)

Versi app: 1.0.0 · Tanggal testing: 2026-09-23 · DB: MySQL lokal `db_toko_buku`
(skema final 11 tabel + `seed_demo.sql`).

Hasil otomatis (mesin, hari yang sama): **E2E PROBE-T8-OK (61 checks,
a–j, DB bersih)** + **PROBE-JAR-OK (export PDF dari shaded JAR)**.
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
Salah satu salah → popup "Username atau password salah", tetap di login.

Register: di layar login klik "Daftar Akun Kasir" → isi username, nama,
password + konfirmasi → akun baru otomatis Kasir → tutup dialog → login
dengan akun tersebut.

## 2. Isi Seed Demo (setelah import seed_demo.sql)

users 2, kategori 5 (Fiksi, Non-Fiksi, Anak, Pendidikan, Komik),
penerbit 3, supplier 3, member 5 (MBR-001..MBR-005), buku 12
(BK-001..BK-012), penjualan 2 (nota -0001 dua hari lalu oleh admin +
member MBR-001 total 215000; nota -0002 hari ini oleh kasir tanpa member
total 105000), pembelian 1 (faktur -0001 kemarin, supplier 1, total
1050000), retur 1 (atas penjualan -0001, BK-001 qty 1, "Sampul rusak").
Stok contoh: BK-001 = 39, BK-002 = 19, BK-003 = 47, BK-004 = 40.
Laba seed (laporan Pendapatan): 95000.

## 3. Skenario Admin (login sebagai admin/admin123)

1. Login → hasil yang diharapkan: Menu Utama terbuka, semua 11 tombol
   modul tampil, sapaan "Selamat datang, Administrator (Admin)".
2. Tiap master (Buku, Kategori, Penerbit, Supplier, Member): tambah 1 data
   → muncul di tabel; ubah → tersimpan; hapus → hilang dari tabel.
3. POS: Penjualan → pilih BK-001 qty 2 → total 130000, bayar 135000,
   kembalian 5000 → Simpan → nota format `NJ-YYYYMMDD-NNNN`
   (contoh `NJ-20260923-0003`); stok BK-001 berkurang 2.
   Coba qty melebihi stok → ditolak ("Stok tidak mencukupi"), stok tetap.
4. Retur: pilih nota POS tadi + BK-001 qty 1 alasan bebas → Simpan →
   stok BK-001 bertambah 1.
5. Pembelian: supplier 1 + BK-001 qty 10 → Simpan → stok BK-001
   bertambah 10.
6. Laporan (periode mencakup 3 hari terakhir): tampilkan ke-6 jenis
   (Data Buku, Penjualan, Pembelian, Stok Menipis, Pendapatan, Buku
   Terlaris) → viewer terbuka berisi data; Export PDF tiap jenis →
   file tersimpan dan bisa dibuka (size > 0).
7. FormUser: tambah user kasir baru → logout → login dengan user baru
   (harus bisa, menu terbatas seperti Kasir).
8. Logout → kembali ke layar Login.

Hapus semua data uji yang dibuat di atas setelah selesai (agar DB kembali
ke isi seed).

## 4. Skenario Kasir (login sebagai kasir/kasir123)

1. Login → Menu Utama HANYA menampilkan: Penjualan, Retur, Laporan,
   Logout. BUKTI visible/hidden: tombol Buku, Kategori, Penerbit,
   Supplier, Member, User, Pembelian TIDAK ada.
2. POS: jual BK-003 qty 1 → nota tersimpan, stok berkurang 1.
3. Retur: retur 1 atas nota tadi → stok kembali.
4. Laporan → jenis terkunci "Penjualan", hanya menampilkan nota milik
   kasir ini (nota admin tidak muncul).
5. Register akun kasir baru dari layar login (logout dulu) → login
   dengan akun baru → menu sama terbatasnya.

## 5. Catatan Bug yang Diperbaiki

- Border/shadow hitam menutupi tombol (bayangan digambar di dalam batas
  komponen) → fix: `util/NeoShadowBorder.java` hanya menggambar strip
  inset (shadow tidak lagi menutup tombol). Terverifikasi visual.

## 6. Batasan yang Diketahui

- Foto login: bila `images/login_bg.jpg` hilang dari classpath, panel kiri
  memakai fallback ikon (bukan error).
- Database harus MySQL lokal (`localhost:3306`, user `root`, tanpa
  password, DB `db_toko_buku`); ganti di `koneksi/Koneksi.java` bila beda.
- Laporan butuh data pada periode yang dipilih; periode kosong → viewer/
  PDF tanpa baris data (bukan error).
- Dibangun untuk Java 11 target (lolos build di toolchain yang tersedia).
