-- ============================================================
-- Seed demo kaya + transaksi — Toko Buku Almira
-- File: database/seed_demo.sql
--
-- Jalankan sekali; untuk ulang: DROP DATABASE + import schema.sql + seed_demo.sql
-- Cara pakai (fresh):
--   mysql -u root -e "DROP DATABASE IF EXISTS db_toko_buku; CREATE DATABASE db_toko_buku;"
--   mysql -u root db_toko_buku < database/schema.sql
--   mysql -u root db_toko_buku < database/seed_demo.sql
--
-- Asumsi fresh-DB (dijamin Task 11): tabel kosong kecuali users berisi
--   admin (id_user=1). Id AUTO_INCREMENT predictable seurut insert:
--     kategori 1..5, penerbit 1..3, supplier 1..3, member 1..5,
--     buku 1..12 (BK-001..BK-012), penjualan 1..2, pembelian 1, retur 1.
--   Untuk USER kasir pada transaksi penjualan (b) dipakai subselect
--     (SELECT id_user FROM users WHERE username='kasir') agar tidak
--     bergantung pada id hardcoded. Untuk buku/supplier/member DITERIMA
--     id seurut fresh sesuai urutan INSERT di bawah.
--
-- Password di-hash SHA2(x,256) MySQL (hex lowercase, sama dengan
--   util.HashUtil.sha256 di aplikasi). Login: admin/admin123, kasir/kasir123.
-- Master (users/kategori/penerbit/supplier/member/buku) pakai INSERT IGNORE
--   agar re-run parsial tidak fatal; transaksi (nota/faktur/retur yang UNIQUE)
--   pakai INSERT biasa agar duplikat GAGAL eksplisit (sinyal, bukan silent).
-- Nomor nota dinamis-konsisten: CONCAT(prefix, DATE_FORMAT(NOW(),...))
--   tanggal pakai NOW() / NOW() - INTERVAL n DAY.
-- Prefix mengikuti util.NotaGenerator: NJ=penjualan, FB=pembelian, RT=retur.
-- ============================================================

-- ---------- USERS (master, IGNORE) ----------
-- Admin dipertahankan id 1; aman bila baris sudah ada (no-op).
INSERT IGNORE INTO users (id_user, username, password, nama_lengkap, role)
VALUES (1, 'admin', SHA2('admin123', 256), 'Administrator', 'Admin');
-- Normalisasi hash admin (idempotent): schema.sql men-seed plain 'admin123',
-- aplikasi (view.Login + HashUtil.sha256) membandingkan hash. UPDATE ini
-- memastikan admin/admin123 bisa auth baik di DB lama (sudah hash: no-op
-- secara nilai) maupun fresh import schema.sql (plain -> hash).
UPDATE users SET password = SHA2('admin123', 256) WHERE username = 'admin';
-- Kasir demo (id fresh = 2).
INSERT IGNORE INTO users (username, password, nama_lengkap, role)
VALUES ('kasir', SHA2('kasir123', 256), 'Kasir Demo', 'Kasir');

-- ---------- KATEGORI (5, master, IGNORE) -> id 1..5 ----------
INSERT IGNORE INTO kategori (nama_kategori) VALUES
('Fiksi'),
('Non-Fiksi'),
('Anak'),
('Pendidikan'),
('Komik');

-- ---------- PENERBIT (3, master, IGNORE) -> id 1..3 ----------
INSERT IGNORE INTO penerbit (nama_penerbit, alamat, no_telp) VALUES
('Pustaka Cerdas', 'Jl. Merdeka No. 10, Jakarta', '021-5551234'),
('Cahaya Ilmu', 'Jl. Cendekia No. 25, Bandung', '081234567890'),
('Wacana Baru', 'Jl. Wacana No. 7, Yogyakarta', '082134567891');

-- ---------- SUPPLIER (3, master, IGNORE) -> id 1..3 ----------
INSERT IGNORE INTO supplier (nama_supplier, alamat, no_telp) VALUES
('Distributor Buku Merdeka', 'Jl. Gudang Buku No. 1, Jakarta', '021-7778888'),
('Grosir Buku Jaya', 'Jl. Pasar Buku No. 33, Surabaya', '081345678901'),
('Supplier Pustaka Nusantara', 'Jl. Pustaka No. 50, Semarang', '082256789012');

-- ---------- MEMBER (5, master, IGNORE) -> id 1..5 ----------
INSERT IGNORE INTO member (kode_member, nama, alamat, no_telp) VALUES
('MBR-001', 'Budi Santoso', 'Jl. Mawar No. 5, Jakarta', '081234567001'),
('MBR-002', 'Siti Aminah', 'Jl. Melati No. 12, Bandung', '081234567002'),
('MBR-003', 'Agus Setiawan', 'Jl. Kenanga No. 8, Surabaya', '081234567003'),
('MBR-004', 'Dewi Lestari', 'Jl. Anggrek No. 20, Yogyakarta', '081234567004'),
('MBR-005', 'Rina Marlina', 'Jl. Dahlia No. 3, Semarang', '081234567005');

-- ---------- BUKU (12, master, IGNORE) -> id 1..12 ----------
-- Sebar 5 kategori & 3 penerbit; harga_beli 25000-120000;
-- harga_jual = beli + margin 30-50%; stok awal 10-50.
-- BK-001 Fiksi/Pustaka Cerdas 45000->65000 (44.4%) stok 30
-- BK-002 Non-Fiksi/Cahaya Ilmu 60000->85000 (41.7%) stok 20
-- BK-003 Anak/Wacana Baru 25000->35000 (40.0%) stok 50
-- BK-004 Pendidikan/Pustaka Cerdas 40000->55000 (37.5%) stok 25
-- BK-005 Komik/Cahaya Ilmu 30000->45000 (50.0%) stok 40
-- BK-006 Fiksi/Wacana Baru 50000->70000 (40.0%) stok 15
-- BK-007 Non-Fiksi/Pustaka Cerdas 75000->100000 (33.3%) stok 12
-- BK-008 Anak/Cahaya Ilmu 28000->40000 (42.9%) stok 35
-- BK-009 Pendidikan/Wacana Baru 55000->75000 (36.4%) stok 18
-- BK-010 Komik/Pustaka Cerdas 32000->45000 (40.6%) stok 45
-- BK-011 Fiksi/Cahaya Ilmu 48000->68000 (41.7%) stok 22
-- BK-012 Non-Fiksi/Wacana Baru 65000->90000 (38.5%) stok 10
INSERT IGNORE INTO buku (kode_buku, judul, penulis, id_penerbit, id_kategori, harga_beli, harga_jual, stok) VALUES
('BK-001', 'Senja di Ujung Sawah', 'Ahmad Hidayat', 1, 1, 45000, 65000, 30),
('BK-002', 'Jejak Langkah Merdeka', 'Sari Wulandari', 2, 2, 60000, 85000, 20),
('BK-003', 'Petualangan Si Kancil', 'Rina Kartika', 3, 3, 25000, 35000, 50),
('BK-004', 'Matematika Dasar Kelas 6', 'Bambang Sutrisno', 1, 4, 40000, 55000, 25),
('BK-005', 'Pendekar Cisadane', 'Joko Prasetyo', 2, 5, 30000, 45000, 40),
('BK-006', 'Cinta di Musim Hujan', 'Dewi Anggraini', 3, 1, 50000, 70000, 15),
('BK-007', 'Sejarah Nusantara', 'Slamet Riyadi', 1, 2, 75000, 100000, 12),
('BK-008', 'Dongeng Nusantara', 'Maya Putri', 2, 3, 28000, 40000, 35),
('BK-009', 'Fisika SMA Kelas 10', 'Hendra Gunawan', 3, 4, 55000, 75000, 18),
('BK-010', 'Garuda Sakti', 'Andi Wijaya', 1, 5, 32000, 45000, 45),
('BK-011', 'Rindu di Kota Tua', 'Fitri Handayani', 2, 1, 48000, 68000, 22),
('BK-012', 'Panduan Wirausaha Muda', 'Budi Hartono', 3, 2, 65000, 90000, 10);

-- ---------- PENJUALAN (2, transaksi, INSERT biasa) -> id 1..2 ----------
-- (a) nota -0001, 2 hari lalu, admin (id 1), member 1.
--     Item: BK-001 qty 2 @65000 = 130000; BK-002 qty 1 @85000 = 85000.
--     Total 215000; bayar = total + 5000 = 220000; kembalian 5000.
INSERT INTO penjualan (no_nota, tanggal, id_user, id_member, total, bayar, kembalian, metode_bayar, diskon)
VALUES (CONCAT('NJ-', DATE_FORMAT(NOW() - INTERVAL 2 DAY, '%Y%m%d'), '-0001'), NOW() - INTERVAL 2 DAY, 1, 1, 215000, 220000, 5000, 'Tunai', 0);
-- (b) nota -0002, hari ini, kasir (subselect), tanpa member, bayar pas.
--     Item: BK-003 qty 3 @35000 = 105000. Total/bayar 105000, kembalian 0.
INSERT INTO penjualan (no_nota, tanggal, id_user, id_member, total, bayar, kembalian, metode_bayar, diskon)
VALUES (CONCAT('NJ-', DATE_FORMAT(NOW(), '%Y%m%d'), '-0002'), NOW(), (SELECT id_user FROM users WHERE username = 'kasir'), NULL, 105000, 105000, 0, 'Tunai', 0);

-- ---------- DETAIL PENJUALAN (transaksi, INSERT biasa) ----------
-- id_penjualan 1 = penjualan (a); id_buku 1 = BK-001, 2 = BK-002 (fresh-DB).
INSERT INTO detail_penjualan (id_penjualan, id_buku, qty, harga_jual, subtotal) VALUES
(1, 1, 2, 65000, 130000),
(1, 2, 1, 85000, 85000);
-- id_penjualan 2 = penjualan (b); id_buku 3 = BK-003.
INSERT INTO detail_penjualan (id_penjualan, id_buku, qty, harga_jual, subtotal) VALUES
(2, 3, 3, 35000, 105000);

-- ---------- PEMBELIAN (1, transaksi, INSERT biasa) -> id 1 ----------
-- Faktur -0001, kemarin, supplier 1, admin (id 1).
--   Item: BK-001 qty 10 @45000 = 450000; BK-004 qty 15 @40000 = 600000.
--   Total 1050000.
INSERT INTO pembelian (no_faktur, tanggal, id_supplier, id_user, total)
VALUES (CONCAT('FB-', DATE_FORMAT(NOW() - INTERVAL 1 DAY, '%Y%m%d'), '-0001'), NOW() - INTERVAL 1 DAY, 1, 1, 1050000);

-- ---------- DETAIL PEMBELIAN (transaksi, INSERT biasa) ----------
-- id_pembelian 1; id_buku 1 = BK-001, 4 = BK-004 (fresh-DB).
INSERT INTO detail_pembelian (id_pembelian, id_buku, qty, harga_beli, subtotal) VALUES
(1, 1, 10, 45000, 450000),
(1, 4, 15, 40000, 600000);

-- ---------- RETUR (1, transaksi, INSERT biasa) -> id 1 ----------
-- No retur -0001, hari ini, atas penjualan (a) id 1, buku BK-001 id 1, qty 1.
-- Alasan: "Sampul rusak".
INSERT INTO retur (no_retur, tanggal, id_penjualan, id_buku, qty, alasan)
VALUES (CONCAT('RT-', DATE_FORMAT(NOW(), '%Y%m%d'), '-0001'), NOW(), 1, 1, 1, 'Sampul rusak');

-- ---------- STOK FINAL (eksplisit, sesuai perilaku DAO aplikasi) ----------
-- Rumus: stok_final = stok_awal - terjual + dibeli + diretur.
-- Penjualan mengurangi stok (PenjualanDAOImpl.saveWithDetail),
-- pembelian menambah (PembelianDAOImpl.saveWithDetail),
-- retur penjualan menambah (ReturDAOImpl.saveRetur).
-- BK-001: 30 - 2(jual a) + 10(beli) + 1(retur) = 39
UPDATE buku SET stok = 39 WHERE kode_buku = 'BK-001';
-- BK-002: 20 - 1(jual a) = 19
UPDATE buku SET stok = 19 WHERE kode_buku = 'BK-002';
-- BK-003: 50 - 3(jual b) = 47
UPDATE buku SET stok = 47 WHERE kode_buku = 'BK-003';
-- BK-004: 25 + 15(beli) = 40
UPDATE buku SET stok = 40 WHERE kode_buku = 'BK-004';
-- BK-005..BK-012 tidak tersentuh transaksi: stok tetap awal
-- (40, 15, 12, 35, 18, 45, 22, 10).
