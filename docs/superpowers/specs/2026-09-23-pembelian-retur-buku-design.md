# Spec Desain Ronde 4 — Real-World Pembelian, Struk & Riwayat Retur, dan Deskripsi Buku

Status: Disetujui user ("oke gass implementasi tulis plannya dulu").  
Acuan: Diskusi brainstorming 2026-09-23, `docs/prd.md`, `docs/design.md`, dan codebase aktif Toko Buku Almira.

---

## 1. Schema Database & Migrasi

### 1.1 Perubahan Kolom
- Tabel `buku` ditambahkan kolom `deskripsi TEXT NULL` untuk menyimpan sinopsis atau ringkasan isi buku.
- Skema tabel `supplier`, `pembelian`, `detail_pembelian`, dan `retur` tetap dipertahankan tanpa perubahan struktur (zero breaking change).

### 1.2 File Migrasi (`database/migrasi_ronde4_buku_retur.sql`)
```sql
-- Migrasi Ronde 4: Tambah Deskripsi Buku
ALTER TABLE buku ADD COLUMN IF NOT EXISTS deskripsi TEXT NULL;
```
- Idempoten: aman di-run ulang tanpa merusak data lama.
- Diupdate juga pada `database/schema.sql`.

---

## 2. Fitur Modul 1: Deskripsi / Sinopsis Buku

### 2.1 Backend (`model/Buku.java` & `dao/BukuDAOImpl.java`)
- `model/Buku.java`: tambah properti `private String deskripsi`, getter `getDeskripsi()`, setter `setDeskripsi(String)`.
- `dao/BukuDAOImpl.java`:
  - `BASE_SELECT`: tambahkan `b.deskripsi`.
  - `map()`: baca `rs.getString("deskripsi")`.
  - `insert()`: simpan kolom `deskripsi` (9 parameter).
  - `update()`: update kolom `deskripsi` (10 parameter).
  - `search()`: sertakan `b.deskripsi LIKE ?` untuk fleksibilitas pencarian.

### 2.2 UI Form Buku (`view/FormBuku.java`)
- Tambah field input `JTextArea txtDeskripsi` terbungkus `JScrollPane` (tinggi ~3 baris, font rapi, border hitam neobrutalisme).
- Saat baris tabel buku diklik, `txtDeskripsi` otomatis terisi deskripsi/sinopsis buku terkait.
- Saat tombol Reset / Batal diklik, `txtDeskripsi` dibersihkan.
- Validasi & simpan (Tambah / Ubah) otomatis menyertakan deskripsi buku.

---

## 3. Fitur Modul 2: Form Pembelian Real-World

### 3.1 UX & Inisialisasi (`view/FormPembelian.java`)
- **Auto-Load Katalog:** Panggil `cariBuku("")` pada konstruktor sehingga tabel hasil pencarian buku langsung terisi seluruh katalog aktif tanpa harus mengetik kata kunci terlebih dahulu.
- **Preview No. Faktur & Info Draft:** Tampilkan nomor faktur otomatis yang sedang didraft (misal `FB-YYYYMMDD-XXXX`).
- **Panel Detail Supplier:**
  - Saat supplier dipilih pada dropdown `cmbSupplier`, panel informasi dinamis langsung menampilkan:  
    `Alamat: [Alamat] | No. Telp: [No. Telp]` (mengambil data dari model `Supplier`).
- **Simulasi Stok di Tabel Keranjang:**
  - Struktur kolom keranjang: `["Kode", "Judul", "Stok Sekarang", "Qty Beli", "Estimasi Stok Baru", "Harga Beli", "Subtotal"]`.
  - Staf gudang/admin langsung melihat estimasi stok pasca-pembelian (`stok_sekarang + qty_beli`).
- **Summary Metrics Card:**
  - Label indikator informatif:
    - Total Jenis Buku (mis. `3 Item`)
    - Total Kuantitas (mis. `45 Pcs`)
    - Total Biaya Faktur (mis. `Rp 2.450.000`)
- **Tombol Reset Keranjang:** Tombol bersihkan keranjang belanja dengan konfirmasi.

### 3.2 Bukti Penerimaan Barang (`view/FakturPembelianDialog.java`)
- Dialog modal setelah transaksi pembelian berhasil disimpan.
- Berisi struk/lembar tanda terima penerimaan barang masuk:
  - Header: Nama Toko, Info Supplier (Nama, Alamat, Telp).
  - No. Faktur Pembelian & Tanggal/Jam Transaksi.
  - User Penerima (Admin/Kasir).
  - Tabel Item: Kode, Judul, Qty Masuk, Harga Beli Satuan, Subtotal.
  - Total Biaya Pembelian.
  - Tombol **Cetak Bukti Pembelian** (ke printer) dan tombol **Tutup**.

---

## 4. Fitur Modul 3: Modul Retur (Logika Refund & Dua Tab)

### 4.1 Logika Refund Real-Time & Tabbed UI (`view/FormRetur.java`)
- Layout utama menggunakan `JTabbedPane` dengan 2 Tab:
  1. **Tab 1 — Input Retur:**
     - Panel Kiri: Daftar 50 nota penjualan terbaru + live search filter.
     - Panel Kanan: Tabel item nota + formulir retur per-item.
     - Saat buku dipilih dan `txtQty` diisi, tampilkan **Kalkulasi Refund Real-Time**:
       `Harga Jual: Rp XX.XXX | Estimasi Uang Kembali (Refund): Rp YY.YYY`.
     - Validasi `0 < qtyRetur <= sisa`.
     - Simpan retur atomik (tabel `retur` + update stok buku).
  2. **Tab 2 — Riwayat Retur:**
     - Tabel daftar transaksi retur yang sudah terjadi (No Retur, Tanggal, No Nota, Judul Buku, Qty, Alasan, Kasir/Total).
     - Kolom pencarian riwayat retur (live filter).
     - Tombol **Cetak Ulang Bukti Retur**.

### 4.2 Bukti Nota Retur (`view/StrukReturDialog.java`)
- Format Struk Thermal Monospace Neobrutalisme:
  - Header Toko Almira.
  - No. Retur (`RT-YYYYMMDD-XXXX`), Tanggal/Jam Retur, Kasir yang melayani.
  - Referensi Nota Penjualan Asal & Nama Member (jika ada).
  - Detail Buku yang Diretur: Judul, Qty, Harga Satuan, Total Refund Uang Kembali.
  - Alasan Pengembalian Barang.
  - Footer Catatan Tanda Terima / Bukti Pengembalian Dana.
- Aksi Tombol:
  - **Cetak Struk** (ke printer kasir via Java PrintService).
  - **Export PDF** (ekspor file PDF bukti tanda terima retur resmi).
  - **Tutup**.

---

## 5. Error Handling & Edge Cases
1. **Qty Retur melebihi sisa:** Dicegah dengan validasi `qtyRetur > sisa` dengan pesan error eksplisit.
2. **Input angka non-numerik:** Validasi `Validasi.integer()` dan `Validasi.angka()` mencegah exception.
3. **Katalog kosong / stok nol:** Pembelian tetap bisa memilih buku dengan stok 0 untuk di-restock.
4. **Pembatalan keranjang:** Konfirmasi popup sebelum mengosongkan item pembelian.
5. **Rollback transaksi:** Semua operasi multi-tabel (`pembelian` + `detail_pembelian` + `stok`, dan `retur` + `stok`) dibungkus dalam blok transaksi `Connection.setAutoCommit(false)` dengan `rollback()`.
