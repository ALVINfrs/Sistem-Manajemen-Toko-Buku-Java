-- ============================================================
-- Migrasi Ronde 4 — Hapus Member
-- File: database/migrasi_ronde4.sql
--
-- Run-book:
--   mysql -u root db_toko_buku < database/migrasi_ronde4.sql
--
-- Efek: penjualan DROP FK id_member (nama asli: penjualan_ibfk_2),
--   DROP COLUMN id_member, DROP TABLE member.
-- Aman di-run ulang (idempoten): FK hanya di-drop bila masih ada.
-- ============================================================

SET @fk := (
    SELECT CONSTRAINT_NAME
    FROM information_schema.KEY_COLUMN_USAGE
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'penjualan'
      AND COLUMN_NAME = 'id_member'
      AND REFERENCED_TABLE_NAME = 'member'
    LIMIT 1
);

SET @sql := IF(@fk IS NOT NULL,
    CONCAT('ALTER TABLE penjualan DROP FOREIGN KEY `', @fk, '`'),
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

ALTER TABLE penjualan DROP COLUMN IF EXISTS id_member;
DROP TABLE IF EXISTS member;
