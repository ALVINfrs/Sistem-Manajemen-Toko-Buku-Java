-- ============================================================
-- Verifikasi seed data Toko Almira
-- Jalankan: mysql -u root db_toko_buku < database/verify_seed_almira.sql
-- Semua baris harus berstatus OK (kecuali baris informasi).
-- ============================================================

SELECT 'users' AS check_name, COUNT(*) AS value,
       IF(COUNT(*) = 2, 'OK', 'FAIL') AS status FROM users
UNION ALL
SELECT 'kategori', COUNT(*), IF(COUNT(*) = 9, 'OK', 'FAIL') FROM kategori
UNION ALL
SELECT 'penerbit', COUNT(*), IF(COUNT(*) = 11, 'OK', 'FAIL') FROM penerbit
UNION ALL
SELECT 'supplier', COUNT(*), IF(COUNT(*) = 6, 'OK', 'FAIL') FROM supplier
UNION ALL
SELECT 'buku_total', COUNT(*), IF(COUNT(*) = 80, 'OK', 'FAIL') FROM buku
UNION ALL
SELECT 'buku_jenis_buku', COUNT(*), IF(COUNT(*) = 56, 'OK', 'FAIL') FROM buku WHERE jenis = 'Buku'
UNION ALL
SELECT 'buku_jenis_atk', COUNT(*), IF(COUNT(*) = 24, 'OK', 'FAIL') FROM buku WHERE jenis = 'ATK'
UNION ALL
SELECT 'penjualan', COUNT(*), IF(COUNT(*) BETWEEN 650 AND 800, 'OK', 'FAIL') FROM penjualan
UNION ALL
SELECT 'detail_penjualan', COUNT(*), IF(COUNT(*) >= 2000, 'OK', 'FAIL') FROM detail_penjualan
UNION ALL
SELECT 'pembelian', COUNT(*), IF(COUNT(*) BETWEEN 15 AND 45, 'OK', 'FAIL') FROM pembelian
UNION ALL
SELECT 'detail_pembelian', COUNT(*), IF(COUNT(*) >= 50, 'OK', 'FAIL') FROM detail_pembelian
UNION ALL
SELECT 'retur', COUNT(*), IF(COUNT(*) BETWEEN 15 AND 25, 'OK', 'FAIL') FROM retur;

SELECT 'penjualan_range' AS check_name,
       CONCAT(MIN(DATE(tanggal)), ' s/d ', MAX(DATE(tanggal))) AS value,
       IF(MIN(DATE(tanggal)) = '2026-09-15' AND MAX(DATE(tanggal)) = '2026-11-11', 'OK', 'FAIL') AS status
FROM penjualan
UNION ALL
SELECT 'pembelian_range',
       CONCAT(MIN(DATE(tanggal)), ' s/d ', MAX(DATE(tanggal))),
       IF(MIN(DATE(tanggal)) >= '2026-09-15' AND MAX(DATE(tanggal)) <= '2026-11-11', 'OK', 'FAIL')
FROM pembelian
UNION ALL
SELECT 'retur_range',
       CONCAT(MIN(DATE(tanggal)), ' s/d ', MAX(DATE(tanggal))),
       IF(MIN(DATE(tanggal)) >= '2026-09-15' AND MAX(DATE(tanggal)) <= '2026-11-11', 'OK', 'FAIL')
FROM retur;

SELECT 'sales_on_sunday' AS check_name, COUNT(*) AS value,
       IF(COUNT(*) = 0, 'OK', 'FAIL') AS status
FROM penjualan WHERE DAYOFWEEK(tanggal) = 1
UNION ALL
SELECT 'purchases_on_sunday', COUNT(*), IF(COUNT(*) = 0, 'OK', 'FAIL')
FROM pembelian WHERE DAYOFWEEK(tanggal) = 1
UNION ALL
SELECT 'returns_on_sunday', COUNT(*), IF(COUNT(*) = 0, 'OK', 'FAIL')
FROM retur WHERE DAYOFWEEK(tanggal) = 1
UNION ALL
SELECT 'sales_out_of_range', COUNT(*), IF(COUNT(*) = 0, 'OK', 'FAIL')
FROM penjualan WHERE DATE(tanggal) < '2026-09-15' OR DATE(tanggal) > '2026-11-11';

SELECT 'sale_total_mismatch' AS check_name, COUNT(*) AS value,
       IF(COUNT(*) = 0, 'OK', 'FAIL') AS status
FROM (
    SELECT p.id_penjualan
    FROM penjualan p
    JOIN detail_penjualan d ON d.id_penjualan = p.id_penjualan
    GROUP BY p.id_penjualan, p.total, p.diskon
    HAVING ABS(p.total - (SUM(d.subtotal) - p.diskon)) > 0.01
) x
UNION ALL
SELECT 'purchase_total_mismatch', COUNT(*), IF(COUNT(*) = 0, 'OK', 'FAIL')
FROM (
    SELECT p.id_pembelian
    FROM pembelian p
    JOIN detail_pembelian d ON d.id_pembelian = p.id_pembelian
    GROUP BY p.id_pembelian, p.total
    HAVING ABS(p.total - SUM(d.subtotal)) > 0.01
) x
UNION ALL
SELECT 'negative_stock', COUNT(*), IF(COUNT(*) = 0, 'OK', 'FAIL')
FROM buku WHERE stok < 0;

SELECT 'retur_over_sold' AS check_name, COUNT(*) AS value,
       IF(COUNT(*) = 0, 'OK', 'FAIL') AS status
FROM (
    SELECT r.id_penjualan, r.id_buku
    FROM retur r
    JOIN detail_penjualan d
      ON d.id_penjualan = r.id_penjualan AND d.id_buku = r.id_buku
    GROUP BY r.id_penjualan, r.id_buku
    HAVING SUM(r.qty) > SUM(d.qty)
) x
UNION ALL
SELECT 'duplicate_no_nota', COUNT(*), IF(COUNT(*) = 0, 'OK', 'FAIL')
FROM (SELECT no_nota FROM penjualan GROUP BY no_nota HAVING COUNT(*) > 1) x
UNION ALL
SELECT 'duplicate_no_faktur', COUNT(*), IF(COUNT(*) = 0, 'OK', 'FAIL')
FROM (SELECT no_faktur FROM pembelian GROUP BY no_faktur HAVING COUNT(*) > 1) x
UNION ALL
SELECT 'duplicate_no_retur', COUNT(*), IF(COUNT(*) = 0, 'OK', 'FAIL')
FROM (SELECT no_retur FROM retur GROUP BY no_retur HAVING COUNT(*) > 1) x
UNION ALL
SELECT 'nota_format_bad', COUNT(*), IF(COUNT(*) = 0, 'OK', 'FAIL')
FROM penjualan WHERE no_nota NOT REGEXP '^NJ-[0-9]{8}-[0-9]{4}$'
UNION ALL
SELECT 'faktur_format_bad', COUNT(*), IF(COUNT(*) = 0, 'OK', 'FAIL')
FROM pembelian WHERE no_faktur NOT REGEXP '^FB-[0-9]{8}-[0-9]{4}$'
UNION ALL
SELECT 'retur_format_bad', COUNT(*), IF(COUNT(*) = 0, 'OK', 'FAIL')
FROM retur WHERE no_retur NOT REGEXP '^RT-[0-9]{8}-[0-9]{4}$'
UNION ALL
SELECT 'retur_not_in_sale', COUNT(*), IF(COUNT(*) = 0, 'OK', 'FAIL')
FROM retur r
LEFT JOIN detail_penjualan d
  ON d.id_penjualan = r.id_penjualan AND d.id_buku = r.id_buku
WHERE d.id_detail IS NULL
UNION ALL
SELECT 'retur_before_sale', COUNT(*), IF(COUNT(*) = 0, 'OK', 'FAIL')
FROM retur r JOIN penjualan p ON p.id_penjualan = r.id_penjualan
WHERE DATE(r.tanggal) < DATE(p.tanggal)
UNION ALL
SELECT 'sale_without_detail', COUNT(*), IF(COUNT(*) = 0, 'OK', 'FAIL')
FROM penjualan p LEFT JOIN detail_penjualan d ON d.id_penjualan = p.id_penjualan
WHERE d.id_detail IS NULL
UNION ALL
SELECT 'purchase_without_detail', COUNT(*), IF(COUNT(*) = 0, 'OK', 'FAIL')
FROM pembelian p LEFT JOIN detail_pembelian d ON d.id_pembelian = p.id_pembelian
WHERE d.id_detail IS NULL
UNION ALL
SELECT 'orphan_detail_penjualan', COUNT(*), IF(COUNT(*) = 0, 'OK', 'FAIL')
FROM detail_penjualan d LEFT JOIN penjualan p ON p.id_penjualan = d.id_penjualan
WHERE p.id_penjualan IS NULL
UNION ALL
SELECT 'orphan_detail_pembelian', COUNT(*), IF(COUNT(*) = 0, 'OK', 'FAIL')
FROM detail_pembelian d LEFT JOIN pembelian p ON p.id_pembelian = d.id_pembelian
WHERE p.id_pembelian IS NULL
UNION ALL
SELECT 'orphan_retur_sale', COUNT(*), IF(COUNT(*) = 0, 'OK', 'FAIL')
FROM retur r LEFT JOIN penjualan p ON p.id_penjualan = r.id_penjualan
WHERE p.id_penjualan IS NULL
UNION ALL
SELECT 'orphan_retur_buku', COUNT(*), IF(COUNT(*) = 0, 'OK', 'FAIL')
FROM retur r LEFT JOIN buku b ON b.id_buku = r.id_buku
WHERE b.id_buku IS NULL;

SELECT 'bulk_sales' AS check_name, COUNT(*) AS value,
       IF(COUNT(*) BETWEEN 40 AND 60, 'OK', 'FAIL') AS status
FROM penjualan WHERE total >= 1000000
UNION ALL
SELECT 'omzet', SUM(total), 'INFO' FROM penjualan
UNION ALL
SELECT 'pembelian_total', SUM(total), 'INFO' FROM pembelian
UNION ALL
SELECT 'retur_refund', SUM(r.qty * COALESCE(
           (SELECT dp.harga_jual FROM detail_penjualan dp
            WHERE dp.id_penjualan = r.id_penjualan AND dp.id_buku = r.id_buku LIMIT 1),
           b.harga_jual)), 'INFO'
FROM retur r JOIN buku b ON b.id_buku = r.id_buku;

SELECT 'stok_min' AS check_name, MIN(stok) AS value,
       IF(MIN(stok) >= 5, 'OK', 'FAIL') AS status FROM buku
UNION ALL
SELECT 'stok_max', MAX(stok), 'INFO' FROM buku
UNION ALL
SELECT 'payment_tunai', COUNT(*), 'INFO' FROM penjualan WHERE metode_bayar = 'Tunai'
UNION ALL
SELECT 'payment_qris', COUNT(*), 'INFO' FROM penjualan WHERE metode_bayar = 'QRIS'
UNION ALL
SELECT 'payment_transfer', COUNT(*), 'INFO' FROM penjualan WHERE metode_bayar = 'Transfer';
