-- ============================================================
-- Migrasi Ronde 3 — POS beneran + struk + riwayat
-- File: database/migrasi_ronde3.sql
--
-- Run-book:
--   mysql -u root db_toko_buku < database/migrasi_ronde3.sql
--
-- Aman di-run ulang (idempoten MariaDB: ADD COLUMN IF NOT EXISTS).
-- JANGAN DROP tabel: DB dev berisi seed (ALTER saja).
-- Efek: penjualan + metode_bayar (Tunai|Transfer|QRIS, default 'Tunai')
--   + diskon (gabungan member+manual, default 0);
--   kategori + deskripsi (opsional, NULL).
-- Baris lama otomatis: metode_bayar='Tunai', diskon=0, deskripsi=NULL.
-- Verifikasi: SHOW COLUMNS FROM penjualan; SHOW COLUMNS FROM kategori;
-- ============================================================

ALTER TABLE penjualan ADD COLUMN IF NOT EXISTS metode_bayar VARCHAR(20) NOT NULL DEFAULT 'Tunai';
ALTER TABLE penjualan ADD COLUMN IF NOT EXISTS diskon DECIMAL(12,2) NOT NULL DEFAULT 0;
ALTER TABLE kategori ADD COLUMN IF NOT EXISTS deskripsi VARCHAR(255) NULL;
