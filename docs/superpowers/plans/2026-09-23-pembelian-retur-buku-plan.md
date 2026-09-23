# Implementasi Ronde 4: Pembelian Real-World, Struk & Riwayat Retur, dan Deskripsi Buku

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Menyempurnakan modul Pembelian menjadi alur pengadaan *real-world* (auto-load katalog, info supplier, simulasi stok, ringkasan pcs, faktur masuk barang), merombak modul Retur dengan kalkulasi refund real-time, tab riwayat retur, dan bukti struk retur thermal + PDF, serta menambahkan field deskripsi/sinopsis buku pada database dan Form Buku.

**Architecture:**
- **Database & Model:** Tambah kolom `deskripsi TEXT` pada tabel `buku` via migrasi idempoten `migrasi_ronde4_buku_retur.sql`. Update model `Buku` dan `BukuDAOImpl`.
- **Modul Pembelian:** Transformasi `FormPembelian` menjadi procurement dashboard dengan auto-load data buku, detail info supplier terpilih, simulasi kolom stok baru di keranjang, summary metrics (jenis, pcs, rupiah), tombol reset, dan pop-up tanda terima `FakturPembelianDialog`.
- **Modul Retur:** Ubah `FormRetur` menjadi `JTabbedPane` (Tab Input Retur & Tab Riwayat Retur), tambahkan label kalkulasi refund dinamis, pop-up tanda terima `StrukReturDialog` (cetak thermal + PDF), dan tombol cetak ulang bukti retur.

**Tech Stack:** Java Swing, FlatLaf / NeoBrutalisme, JDBC MySQL/MariaDB, Ikonli Material Design Icons, JasperReports / Java PrintService.

**Spec:** [`docs/superpowers/specs/2026-09-23-pembelian-retur-buku-design.md`](file:///E:/Sistem-Informasi-Manajemen-Toko-Buku/docs/superpowers/specs/2026-09-23-pembelian-retur-buku-design.md)

## Global Constraints
- Bahasa antarmuka: Bahasa Indonesia yang rapi dan konsisten.
- Skema transaksi (`supplier`, `pembelian`, `detail_pembelian`, `retur`) TIDAK diubah strukturnya (zero breaking change).
- Tema UI mengikuti `NeoBrutalTheme` (border tebal 2px hitam, warna pop pastel, bayangan offset).
- Kompilasi Maven (`mvn clean compile`) wajib lulus 100% tanpa error.

---

### Task 1: Migrasi Database & Backend Model/DAO Deskripsi Buku

**Files:**
- Create: `database/migrasi_ronde4_buku_retur.sql`
- Modify: `database/schema.sql:38-51`
- Modify: `src/main/java/model/Buku.java`
- Modify: `src/main/java/dao/BukuDAOImpl.java:17-160`

**Interfaces:**
- Consumes: Database `db_toko_buku`
- Produces: `Buku.getDeskripsi()`, `Buku.setDeskripsi(String)`, `BukuDAO.insert()`, `BukuDAO.update()`, `BukuDAO.search()` dengan field `deskripsi`.

- [x] **Step 1: Buat file migrasi SQL**
  Buat `database/migrasi_ronde4_buku_retur.sql`:
  ```sql
  -- Migrasi Ronde 4: Tambah Deskripsi Buku
  ALTER TABLE buku ADD COLUMN IF NOT EXISTS deskripsi TEXT NULL;
  ```
  Dan perbarui `database/schema.sql` pada tabel `buku` dengan menambahkan kolom `deskripsi TEXT NULL`.

- [x] **Step 2: Jalankan migrasi di database lokal**
  Eksekusi script migrasi ke MariaDB:
  ```powershell
  mysql -u root db_toko_buku < database/migrasi_ronde4_buku_retur.sql
  ```
  Verifikasi kolom telah masuk:
  ```powershell
  mysql -u root -e "USE db_toko_buku; DESCRIBE buku;"
  ```

- [x] **Step 3: Update `model/Buku.java`**
  Tambahkan field `private String deskripsi;` beserta getter dan setternya:
  ```java
  public String getDeskripsi() { return deskripsi; }
  public void setDeskripsi(String deskripsi) { this.deskripsi = deskripsi; }
  ```

- [x] **Step 4: Update `dao/BukuDAOImpl.java`**
  - Pada `BASE_SELECT`: tambahkan `b.deskripsi`.
  - Pada `map(ResultSet rs)`: `b.setDeskripsi(rs.getString("deskripsi"));`.
  - Pada `insert(Buku b)`: sertakan kolom `deskripsi` (9 placeholder).
  - Pada `update(Buku b)`: sertakan `deskripsi=?` (10 placeholder).
  - Pada `search(String keyword)`: tambahkan `OR b.deskripsi LIKE ?` pada klausa WHERE.

- [x] **Step 5: Verifikasi build backend**
  Jalankan `mvn test-compile` dan pastikan tidak ada error kompilasi.

- [x] **Step 6: Commit Task 1**
  ```powershell
  git add database/ model/Buku.java dao/BukuDAOImpl.java
  git commit -m "feat(buku): migrasi database dan backend field deskripsi buku"
  ```

---

### Task 2: UI Master Buku: Sinopsis & Deskripsi di FormBuku

**Files:**
- Modify: `src/main/java/view/FormBuku.java:55-250`

**Interfaces:**
- Consumes: `Buku.getDeskripsi()`, `Buku.setDeskripsi(String)`
- Produces: UI input `txtDeskripsi` di `FormBuku`

- [x] **Step 1: Tambahkan komponen `txtDeskripsi` di `FormBuku.java`**
  - Deklarasikan `private JTextArea txtDeskripsi;`.
  - Pada `buildInputCard()`, buat `txtDeskripsi = new JTextArea(3, 20);` dengan line wrapping, border hitam 2px, terbungkus `JScrollPane`.
  - Tambahkan label "Deskripsi / Sinopsis" dan pasang komponen tersebut ke layout form.

- [x] **Step 2: Sinkronisasi data form dengan baris tabel**
  - Pada `isiFieldDariBaris()`: ambil `buku.getDeskripsi()`, isi ke `txtDeskripsi.setText(b.getDeskripsi() != null ? b.getDeskripsi() : "");`.
  - Pada `bersihkan()`: tambahkan `txtDeskripsi.setText("");`.

- [x] **Step 3: Update operasi simpan dan ubah buku**
  - Pada `doSimpan()`: saat membuat object `Buku b`, panggil `b.setDeskripsi(txtDeskripsi.getText().trim());`.

- [x] **Step 4: Verifikasi kompilasi dan fungsionalitas FormBuku**
  Jalankan `mvn compile` untuk memastikan form terkompilasi bersih.

- [x] **Step 5: Commit Task 2**
  ```powershell
  git add src/main/java/view/FormBuku.java
  git commit -m "feat(ui): input deskripsi dan sinopsis buku di FormBuku"
  ```

---

### Task 3: Form Pembelian Real-World Upgrade & Faktur Masuk Barang

**Files:**
- Create: `src/main/java/view/FakturPembelianDialog.java`
- Modify: `src/main/java/view/FormPembelian.java`

**Interfaces:**
- Consumes: `SupplierDAO.getAll()`, `BukuDAO.getAll()`, `PembelianDAO.saveWithDetail()`
- Produces: `FakturPembelianDialog`, alur procurement real-world di `FormPembelian`

- [x] **Step 1: Buat dialog Bukti Masuk Barang `FakturPembelianDialog.java`**
  - Buat class `FakturPembelianDialog extends JDialog` bergaya neobrutalisme (mirip `StrukDialog`).
  - Menampilkan struk tanda terima pembelian/faktur masuk barang:
    - Judul Toko Buku Almira & Faktur Pembelian Masuk Gudang.
    - No. Faktur, Tanggal, Supplier (Nama, Alamat, Telp), User Penerima.
    - Tabel Item (Kode, Judul, Qty, Harga Beli, Subtotal).
    - Total Pembelian.
    - Tombol "Cetak Bukti" (`PrinterJob` / dialog cetak) dan tombol "Tutup".

- [x] **Step 2: Update konstruktor dan inisialisasi `FormPembelian.java`**
  - Di konstruktor `FormPembelian()`: tambahkan pemanggilan `cariBuku("");` tepat setelah `reloadSupplier();` agar katalog buku langsung tampil sejak detik pertama.
  - Tambahkan label preview No. Faktur Draft di bagian header: `lblNoFakturDraft`.

- [x] **Step 3: Tambahkan panel informasi supplier terpilih**
  - Tambahkan `JLabel lblSupplierInfo` di bawah `cmbSupplier`.
  - Berikan listener pada `cmbSupplier`: saat supplier dipilih, cari supplier yang cocok dari list dan tampilkan `📍 Alamat: [alamat] | 📞 Telp: [no_telp]`. Jika kosong, sembunyikan atau tampilkan tanda hubung.

- [x] **Step 4: Upgrade tabel keranjang & simulasi stok**
  - Ubah kolom `modelItem` menjadi: `["Kode", "Judul", "Stok Lama", "Qty Beli", "Stok Baru", "Harga Beli", "Subtotal"]`.
  - Saat `tambahItem()`: hitung `stokBaru = r.buku.getStok() + r.qty`.
  - Saat baris di `tblHasil` diklik, otomatis isi `txtHargaBeli` dengan `buku.getHargaBeli()`.

- [x] **Step 5: Tambahkan Summary Card & Tombol Reset**
  - Di panel kanan bawah, tampilkan ringkasan:
    - Total Jenis Buku (misal: `3 Jenis`)
    - Total Kuantitas (misal: `45 Pcs`)
    - Total Nominal Tagihan (`lblTotal`)
  - Tambahkan tombol "Reset" bergaya secondary di samping tombol "Hapus" untuk mengosongkan keranjang pembelian dengan konfirmasi.

- [x] **Step 6: Panggil `FakturPembelianDialog` setelah berhasil simpan**
  - Pada `doSimpan()`: setelah `pembelianDAO.saveWithDetail(h, details);` berhasil, tampilkan `new FakturPembelianDialog(frame, h, details, supplierName, supplierAlamat, supplierTelp).setVisible(true);`.

- [x] **Step 7: Verifikasi kompilasi Maven**
  Jalankan `mvn compile` untuk memastikan modul pembelian bebas error.

- [x] **Step 8: Commit Task 3**
  ```powershell
  git add src/main/java/view/FakturPembelianDialog.java src/main/java/view/FormPembelian.java
  git commit -m "feat(pembelian): alur procurement real-world, simulasi stok, dan bukti faktur penerimaan barang"
  ```

---

### Task 4: Bukti Struk & Dokumen Retur: `StrukReturDialog.java`

**Files:**
- Create: `src/main/java/view/StrukReturDialog.java`

**Interfaces:**
- Consumes: Data `Retur`, `Penjualan`, `Buku`
- Produces: `StrukReturDialog` dengan thermal monospace view + cetak printer + export PDF tanda terima.

- [x] **Step 1: Implementasi template thermal monospace pada `StrukReturDialog.java`**
  - Buat `StrukReturDialog extends JDialog`.
  - Format nota tanda terima retur thermal:
    ```
    ==========================================
               TOKO BUKU ALMIRA               
        Bukti Tanda Terima Retur Buku         
    ==========================================
    No. Retur   : RT-20260923-0001
    Tanggal     : 23-09-2026 18:30
    Kasir       : Kasir Demo
    ------------------------------------------
    No. Nota Asli : NJ-20260923-0001
    Pelanggan     : Umum / Member
    ------------------------------------------
    Buku yang Diretur:
    [BK-001] Senja di Ujung Sawah
    Qty Retur   : 1 pcs
    Harga Satuan: Rp 65.000
    Alasan      : Halaman rusak/cacat
    ------------------------------------------
    TOTAL REFUND (UANG KEMBALI):
    Rp 65.000
    ==========================================
       Barang & Pengembalian Dana Telah       
          Diterima & Diselesaikan             
    ==========================================
    ```

- [x] **Step 2: Tambahkan aksi Cetak Struk dan Export PDF**
  - Tombol "Cetak Struk": memanggil `txtStruk.print()` melalui `PrinterJob`.
  - Tombol "Export PDF": generate PDF tanda terima formal menggunakan JasperReports atau file chooser PDF.
  - Tombol "Tutup": dispose dialog.

- [x] **Step 3: Verifikasi kompilasi `StrukReturDialog.java`**
  Jalankan `mvn compile`.

- [x] **Step 4: Commit Task 4**
  ```powershell
  git add src/main/java/view/StrukReturDialog.java
  git commit -m "feat(retur): dialog struk thermal dan dokumen bukti retur"
  ```

---

### Task 5: Modul Retur Lengkap: Live Refund & Dua Tab (Input & Riwayat)

**Files:**
- Modify: `src/main/java/dao/ReturDAO.java`
- Modify: `src/main/java/dao/ReturDAOImpl.java`
- Modify: `src/main/java/view/FormRetur.java`

**Interfaces:**
- Consumes: `ReturDAO.getAll()`, `PenjualanDAO`, `StrukReturDialog`
- Produces: `FormRetur` 2 Tab (Input Retur & Riwayat Retur) dengan kalkulasi refund real-time.

- [x] **Step 1: Perkaya DAO Retur untuk data riwayat**
  - Pastikan `ReturDAOImpl.getAll()` atau method pencarian mengambil data lengkap: `no_retur`, `tanggal`, `id_penjualan`, `no_nota`, `id_buku`, `kode_buku`, `judul`, `qty`, `alasan`, dan harga jual buku dari penjualan untuk hitung nominal refund.

- [x] **Step 2: Restrukturisasi `FormRetur.java` dengan `JTabbedPane`**
  - Ganti root layout `FormRetur` menjadi `BorderLayout` dengan `JTabbedPane` di tengah:
    - **Tab 1:** "Input Retur" (berisi panel cari nota, item nota, form input retur).
    - **Tab 2:** "Riwayat Retur" (berisi tabel daftar retur, pencarian, dan tombol cetak ulang).

- [x] **Step 3: Tambahkan kalkulasi refund real-time pada Tab Input**
  - Di panel input retur, tambahkan `JLabel lblRefundInfo` (warna primary, font bold).
  - Pasang DocumentListener pada `txtQty`: saat kasir mengetik qty retur, otomatis hitung:
    `refund = qty * d.getHargaJual();`
    Tampilkan: `Uang Kembali (Refund): Rp [format rupiah]`.

- [x] **Step 4: Tampilkan `StrukReturDialog` setelah retur disimpan**
  - Pada `doSimpan()`: setelah retur tersimpan dan stok buku bertambah, langsung buka `new StrukReturDialog(...)`.
  - Refresh tabel riwayat retur pada Tab 2.

- [x] **Step 5: Implementasikan Tab Riwayat Retur & Cetak Ulang**
  - Buat tabel `tblRiwayatRetur` dengan kolom: `["No. Retur", "Tanggal", "No. Nota", "Buku", "Qty", "Alasan", "Refund"]`.
  - Berikan kolom pencarian live untuk no. retur atau no. nota.
  - Tambahkan tombol "Cetak Ulang Bukti" yang akan membuka `StrukReturDialog` untuk baris yang dipilih.

- [x] **Step 6: Verifikasi kompilasi dan keselarasan UI**
  Jalankan `mvn compile`.

- [x] **Step 7: Commit Task 5**
  ```powershell
  git add src/main/java/dao/ReturDAO* src/main/java/view/FormRetur.java
  git commit -m "feat(retur): integrasi dua tab input & riwayat retur, live refund, dan cetak ulang nota"
  ```

---

### Task 6: Verifikasi Akhir & Pengujian End-to-End

**Files:**
- Test all modified files

- [x] **Step 1: Jalankan kompilasi penuh dan unit test**
  ```powershell
  mvn clean test
  ```
  Pastikan `BUILD SUCCESS` tanpa warning/error fatal.

- [x] **Step 2: Verifikasi alur Form Buku**
  - Jalankan aplikasi (`mvn exec:java -Dexec.mainClass="view.Login"`).
  - Buka Master Buku, periksa field "Deskripsi / Sinopsis", coba simpan buku dengan sinopsis, dan pastikan datanya tersimpan dan terbaca.

- [x] **Step 3: Verifikasi alur Form Pembelian**
  - Buka Form Pembelian: pastikan katalog buku langsung muncul (auto-load).
  - Pilih supplier: pastikan info alamat dan nomor telepon supplier muncul.
  - Tambah buku ke keranjang: periksa kolom simulasi stok (`Stok Lama -> Qty -> Stok Baru`) dan ringkasan kuantitas pcs.
  - Simpan pembelian: periksa munculnya dialog Faktur Pembelian Masuk Barang.

- [x] **Step 4: Verifikasi alur Form Retur**
  - Buka Form Retur: coba input retur pada nota yang ada, perhatikan info nominal refund uang kembali.
  - Simpan retur: pastikan pop-up Struk Retur muncul lengkap dengan tombol Cetak & PDF.
  - Buka Tab Riwayat Retur: periksa apakah transaksi retur yang barusan dibuat sudah tercatat di tabel riwayat dan bisa dicetak ulang.

- [x] **Step 5: Commit akhir dan dokumentasi**
  ```powershell
  git add .
  git commit -m "chore: finalisasi pengujian end-to-end modul pembelian, retur, dan deskripsi buku"
  ```
