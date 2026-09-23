# Sistem Informasi Manajemen Toko Buku — CV Almira Jaya Abadi

Aplikasi desktop Java Swing untuk penjualan, pembelian, retur, master data,
dan laporan (JasperReports + export PDF) di toko buku CV Almira Jaya Abadi.

## Syarat

- XAMPP + MySQL/MariaDB (atau server MySQL lain), database `db_toko_buku`
- JDK 11+ dan Maven (portable Maven bisa dipakai via CLI)
- NetBeans (atau IDE lain) untuk membuka sebagai Maven Project

## Setup Database

```sql
CREATE DATABASE db_toko_buku;
```

Import wajib (skema final, 11 tabel):

```sh
mysql -u root db_toko_buku < database/schema.sql
```

Opsional — data demo kaya (2 user, 5 kategori, 3 penerbit, 3 supplier,
5 member, 12 buku BK-001..BK-012, 2 penjualan, 1 pembelian, 1 retur):

```sh
mysql -u root db_toko_buku < database/seed_demo.sql
```

Untuk ulang dari nol: `DROP DATABASE` + `CREATE DATABASE` + import skema
(+ seed bila perlu). Koneksi dipakai aplikasi: `jdbc:mysql://localhost:3306/db_toko_buku`
user `root` tanpa password (lihat `src/main/java/koneksi/Koneksi.java`).

## Buka + Jalankan (NetBeans)

1. File → Open Project → pilih folder ini (Maven Project).
2. Build Project (membersihkan + mengompilasi).
3. Run (main class `view.Login`).

## Akun

| Username | Password   | Role  |
|----------|------------|-------|
| admin    | admin123   | Admin |
| kasir    | kasir123   | Kasir |

Akun admin berasal dari schema.sql, kasir dari seed_demo.sql.
Catatan: `schema.sql` men-seed admin dengan password plain, sedangkan
aplikasi membandingkan hash SHA-256 (`view.Login` + `util.HashUtil`),
jadi login `admin/admin123` butuh normalisasi hash dari `seed_demo.sql`
(`UPDATE users SET password = SHA2('admin123', 256) ...`). Praktisnya:
import `seed_demo.sql` agar kedua akun bisa login. Tombol
"Daftar Akun Kasir" di layar login membuat akun baru yang otomatis
berperan Kasir (bisa langsung dipakai login).

## Foto Login

Sudah bundled: `src/main/resources/images/login_bg.jpg` tampil di panel
kiri layar login. Bila file tidak ditemukan, aplikasi otomatis memakai
fallback (ikon buku) — login tetap jalan normal.

## Fitur per Role

Admin (semua modul): Buku, Kategori, Penerbit, Supplier, Member,
Manajemen User (FormUser), Penjualan (POS + stok otomatis), Pembelian
(stok bertambah), Retur (stok kembali), 6 Laporan + Export PDF
(Data Buku, Penjualan, Pembelian, Stok Menipis, Pendapatan/Laba,
Buku Terlaris).

Kasir (terbatas): Penjualan, Retur, Laporan Penjualan (hanya nota miliknya
sendiri). Modul master, pembelian, dan manajemen user disembunyikan.

## Struktur Folder

```text
database/            schema.sql (final), seed_demo.sql (demo)
docs/                PRD, DESIGN, rencana
src/main/java/
  dao/               DAO + Impl semua entity (JDBC, transaksi di Impl)
  koneksi/           Koneksi MySQL
  model/             POJO entity + model laporan
  report/            ReportHelper (JasperReports compile/fill/export PDF)
  util/              Sesi, HashUtil (SHA-256), NotaGenerator (NJ/FB/RT),
                     NeoBrutalTheme, NeoShadowBorder, Validasi, AppConfig
  view/              Login, MenuUtama, RegisterDialog, Form master (5),
                     FormUser, FormPenjualan, FormPembelian, FormRetur,
                     FormLaporan, StrukDialog
src/main/resources/
  images/login_bg.jpg
  reports/           6 file .jrxml laporan
```

## CLI Alternatif

```sh
mvn package
java -jar target/sistem-toko-buku-1.0.0.jar
```

Catatan: bila `mvn` belum ada di PATH, pakai path portable Maven Anda
sebagai alternatif (contoh `C:\...\mvn.cmd package`).

JAR hasil `package` adalah fat/shaded JAR (semua dependensi di dalam,
termasuk JasperReports) — terbukti bisa export PDF langsung dari JAR.
Panduan testing manual: lihat `TESTING.md`.
