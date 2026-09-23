-- ============================================================
-- Migrasi Ronde 4 — Deskripsi Buku
-- File: database/migrasi_ronde4_buku_retur.sql
--
-- Run-book:
--   mysql -u root db_toko_buku < database/migrasi_ronde4_buku_retur.sql
--
-- Aman di-run ulang (idempoten MariaDB/MySQL: ADD COLUMN IF NOT EXISTS).
-- ============================================================

ALTER TABLE buku ADD COLUMN IF NOT EXISTS deskripsi TEXT NULL;
