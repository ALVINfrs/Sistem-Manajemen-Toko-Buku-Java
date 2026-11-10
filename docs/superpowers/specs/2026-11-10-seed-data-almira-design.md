# Seed Data Realistis Toko Almira — Design

Tanggal: 2026-11-10
Status: Disetujui lewat diskusi 2026-11-10

## Tujuan

Mengisi database `db_toko_buku` dengan data master dan transaksi yang terlihat
seperti operasional nyata Toko Buku CV. Almira Jaya Abadi, supaya seluruh
laporan (penjualan, pembelian, laba kotor, supplier, retur, stok, grafik)
terisi dan layak dipakai demo KKP.

## Non-Goals

- Tidak mengubah kode Java (`src/main/java/**`, `src/test/java/**`).
- Tidak mengubah struktur tabel / `database/schema.sql`.
- Tidak menambah dependency Maven atau library baru.
- Tidak menambah kolom nama pelanggan/sekolah.
- Tidak mengubah logika aplikasi, auth, DAO, validasi, atau transaksi.

## Keputusan yang Sudah Disepakati

- Periode inti: 15 September – 10 Oktober 2026.
- Perpanjangan sampai 11 November 2026 agar filter default Laporan
  (30 hari terakhir dari hari ini) langsung terisi.
- Toko buka Senin–Sabtu; Minggu libur. Total 50 hari operasional.
- Reset total master + transaksi; users `admin` dan `kasir` dibuat ulang.
- Tanpa nama pelanggan; penjualan borongan sekolah dibedakan dari qty besar
  dan nilai nota.
- Retur penjualan 2–3% dari nota.
- File seed baru: `database/seed_almira_real.sql`.
- File verifikasi: `database/verify_seed_almira.sql`.
- Data lama dibackup ke folder temp sebelum import.
- Import langsung ke MySQL lokal XAMPP setelah seed dibuat.

## Data Master

- 11 penerbit: Erlangga, Yudhistira, Intan Pariwara, Grafindo Media Pratama,
  Tiga Serangkai, Penerbit Andi, Gramedia Pustaka Utama, Mizan,
  Elex Media Komputindo, M&C!, Pusat Perbukuan.
- 6 supplier distribusi buku dan ATK.
- 9 kategori: Buku SD, Buku SMP, Buku SMA/SMK, LKS & Modul, UTBK/SNBT,
  Novel, Komik, Agama & Umum, Alat Tulis.
- 80 produk: 56 buku + 24 ATK.
- Harga dan margin realistis:
  - Buku paket/LKS: margin tipis 15–30%.
  - Novel/komik: margin 25–40%.
  - ATK: margin 20–50%.
- ATK memakai `jenis='ATK'`, `satuan` wajib, `id_penerbit` NULL.
- Kode buku: `BK-001`…`BK-056`; ATK: `ATK-001`…`ATK-024`.

## Pola Transaksi

- 650–800 nota penjualan, 2.000+ baris detail.
- Penjualan perorangan: 10–18 nota/hari, 1–4 item, qty 1–3.
- Penjualan borongan sekolah: ±40–60 nota, 3–6 judul LKS/buku paket,
  qty 20–45 per judul, nilai Rp1,5–6 juta.
- Pembayaran: Tunai 70%, QRIS 20%, Transfer 10%.
  - Tunai: `bayar >= total`, kembalian = bayar − total.
  - QRIS/Transfer: bayar = total, kembalian 0.
- Diskon: 0 untuk perorangan; 5% untuk sebagian nota borongan.
- 15–40 faktur pembelian, 4–10 judul per faktur; restock dari supplier.
- 15–25 retur penjualan (2–3% nota), qty 1–3, alasan realistis.
- Jam penjualan 08.15–19.45; pembelian pagi 09.00–11.00.
- Tidak ada transaksi hari Minggu.

## Aturan Konsistensi

- Format nota: `NJ-YYYYMMDD-####`, faktur `FB-YYYYMMDD-####`,
  retur `RT-YYYYMMDD-####`; unik.
- `penjualan.total = SUM(detail_penjualan.subtotal) − diskon`.
- `pembelian.total = SUM(detail_pembelian.subtotal)`.
- Stok akhir = stok awal + pembelian − penjualan + retur; tidak negatif.
- Qty retur per nota+barang tidak melebihi qty terjual.
- Semua foreign key valid.
- Simulasi stok berjalan kronologis; pembelian dibuat saat stok menipis.
- Semua teks UTF-8, tidak ada mojibake.

## Acceptance Criteria

- Import SQL selesai tanpa error.
- 80 produk (56 buku, 24 ATK), 2 user, 11 penerbit, 6 supplier, 9 kategori.
- 650–800 penjualan, 15–40 pembelian, 15–25 retur.
- Semua query verifikasi menghasilkan 0 pelanggaran.
- Dashboard dan Laporan (periode default) menampilkan data.
- Chart kurva laporan menampilkan tren harian yang wajar.
- `mvn clean package` tetap 21/21 lulus.
- Tidak ada perubahan pada kode Java.

## Hasil Eksekusi (2026-11-11)

- 80 produk (56 buku + 24 ATK), 11 penerbit, 6 supplier, 9 kategori, 2 user.
- 785 penjualan (2.152 detail, 55 nota borongan ≥ Rp1 juta), 36 pembelian
  (79 detail), 20 retur.
- Omzet Rp516.661.500; total pembelian Rp447.580.000; refund retur
  Rp1.680.000; stok akhir minimum 24.
- Pembayaran: Tunai 568, QRIS 141, Transfer 76.
- Semua query `database/verify_seed_almira.sql` berstatus OK; `mvn clean
  package` 21/21 lulus; probe screenshot `PROBE-R7-FULLSCREEN-OK`.
