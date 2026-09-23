CREATE TABLE users (
    id_user INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    nama_lengkap VARCHAR(100) NOT NULL,
    role ENUM('Admin','Kasir') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE kategori (
    id_kategori INT AUTO_INCREMENT PRIMARY KEY,
    nama_kategori VARCHAR(50) NOT NULL,
    deskripsi VARCHAR(255) NULL
);

CREATE TABLE penerbit (
    id_penerbit INT AUTO_INCREMENT PRIMARY KEY,
    nama_penerbit VARCHAR(100) NOT NULL,
    alamat VARCHAR(255),
    no_telp VARCHAR(20)
);

CREATE TABLE supplier (
    id_supplier INT AUTO_INCREMENT PRIMARY KEY,
    nama_supplier VARCHAR(100) NOT NULL,
    alamat VARCHAR(255),
    no_telp VARCHAR(20)
);

CREATE TABLE member (
    id_member INT AUTO_INCREMENT PRIMARY KEY,
    kode_member VARCHAR(20) UNIQUE,
    nama VARCHAR(100) NOT NULL,
    alamat VARCHAR(255),
    no_telp VARCHAR(20)
);

CREATE TABLE buku (
    id_buku INT AUTO_INCREMENT PRIMARY KEY,
    kode_buku VARCHAR(20) UNIQUE NOT NULL,
    judul VARCHAR(150) NOT NULL,
    penulis VARCHAR(100),
    id_penerbit INT,
    id_kategori INT,
    harga_beli DECIMAL(10,2) NOT NULL DEFAULT 0,
    harga_jual DECIMAL(10,2) NOT NULL DEFAULT 0,
    stok INT NOT NULL DEFAULT 0,
    deskripsi TEXT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_penerbit) REFERENCES penerbit(id_penerbit),
    FOREIGN KEY (id_kategori) REFERENCES kategori(id_kategori)
);

CREATE TABLE penjualan (
    id_penjualan INT AUTO_INCREMENT PRIMARY KEY,
    no_nota VARCHAR(30) UNIQUE NOT NULL,
    tanggal DATETIME NOT NULL,
    id_user INT NOT NULL,
    id_member INT NULL,
    total DECIMAL(12,2) NOT NULL DEFAULT 0,
    bayar DECIMAL(12,2) NOT NULL DEFAULT 0,
    kembalian DECIMAL(12,2) NOT NULL DEFAULT 0,
    metode_bayar VARCHAR(20) NOT NULL DEFAULT 'Tunai',
    diskon DECIMAL(12,2) NOT NULL DEFAULT 0,
    FOREIGN KEY (id_user) REFERENCES users(id_user),
    FOREIGN KEY (id_member) REFERENCES member(id_member)
);

CREATE TABLE detail_penjualan (
    id_detail INT AUTO_INCREMENT PRIMARY KEY,
    id_penjualan INT NOT NULL,
    id_buku INT NOT NULL,
    qty INT NOT NULL,
    harga_jual DECIMAL(10,2) NOT NULL,
    subtotal DECIMAL(12,2) NOT NULL,
    FOREIGN KEY (id_penjualan) REFERENCES penjualan(id_penjualan),
    FOREIGN KEY (id_buku) REFERENCES buku(id_buku)
);

CREATE TABLE pembelian (
    id_pembelian INT AUTO_INCREMENT PRIMARY KEY,
    no_faktur VARCHAR(30) UNIQUE NOT NULL,
    tanggal DATETIME NOT NULL,
    id_supplier INT NOT NULL,
    id_user INT NOT NULL,
    total DECIMAL(12,2) NOT NULL DEFAULT 0,
    FOREIGN KEY (id_supplier) REFERENCES supplier(id_supplier),
    FOREIGN KEY (id_user) REFERENCES users(id_user)
);

CREATE TABLE detail_pembelian (
    id_detail INT AUTO_INCREMENT PRIMARY KEY,
    id_pembelian INT NOT NULL,
    id_buku INT NOT NULL,
    qty INT NOT NULL,
    harga_beli DECIMAL(10,2) NOT NULL,
    subtotal DECIMAL(12,2) NOT NULL,
    FOREIGN KEY (id_pembelian) REFERENCES pembelian(id_pembelian),
    FOREIGN KEY (id_buku) REFERENCES buku(id_buku)
);

CREATE TABLE retur (
    id_retur INT AUTO_INCREMENT PRIMARY KEY,
    no_retur VARCHAR(30) UNIQUE NOT NULL,
    tanggal DATETIME NOT NULL,
    id_penjualan INT NOT NULL,
    id_buku INT NOT NULL,
    qty INT NOT NULL,
    alasan VARCHAR(255),
    FOREIGN KEY (id_penjualan) REFERENCES penjualan(id_penjualan),
    FOREIGN KEY (id_buku) REFERENCES buku(id_buku)
);

INSERT INTO users (username, password, nama_lengkap, role)
VALUES ('admin', 'admin123', 'Administrator', 'Admin');
