# Spec Desain Ronde 4 — Hapus Member, tambah ATK, rapikan report

Status: disetujui user ("gass implement"). Acuan: `docs/prd.md` + `docs/design.md`.
Stock: tidak ada yang TBD. Semua angka/nama eksplisit di bawah.

## 1. Hapus Member total

- DB (`database/migrasi_ronde4.sql`, idempoten MariaDB): DROP FK
  `penjualan.id_member` lalu `DROP TABLE member`. `database/schema.sql`:
  hapus CREATE TABLE member + kolom `id_member` di penjualan.
  `database/seed_demo.sql`: hapus INSERT member; penjualan seed `id_member=NULL`.
- Kode HAPUS: `model/Member.java`, `dao/MemberDAO.java`,
  `dao/MemberDAOImpl.java`, `view/FormMember.java`.
- `model/Penjualan.java` + `dao/PenjualanDAOImpl.java`: hapus field/mapping
  `idMember/kodeMember` (SELECT/INSERT/UPDATE tanpa kolom itu).
  `LapPenjualan`: hapus field `member`; jrxml penjualan: kolom Member diganti
  kolom Metode (sudah ada) — kolom menjadi: Nota, Tanggal, Kasir, Metode,
  Diskon, Total.
- `view/MenuUtama.java`: tombol Member diganti tombol ATK (posisi sama,
  ikon `STORE`/`CART`-sejenis terverifikasi javap, FQN `view.FormAtk`).
- `view/FormPenjualan.java`: hapus combo member + `hitungDiskonMember` +
  konstanta syarat (`SYARAT_DISKON_QTY/_NOMINAL/_DISKON_MEMBER_PCT` dihapus
  dari AppConfig bila tak dipakai lagi); struk selalu non-member.
- Sisa lapMember (model/DAO bila ada): hapus.
- `README.md` + `TESTING.md`: hapus jejak member.

## 2. Diskon POS baru: persen + nominal (bebas semua role, tanpa batas)

- Ringkasan: Subtotal → Diskon % (input, default 0) → Potongan Rp (input,
  default 0) → Total = (subtotal − persen%) − nominal.
- Validasi: `0 <= persen <= 100` else "Diskon persen 0-100";
  `0 <= nominal <= sisa-setelah-persen` else "Potongan melebihi sisa".
- `penjualan.diskon` = total gabungan rupiah (`round(subtotal*p/100) + nominal`).
- Struk: 2 baris "Diskon (x%)" + "Potongan". Riwayat: 1 baris total (aturan R3-1 tetap).

## 3. ATK via kolom `jenis` + `satuan`

- DB: `buku` tambah `jenis ENUM('Buku','ATK') NOT NULL DEFAULT 'Buku'` dan
  `satuan VARCHAR(20) NULL` (NULL = tampil "-"; untuk ATK wajib pilih).
  Daftar satuan: `pcs, pak, lusin, box, rim`.
- `model/Buku.java`: +`jenis:String` (default "Buku"), +`satuan:String`
  (nullable); DAO map/insert/update mencakup keduanya.
- `dao/BukuDAO.java` + impl: `getAllByJenis(String)`, `searchByJenis(String, keyword)`
  (semua query现有 + `AND jenis=?`).
- `view/FormBuku.java`: selalu filter `jenis='Buku'` (loadTable + cari).
- `view/FormAtk.java` BARU (pola FormBuku, FQN `view.FormAtk`): field Kode,
  Nama Barang (→ kolom judul), Kategori (combo), Satuan (combo 5 opsi, wajib),
  Harga Beli, Harga Jual, Stok, Deskripsi opsional. Penulis/penerbit: TIDAK ada
  (insert NULL). Validasi: wajib kode/nama/kategori/satuan, angka harga,
  integer stok, unik kode ("Kode sudah dipakai").
- `view/FormPenjualan.java`: pencarian + keranjang campur Buku+ATK
  (pakai `search` tanpa filter); kolom Jenis di tabel hasil + keranjang;
  baris item tampil satuan ("2 pak x 15.000"); struk item ikut satuan.
- `view/FormPembelian.java`: pencarian campur + kolom Jenis di daftar item;
  hint diperbarui ("berlaku untuk Buku & ATK").
- Kategori reuse (tambah seed kategori "Alat Tulis").
- Seed: 6 contoh ATK (pulpen, buku tulis, pensil, penghapus, penggaris, spidol;
  harga realistis, satuan bervariasi, stok 20-100).

## 4. Report: hapus 2 menu, tambah kolom/filter Jenis

- Hapus dari menu laporan: Buku Terlaris + Stok Menipis (tetap di Dashboard;
  DAO `lapTerlaris`/`getStokMenipis` TETAP untuk dashboard).
  Hapus file `lap_terlaris.jrxml` + `lap_stok.jrxml`; hapus konstanta JENIS +
  mapping + excel-mapper terkait di FormLaporan; update
  `src/test/.../ReportCompilationTest.java` (9 → 7).
  Combo Kasir tersisa: Penjualan, Retur.
- 7 laporan tersisa berbasis buku (Data Buku, Penjualan?, Pendapatan,
  Terlaris-dihapus, Laba Kotor, Stok-dihapus, Data): yang memakai bean item
  buku (Data Buku, Pendapatan, Laba Kotor) dapat **kolom Jenis**;
  FormLaporan dapat dropdown filter **Semua/Buku/ATK** (filter list di Java
  SETELAH query DAO, tanpa ubah SQL; berlaku untuk laporan berbasis buku;
  disembunyikan untuk Penjualan/Pembelian/Supplier/Retur).
- Bean item (LapStok? LapPendapatan? LapLabaKotor? LapTerlaris-dihapus):
  tambah field `jenis` + DAO map dari `b.jenis`. (Daftar bean dicek saat
  implementasi terhadap kode aktual; prinsip: semua baris item buku
  membawa jenis.)

## 5. Testing & DoD ronde 4

- Probe per task (prefix TEST-R4, cleanup + assert COUNT 0): member hilang
  total (tabel tak ada, combo tak ada, diskon persen+nominal batas:
  persen 0/50/100/101, nominal pas/lebih), ATK CRUD + satuan wajib,
  POS campur + satuan tampil, pembelian ATK + stok, 7 jrxml fill +
  filter Jenis, dashboard terlaris/stok tetap jalan.
- E2E Task 20: skenario lama (tanpa member) + baru; `mvn package`;
  README/TESTING update; review + final review.

## Self-review spec

1. Placeholder: tidak ada; semua nama kolom/nilai/daftar eksplisit.
2. Konsistensi: diskon member hilang di semua lapis (POS+struk+konstanta);
   `diskon` tetap gabungan; Riwayat R3-1 tetap; dashboard tidak berubah.
3. Scope: 4 task (17-20) sekuensial; §4 butuh cek bean aktual saat eksekusi.
4. Ambiguitas: persen dihitung dari subtotal SEBELUM nominal (§2 eksplisit);
   satuan buku NULL tampil "-" (§3); filter Jenis Java-side (§4).
