-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Sep 23, 2026 at 09:51 AM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `db_toko_buku`
--

-- --------------------------------------------------------

--
-- Table structure for table `buku`
--

CREATE TABLE `buku` (
  `id_buku` int(11) NOT NULL,
  `kode_buku` varchar(20) NOT NULL,
  `judul` varchar(150) NOT NULL,
  `penulis` varchar(100) DEFAULT NULL,
  `id_penerbit` int(11) DEFAULT NULL,
  `id_kategori` int(11) DEFAULT NULL,
  `harga_beli` decimal(10,2) NOT NULL DEFAULT 0.00,
  `harga_jual` decimal(10,2) NOT NULL DEFAULT 0.00,
  `stok` int(11) NOT NULL DEFAULT 0,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `buku`
--

INSERT INTO `buku` (`id_buku`, `kode_buku`, `judul`, `penulis`, `id_penerbit`, `id_kategori`, `harga_beli`, `harga_jual`, `stok`, `created_at`) VALUES
(1, 'BK-001', 'Senja di Ujung Sawah', 'Ahmad Hidayat', 1, 1, 45000.00, 65000.00, 36, '2026-09-23 03:45:34'),
(2, 'BK-002', 'Jejak Langkah Merdeka', 'Sari Wulandari', 2, 2, 60000.00, 85000.00, 19, '2026-09-23 03:45:34'),
(3, 'BK-003', 'Petualangan Si Kancil', 'Rina Kartika', 3, 3, 25000.00, 35000.00, 147, '2026-09-23 03:45:34'),
(4, 'BK-004', 'Matematika Dasar Kelas 6', 'Bambang Sutrisno', 1, 4, 40000.00, 55000.00, 40, '2026-09-23 03:45:34'),
(5, 'BK-005', 'Pendekar Cisadane', 'Joko Prasetyo', 2, 5, 30000.00, 45000.00, 40, '2026-09-23 03:45:34'),
(6, 'BK-006', 'Cinta di Musim Hujan', 'Dewi Anggraini', 3, 1, 50000.00, 70000.00, 15, '2026-09-23 03:45:34'),
(7, 'BK-007', 'Sejarah Nusantara', 'Slamet Riyadi', 1, 2, 75000.00, 100000.00, 12, '2026-09-23 03:45:34'),
(8, 'BK-008', 'Dongeng Nusantara', 'Maya Putri', 2, 3, 28000.00, 40000.00, 35, '2026-09-23 03:45:34'),
(9, 'BK-009', 'Fisika SMA Kelas 10', 'Hendra Gunawan', 3, 4, 55000.00, 75000.00, 18, '2026-09-23 03:45:34'),
(10, 'BK-010', 'Garuda Sakti', 'Andi Wijaya', 1, 5, 32000.00, 45000.00, 45, '2026-09-23 03:45:34'),
(11, 'BK-011', 'Rindu di Kota Tua', 'Fitri Handayani', 2, 1, 48000.00, 68000.00, 22, '2026-09-23 03:45:34'),
(12, 'BK-012', 'Panduan Wirausaha Muda', 'Budi Hartono', 3, 2, 65000.00, 90000.00, 2, '2026-09-23 03:45:34');

-- --------------------------------------------------------

--
-- Table structure for table `detail_pembelian`
--

CREATE TABLE `detail_pembelian` (
  `id_detail` int(11) NOT NULL,
  `id_pembelian` int(11) NOT NULL,
  `id_buku` int(11) NOT NULL,
  `qty` int(11) NOT NULL,
  `harga_beli` decimal(10,2) NOT NULL,
  `subtotal` decimal(12,2) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `detail_pembelian`
--

INSERT INTO `detail_pembelian` (`id_detail`, `id_pembelian`, `id_buku`, `qty`, `harga_beli`, `subtotal`) VALUES
(1, 1, 1, 10, 45000.00, 450000.00),
(2, 1, 4, 15, 40000.00, 600000.00),
(5, 4, 3, 100, 500000.00, 50000000.00);

-- --------------------------------------------------------

--
-- Table structure for table `detail_penjualan`
--

CREATE TABLE `detail_penjualan` (
  `id_detail` int(11) NOT NULL,
  `id_penjualan` int(11) NOT NULL,
  `id_buku` int(11) NOT NULL,
  `qty` int(11) NOT NULL,
  `harga_jual` decimal(10,2) NOT NULL,
  `subtotal` decimal(12,2) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `detail_penjualan`
--

INSERT INTO `detail_penjualan` (`id_detail`, `id_penjualan`, `id_buku`, `qty`, `harga_jual`, `subtotal`) VALUES
(1, 1, 1, 2, 65000.00, 130000.00),
(2, 1, 2, 1, 85000.00, 85000.00),
(3, 2, 3, 3, 35000.00, 105000.00),
(12, 11, 1, 1, 65000.00, 65000.00),
(19, 18, 1, 1, 65000.00, 65000.00),
(20, 19, 1, 1, 65000.00, 65000.00);

-- --------------------------------------------------------

--
-- Table structure for table `kategori`
--

CREATE TABLE `kategori` (
  `id_kategori` int(11) NOT NULL,
  `nama_kategori` varchar(50) NOT NULL,
  `deskripsi` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `kategori`
--

INSERT INTO `kategori` (`id_kategori`, `nama_kategori`, `deskripsi`) VALUES
(1, 'Fiksi', NULL),
(2, 'Non-Fiksi', NULL),
(3, 'Anak', NULL),
(4, 'Pendidikan', NULL),
(5, 'Komik', NULL);

-- --------------------------------------------------------

--
-- Table structure for table `member`
--

CREATE TABLE `member` (
  `id_member` int(11) NOT NULL,
  `kode_member` varchar(20) DEFAULT NULL,
  `nama` varchar(100) NOT NULL,
  `alamat` varchar(255) DEFAULT NULL,
  `no_telp` varchar(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `member`
--

INSERT INTO `member` (`id_member`, `kode_member`, `nama`, `alamat`, `no_telp`) VALUES
(1, 'MBR-001', 'Budi Santoso', 'Jl. Mawar No. 5, Jakarta', '081234567001'),
(2, 'MBR-002', 'Siti Aminah', 'Jl. Melati No. 12, Bandung', '081234567002'),
(3, 'MBR-003', 'Agus Setiawan', 'Jl. Kenanga No. 8, Surabaya', '081234567003'),
(4, 'MBR-004', 'Dewi Lestari', 'Jl. Anggrek No. 20, Yogyakarta', '081234567004'),
(5, 'MBR-005', 'Rina Marlina', 'Jl. Dahlia No. 3, Semarang', '081234567005');

-- --------------------------------------------------------

--
-- Table structure for table `pembelian`
--

CREATE TABLE `pembelian` (
  `id_pembelian` int(11) NOT NULL,
  `no_faktur` varchar(30) NOT NULL,
  `tanggal` datetime NOT NULL,
  `id_supplier` int(11) NOT NULL,
  `id_user` int(11) NOT NULL,
  `total` decimal(12,2) NOT NULL DEFAULT 0.00
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `pembelian`
--

INSERT INTO `pembelian` (`id_pembelian`, `no_faktur`, `tanggal`, `id_supplier`, `id_user`, `total`) VALUES
(1, 'FB-20260922-0001', '2026-09-22 10:45:34', 1, 1, 1050000.00),
(4, 'FB-20260923-0001', '2026-09-23 11:28:51', 1, 1, 50000000.00);

-- --------------------------------------------------------

--
-- Table structure for table `penerbit`
--

CREATE TABLE `penerbit` (
  `id_penerbit` int(11) NOT NULL,
  `nama_penerbit` varchar(100) NOT NULL,
  `alamat` varchar(255) DEFAULT NULL,
  `no_telp` varchar(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `penerbit`
--

INSERT INTO `penerbit` (`id_penerbit`, `nama_penerbit`, `alamat`, `no_telp`) VALUES
(1, 'Pustaka Cerdas', 'Jl. Merdeka No. 10, Jakarta', '021-5551234'),
(2, 'Cahaya Ilmu', 'Jl. Cendekia No. 25, Bandung', '081234567890'),
(3, 'Wacana Baru', 'Jl. Wacana No. 7, Yogyakarta', '082134567891');

-- --------------------------------------------------------

--
-- Table structure for table `penjualan`
--

CREATE TABLE `penjualan` (
  `id_penjualan` int(11) NOT NULL,
  `no_nota` varchar(30) NOT NULL,
  `tanggal` datetime NOT NULL,
  `id_user` int(11) NOT NULL,
  `id_member` int(11) DEFAULT NULL,
  `total` decimal(12,2) NOT NULL DEFAULT 0.00,
  `bayar` decimal(12,2) NOT NULL DEFAULT 0.00,
  `kembalian` decimal(12,2) NOT NULL DEFAULT 0.00,
  `metode_bayar` varchar(20) NOT NULL DEFAULT 'Tunai',
  `diskon` decimal(12,2) NOT NULL DEFAULT 0.00
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `penjualan`
--

INSERT INTO `penjualan` (`id_penjualan`, `no_nota`, `tanggal`, `id_user`, `id_member`, `total`, `bayar`, `kembalian`, `metode_bayar`, `diskon`) VALUES
(1, 'NJ-20260921-0001', '2026-09-21 10:45:34', 1, 1, 215000.00, 220000.00, 5000.00, 'Tunai', 0.00),
(2, 'NJ-20260923-0002', '2026-09-23 10:45:34', 2, NULL, 105000.00, 105000.00, 0.00, 'Tunai', 0.00),
(11, 'NJ-20260923-0003', '2026-09-23 11:10:10', 1, NULL, 65000.00, 100000.00, 35000.00, 'Tunai', 0.00),
(18, 'NJ-20260923-0004', '2026-09-23 13:16:29', 1, NULL, 65000.00, 100000.00, 35000.00, 'Tunai', 0.00),
(19, 'NJ-20260923-0005', '2026-09-23 14:10:57', 1, NULL, 65000.00, 65000.00, 0.00, 'Transfer', 0.00);

-- --------------------------------------------------------

--
-- Table structure for table `retur`
--

CREATE TABLE `retur` (
  `id_retur` int(11) NOT NULL,
  `no_retur` varchar(30) NOT NULL,
  `tanggal` datetime NOT NULL,
  `id_penjualan` int(11) NOT NULL,
  `id_buku` int(11) NOT NULL,
  `qty` int(11) NOT NULL,
  `alasan` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `retur`
--

INSERT INTO `retur` (`id_retur`, `no_retur`, `tanggal`, `id_penjualan`, `id_buku`, `qty`, `alasan`) VALUES
(1, 'RT-20260923-0001', '2026-09-23 10:45:34', 1, 1, 1, 'Sampul rusak');

-- --------------------------------------------------------

--
-- Table structure for table `supplier`
--

CREATE TABLE `supplier` (
  `id_supplier` int(11) NOT NULL,
  `nama_supplier` varchar(100) NOT NULL,
  `alamat` varchar(255) DEFAULT NULL,
  `no_telp` varchar(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `supplier`
--

INSERT INTO `supplier` (`id_supplier`, `nama_supplier`, `alamat`, `no_telp`) VALUES
(1, 'Distributor Buku Merdeka', 'Jl. Gudang Buku No. 1, Jakarta', '021-7778888'),
(2, 'Grosir Buku Jaya', 'Jl. Pasar Buku No. 33, Surabaya', '081345678901'),
(3, 'Supplier Pustaka Nusantara', 'Jl. Pustaka No. 50, Semarang', '082256789012');

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `id_user` int(11) NOT NULL,
  `username` varchar(50) NOT NULL,
  `password` varchar(255) NOT NULL,
  `nama_lengkap` varchar(100) NOT NULL,
  `role` enum('Admin','Kasir') NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`id_user`, `username`, `password`, `nama_lengkap`, `role`, `created_at`) VALUES
(1, 'admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'Administrator', 'Admin', '2026-09-23 03:45:30'),
(2, 'kasir', 'f02b7c1e519e4fa436147f7e1399974f9510aa9c8e0cb8be29151eb540f9d214', 'Kasir Demo', 'Kasir', '2026-09-23 03:45:34');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `buku`
--
ALTER TABLE `buku`
  ADD PRIMARY KEY (`id_buku`),
  ADD UNIQUE KEY `kode_buku` (`kode_buku`),
  ADD KEY `id_penerbit` (`id_penerbit`),
  ADD KEY `id_kategori` (`id_kategori`);

--
-- Indexes for table `detail_pembelian`
--
ALTER TABLE `detail_pembelian`
  ADD PRIMARY KEY (`id_detail`),
  ADD KEY `id_pembelian` (`id_pembelian`),
  ADD KEY `id_buku` (`id_buku`);

--
-- Indexes for table `detail_penjualan`
--
ALTER TABLE `detail_penjualan`
  ADD PRIMARY KEY (`id_detail`),
  ADD KEY `id_penjualan` (`id_penjualan`),
  ADD KEY `id_buku` (`id_buku`);

--
-- Indexes for table `kategori`
--
ALTER TABLE `kategori`
  ADD PRIMARY KEY (`id_kategori`);

--
-- Indexes for table `member`
--
ALTER TABLE `member`
  ADD PRIMARY KEY (`id_member`),
  ADD UNIQUE KEY `kode_member` (`kode_member`);

--
-- Indexes for table `pembelian`
--
ALTER TABLE `pembelian`
  ADD PRIMARY KEY (`id_pembelian`),
  ADD UNIQUE KEY `no_faktur` (`no_faktur`),
  ADD KEY `id_supplier` (`id_supplier`),
  ADD KEY `id_user` (`id_user`);

--
-- Indexes for table `penerbit`
--
ALTER TABLE `penerbit`
  ADD PRIMARY KEY (`id_penerbit`);

--
-- Indexes for table `penjualan`
--
ALTER TABLE `penjualan`
  ADD PRIMARY KEY (`id_penjualan`),
  ADD UNIQUE KEY `no_nota` (`no_nota`),
  ADD KEY `id_user` (`id_user`),
  ADD KEY `id_member` (`id_member`);

--
-- Indexes for table `retur`
--
ALTER TABLE `retur`
  ADD PRIMARY KEY (`id_retur`),
  ADD UNIQUE KEY `no_retur` (`no_retur`),
  ADD KEY `id_penjualan` (`id_penjualan`),
  ADD KEY `id_buku` (`id_buku`);

--
-- Indexes for table `supplier`
--
ALTER TABLE `supplier`
  ADD PRIMARY KEY (`id_supplier`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id_user`),
  ADD UNIQUE KEY `username` (`username`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `buku`
--
ALTER TABLE `buku`
  MODIFY `id_buku` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=19;

--
-- AUTO_INCREMENT for table `detail_pembelian`
--
ALTER TABLE `detail_pembelian`
  MODIFY `id_detail` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT for table `detail_penjualan`
--
ALTER TABLE `detail_penjualan`
  MODIFY `id_detail` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=21;

--
-- AUTO_INCREMENT for table `kategori`
--
ALTER TABLE `kategori`
  MODIFY `id_kategori` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT for table `member`
--
ALTER TABLE `member`
  MODIFY `id_member` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- AUTO_INCREMENT for table `pembelian`
--
ALTER TABLE `pembelian`
  MODIFY `id_pembelian` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT for table `penerbit`
--
ALTER TABLE `penerbit`
  MODIFY `id_penerbit` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT for table `penjualan`
--
ALTER TABLE `penjualan`
  MODIFY `id_penjualan` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=20;

--
-- AUTO_INCREMENT for table `retur`
--
ALTER TABLE `retur`
  MODIFY `id_retur` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT for table `supplier`
--
ALTER TABLE `supplier`
  MODIFY `id_supplier` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `id_user` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `buku`
--
ALTER TABLE `buku`
  ADD CONSTRAINT `buku_ibfk_1` FOREIGN KEY (`id_penerbit`) REFERENCES `penerbit` (`id_penerbit`),
  ADD CONSTRAINT `buku_ibfk_2` FOREIGN KEY (`id_kategori`) REFERENCES `kategori` (`id_kategori`);

--
-- Constraints for table `detail_pembelian`
--
ALTER TABLE `detail_pembelian`
  ADD CONSTRAINT `detail_pembelian_ibfk_1` FOREIGN KEY (`id_pembelian`) REFERENCES `pembelian` (`id_pembelian`),
  ADD CONSTRAINT `detail_pembelian_ibfk_2` FOREIGN KEY (`id_buku`) REFERENCES `buku` (`id_buku`);

--
-- Constraints for table `detail_penjualan`
--
ALTER TABLE `detail_penjualan`
  ADD CONSTRAINT `detail_penjualan_ibfk_1` FOREIGN KEY (`id_penjualan`) REFERENCES `penjualan` (`id_penjualan`),
  ADD CONSTRAINT `detail_penjualan_ibfk_2` FOREIGN KEY (`id_buku`) REFERENCES `buku` (`id_buku`);

--
-- Constraints for table `pembelian`
--
ALTER TABLE `pembelian`
  ADD CONSTRAINT `pembelian_ibfk_1` FOREIGN KEY (`id_supplier`) REFERENCES `supplier` (`id_supplier`),
  ADD CONSTRAINT `pembelian_ibfk_2` FOREIGN KEY (`id_user`) REFERENCES `users` (`id_user`);

--
-- Constraints for table `penjualan`
--
ALTER TABLE `penjualan`
  ADD CONSTRAINT `penjualan_ibfk_1` FOREIGN KEY (`id_user`) REFERENCES `users` (`id_user`),
  ADD CONSTRAINT `penjualan_ibfk_2` FOREIGN KEY (`id_member`) REFERENCES `member` (`id_member`);

--
-- Constraints for table `retur`
--
ALTER TABLE `retur`
  ADD CONSTRAINT `retur_ibfk_1` FOREIGN KEY (`id_penjualan`) REFERENCES `penjualan` (`id_penjualan`),
  ADD CONSTRAINT `retur_ibfk_2` FOREIGN KEY (`id_buku`) REFERENCES `buku` (`id_buku`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
