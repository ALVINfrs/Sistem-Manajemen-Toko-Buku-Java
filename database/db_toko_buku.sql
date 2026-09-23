-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Sep 23, 2026 at 03:08 PM
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
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `deskripsi` text DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `buku`
--

INSERT INTO `buku` (`id_buku`, `kode_buku`, `judul`, `penulis`, `id_penerbit`, `id_kategori`, `harga_beli`, `harga_jual`, `stok`, `created_at`, `deskripsi`) VALUES
(1, 'BK-001', 'Senja di Ujung Sawah', 'Ahmad Hidayat', 1, 1, 45000.00, 65000.00, 37, '2026-09-23 03:45:34', NULL),
(2, 'BK-002', 'Jejak Langkah Merdeka', 'Sari Wulandari', 2, 2, 60000.00, 85000.00, 19, '2026-09-23 03:45:34', NULL),
(3, 'BK-003', 'Petualangan Si Kancil', 'Rina Kartika', 3, 3, 25000.00, 35000.00, 149, '2026-09-23 03:45:34', NULL),
(4, 'BK-004', 'Matematika Dasar Kelas 6', 'Bambang Sutrisno', 1, 4, 40000.00, 55000.00, 40, '2026-09-23 03:45:34', NULL),
(5, 'BK-005', 'Pendekar Cisadane', 'Joko Prasetyo', 2, 5, 30000.00, 45000.00, 40, '2026-09-23 03:45:34', NULL),
(6, 'BK-006', 'Cinta di Musim Hujan', 'Dewi Anggraini', 3, 1, 50000.00, 70000.00, 15, '2026-09-23 03:45:34', NULL),
(7, 'BK-007', 'Sejarah Nusantara', 'Slamet Riyadi', 1, 2, 75000.00, 100000.00, 12, '2026-09-23 03:45:34', NULL),
(8, 'BK-008', 'Dongeng Nusantara', 'Maya Putri', 2, 3, 28000.00, 40000.00, 35, '2026-09-23 03:45:34', NULL),
(9, 'BK-009', 'Fisika SMA Kelas 10', 'Hendra Gunawan', 3, 4, 55000.00, 75000.00, 18, '2026-09-23 03:45:34', NULL),
(10, 'BK-010', 'Garuda Sakti', 'Andi Wijaya', 1, 5, 32000.00, 45000.00, 45, '2026-09-23 03:45:34', NULL),
(11, 'BK-011', 'Rindu di Kota Tua', 'Fitri Handayani', 2, 1, 48000.00, 68000.00, 32, '2026-09-23 03:45:34', NULL),
(12, 'BK-012', 'Panduan Wirausaha Muda', 'Budi Hartono', 3, 2, 65000.00, 90000.00, 2, '2026-09-23 03:45:34', NULL);

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
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `buku`
--
ALTER TABLE `buku`
  MODIFY `id_buku` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=19;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `buku`
--
ALTER TABLE `buku`
  ADD CONSTRAINT `buku_ibfk_1` FOREIGN KEY (`id_penerbit`) REFERENCES `penerbit` (`id_penerbit`),
  ADD CONSTRAINT `buku_ibfk_2` FOREIGN KEY (`id_kategori`) REFERENCES `kategori` (`id_kategori`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
