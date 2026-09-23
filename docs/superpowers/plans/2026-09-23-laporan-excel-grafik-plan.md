# Implementasi Ronde 5: Modernisasi Modul Laporan, Export Excel Native, Kurva Grafik JFreeChart, dan Dokumen Formal JasperReports

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Mengubah modul laporan menjadi dashboard eksekutif modern di `FormLaporan.java` (dengan Kartu KPI, Live Table Preview, dan Kurva Grafik JFreeChart), menambahkan 3 laporan baru (Retur, Laba Kotor/Margin, Pengadaan Supplier), standardisasi 9 template JasperReports (.jrxml) dengan Kop Resmi Toko Buku Almira dan Kolom Tanda Tangan Ganda, serta menyediakan fitur Export Native Microsoft Excel (.xlsx via Apache POI) dengan format mata uang dan header yang rapi.

**Architecture:**
- **Backend & Data Models:** Tambahkan dependensi `org.apache.poi:poi-ooxml:5.2.5` pada `pom.xml`. Buat model DTO `LapRetur`, `LapLabaKotor`, dan `LapSupplier`. Perkaya DAO `ReturDAOImpl`, `PenjualanDAOImpl`, dan `PembelianDAOImpl` dengan method query laporan baru.
- **Reporting & Exporter Engine:** Standardisasi 9 template JasperReports (.jrxml) dengan kop resmi toko dan kolom tanda tangan (Kasir/Admin dan Kepala Toko). Buat utility `util.ExcelExporter` untuk konversi tabel data ke format `.xlsx` dengan styling header, currency format, auto-column width, dan freeze panes.
- **Frontend Dashboard:** Rombak `FormLaporan.java` dengan gaya Neobrutalisme yang memuat Filter Bar 9 laporan, Metric Strip (3 Kartu KPI), `JTabbedPane` (Tab 1: Live Table Preview dengan text search filter; Tab 2: Kurva & Grafik Visual JFreeChart dinamis), serta Action Bar lengkap (Pratinjau Cetak, Export PDF, Export Excel, Segarkan).

**Tech Stack:** Java Swing, FlatLaf / NeoBrutalism, JasperReports 6.21.3, Apache POI 5.2.5 (poi-ooxml), JFreeChart 1.0.19, LGoodDatePicker, JDBC MariaDB/MySQL.

**Spec:** [`docs/superpowers/specs/2026-09-23-laporan-excel-grafik-design.md`](file:///E:/Sistem-Informasi-Manajemen-Toko-Buku/docs/superpowers/specs/2026-09-23-laporan-excel-grafik-design.md)

## Global Constraints
- Bahasa antarmuka dan laporan: Bahasa Indonesia baku yang rapi dan konsisten.
- Skema tabel database inti (`penjualan`, `pembelian`, `retur`, `buku`, `users`) tetap utuh tanpa modifikasi destruktif (zero breaking schema changes).
- Tema UI konsisten dengan `NeoBrutalTheme` (border tebal 2px hitam, bayangan offset, warna primer pop pastel).
- Format angka mata uang di Excel dan PDF wajib menggunakan format Rupiah standar Indonesia (`Rp #,##0`).
- Kompilasi Maven (`mvn clean compile` / `mvn test`) wajib lulus 100% tanpa error.

---

### Task 1: Dependensi Apache POI & DTO Model Laporan Baru

**Files:**
- Modify: `pom.xml`
- Create: `src/main/java/model/LapRetur.java`
- Create: `src/main/java/model/LapLabaKotor.java`
- Create: `src/main/java/model/LapSupplier.java`

**Interfaces:**
- Consumes: Maven dependencies
- Produces: `org.apache.poi.xssf.usermodel.*`, `model.LapRetur`, `model.LapLabaKotor`, `model.LapSupplier`

- [ ] **Step 1: Tambahkan dependensi Apache POI di `pom.xml`**
  Tambahkan dependency `poi-ooxml` versi 5.2.5:
  ```xml
  <dependency>
      <groupId>org.apache.poi</groupId>
      <artifactId>poi-ooxml</artifactId>
      <version>5.2.5</version>
  </dependency>
  ```

- [ ] **Step 2: Buat model `model.LapRetur.java`**
  Memuat field:
  - `String noRetur`, `LocalDateTime tanggal`, `String noNota`, `String kodeBuku`, `String judul`, `int qty`, `double hargaJual`, `double totalRefund`, `String alasan`
  - Konstruktor kosong dan konstruktor berparameter, getter dan setter.

- [ ] **Step 3: Buat model `model.LapLabaKotor.java`**
  Memuat field:
  - `String noNota`, `LocalDateTime tanggal`, `String kodeBuku`, `String judul`, `int qty`, `double hargaBeli`, `double hargaJual`, `double totalModal`, `double totalOmset`, `double labaKotor`, `double marginPct`
  - Konstruktor, getter, setter, dan method helper hitung otomatis.

- [ ] **Step 4: Buat model `model.LapSupplier.java`**
  Memuat field:
  - `int idSupplier`, `String namaSupplier`, `String noTelp`, `String alamat`, `int totalFaktur`, `int totalPcs`, `double totalBiaya`
  - Konstruktor, getter, setter.

- [ ] **Step 5: Verifikasi build backend**
  Jalankan `mvn compile` untuk memastikan dependensi terunduh dan model baru terkompilasi bersih.

- [ ] **Step 6: Commit Task 1**
  ```powershell
  git add pom.xml src/main/java/model/Lap*.java
  git commit -m "feat(report): tambahkan dependensi poi-ooxml dan model dto laporan baru"
  ```

---

### Task 2: DAO Query Backend untuk 3 Laporan Baru

**Files:**
- Modify: `src/main/java/dao/ReturDAO.java`
- Modify: `src/main/java/dao/ReturDAOImpl.java`
- Modify: `src/main/java/dao/PenjualanDAO.java`
- Modify: `src/main/java/dao/PenjualanDAOImpl.java`
- Modify: `src/main/java/dao/PembelianDAO.java`
- Modify: `src/main/java/dao/PembelianDAOImpl.java`

**Interfaces:**
- Consumes: Database `retur`, `penjualan`, `detail_penjualan`, `pembelian`, `detail_pembelian`, `supplier`, `buku`
- Produces:
  - `ReturDAO.lapRetur(LocalDate dari, LocalDate sampai): List<LapRetur>`
  - `PenjualanDAO.lapLabaKotor(LocalDate dari, LocalDate sampai): List<LapLabaKotor>`
  - `PembelianDAO.lapSupplier(LocalDate dari, LocalDate sampai): List<LapSupplier>`

- [ ] **Step 1: Tambahkan method `lapRetur` pada `ReturDAO` & `ReturDAOImpl`**
  Query:
  ```sql
  SELECT r.no_retur, r.tanggal, p.no_nota, b.kode_buku, b.judul, r.qty,
         COALESCE((SELECT dp.harga_jual FROM detail_penjualan dp WHERE dp.id_penjualan = r.id_penjualan AND dp.id_buku = r.id_buku LIMIT 1), b.harga_jual) AS harga_jual,
         r.alasan
  FROM retur r
  JOIN penjualan p ON r.id_penjualan = p.id_penjualan
  JOIN buku b ON r.id_buku = b.id_buku
  WHERE DATE(r.tanggal) BETWEEN ? AND ?
  ORDER BY r.tanggal DESC, r.id_retur DESC
  ```

- [ ] **Step 2: Tambahkan method `lapLabaKotor` pada `PenjualanDAO` & `PenjualanDAOImpl`**
  Query:
  ```sql
  SELECT p.no_nota, p.tanggal, b.kode_buku, b.judul, dp.qty,
         b.harga_beli, dp.harga_jual,
         (dp.qty * b.harga_beli) AS total_modal,
         (dp.qty * dp.harga_jual) AS total_omset,
         ((dp.qty * dp.harga_jual) - (dp.qty * b.harga_beli)) AS laba_kotor
  FROM detail_penjualan dp
  JOIN penjualan p ON dp.id_penjualan = p.id_penjualan
  JOIN buku b ON dp.id_buku = b.id_buku
  WHERE DATE(p.tanggal) BETWEEN ? AND ?
  ORDER BY p.tanggal DESC, p.id_penjualan DESC
  ```

- [ ] **Step 3: Tambahkan method `lapSupplier` pada `PembelianDAO` & `PembelianDAOImpl`**
  Query:
  ```sql
  SELECT s.id_supplier, s.nama_supplier, s.no_telp, s.alamat,
         COUNT(DISTINCT pb.id_pembelian) AS total_faktur,
         COALESCE(SUM(dp.qty), 0) AS total_pcs,
         COALESCE(SUM(dp.subtotal), 0) AS total_biaya
  FROM supplier s
  LEFT JOIN pembelian pb ON s.id_supplier = pb.id_supplier AND DATE(pb.tanggal) BETWEEN ? AND ?
  LEFT JOIN detail_pembelian dp ON pb.id_pembelian = dp.id_pembelian
  GROUP BY s.id_supplier, s.nama_supplier, s.no_telp, s.alamat
  ORDER BY total_biaya DESC
  ```

- [ ] **Step 4: Verifikasi kompilasi backend DAO**
  Jalankan `mvn compile` untuk memastikan implementasi DAO bebas error.

- [ ] **Step 5: Commit Task 2**
  ```powershell
  git add src/main/java/dao/*
  git commit -m "feat(dao): query agregasi retur, laba kotor margin, dan pengadaan supplier"
  ```

---

### Task 3: Utility Export Native Microsoft Excel (`ExcelExporter.java`)

**Files:**
- Create: `src/main/java/util/ExcelExporter.java`

**Interfaces:**
- Consumes: `javax.swing.JTable`, `org.apache.poi.xssf.usermodel.*`
- Produces: `ExcelExporter.exportJTable(JTable table, String title, String periode, File outFile): void`

- [ ] **Step 1: Rancang struktur class `ExcelExporter.java`**
  - Inisialisasi `XSSFWorkbook` dan `XSSFSheet`.
  - Buat Title Banner di baris 0 & 1: Nama Toko Buku Almira, Judul Laporan, dan Periode.
  - Buat `CellStyle` untuk Header Tabel:
    - Font bold putih, background warna slate/navy (`#1E293B`).
    - Border tipis sekeliling sel.
    - Perataan teks tengah (*center*).
  - Buat `CellStyle` untuk Data Rows:
    - Kolom teks: Rata kiri.
    - Kolom kuantitas: Rata kanan dengan format number.
    - Kolom nominal rupiah: Rata kanan dengan data format currency `_([$Rp-id-ID]* #,##0_);_([$Rp-id-ID]* (#,##0);_([$Rp-id-ID]* "-"_);_(@_)` atau format `"Rp "#,##0`.
  - Baris Summary Total di bagian bawah tabel dengan font bold dan double bottom border.
  - Aktifkan `sheet.createFreezePane(0, 4)` agar header terkunci saat scrolling.
  - Lakukan auto-fit lebar kolom (`sheet.autoSizeColumn(i)`).
  - Tulis ke `FileOutputStream(outFile)`.

- [ ] **Step 2: Verifikasi kompilasi utility**
  Jalankan `mvn compile`.

- [ ] **Step 3: Commit Task 3**
  ```powershell
  git add src/main/java/util/ExcelExporter.java
  git commit -m "feat(excel): utilitas export native xlsx dengan styling dan auto-fit kolom"
  ```

---

### Task 4: Template JasperReports Formal Kop Toko & Tanda Tangan Ganda

**Files:**
- Create: `src/main/resources/reports/lap_retur.jrxml`
- Create: `src/main/resources/reports/lap_laba_kotor.jrxml`
- Create: `src/main/resources/reports/lap_supplier.jrxml`
- Modify: `src/main/resources/reports/lap_data_buku.jrxml`
- Modify: `src/main/resources/reports/lap_penjualan.jrxml`
- Modify: `src/main/resources/reports/lap_pembelian.jrxml`
- Modify: `src/main/resources/reports/lap_stok.jrxml`
- Modify: `src/main/resources/reports/lap_pendapatan.jrxml`
- Modify: `src/main/resources/reports/lap_terlaris.jrxml`
- Modify: `src/main/java/report/ReportHelper.java`

**Interfaces:**
- Consumes: Parameter `APP_NAME`, `PERIODE`, `KOTA`, `PETUGAS`, `TGL_CETAK`
- Produces: 9 template JasperReports formal siap cetak PDF dengan kop toko dan kolom TTD

- [ ] **Step 1: Update `ReportHelper.java` untuk parameter formal**
  Pastikan method `ReportHelper.show(...)` dan `ReportHelper.exportPdf(...)` menerima map parameter yang lengkap dengan default fallback jika null.

- [ ] **Step 2: Buat template `lap_retur.jrxml`**
  - Header: Kop Toko Buku Almira + Judul "Laporan Retur Penjualan".
  - Kolom: No. Retur, Tanggal, No. Nota Asal, Kode Buku, Judul Buku, Qty, Harga Satuan, Total Refund, Alasan.
  - Summary: Total Buku Diretur & Total Uang Kembali (Refund).
  - Tanda Tangan: Kolom Dibuat Oleh (Petugas) & Menyetujui (Kepala Toko).

- [ ] **Step 3: Buat template `lap_laba_kotor.jrxml`**
  - Header: Kop Toko Buku Almira + Judul "Laporan Laba Kotor & Margin Penjualan".
  - Kolom: No. Nota, Tanggal, Judul Buku, Qty, Harga Beli (Modal), Harga Jual (Omset), Laba Kotor, Margin (%).
  - Summary: Total Omset, Total Modal Pokok, Total Laba Bersih Toko.
  - Tanda Tangan Ganda.

- [ ] **Step 4: Buat template `lap_supplier.jrxml`**
  - Header: Kop Toko Buku Almira + Judul "Laporan Rekapitulasi Pengadaan Supplier".
  - Kolom: Nama Mitra Supplier, No. Telepon, Alamat, Total Faktur, Total Buku (Pcs), Total Biaya Pembelian.
  - Summary: Total Seluruh Biaya Pengadaan Buku.
  - Tanda Tangan Ganda.

- [ ] **Step 5: Standarisasi 6 template yang sudah ada**
  Tambahkan Kop Resmi Toko (alamat & kontak) dan Kolom TTD Kepala Toko & Petugas pada:
  - `lap_data_buku.jrxml`
  - `lap_penjualan.jrxml`
  - `lap_pembelian.jrxml`
  - `lap_stok.jrxml`
  - `lap_pendapatan.jrxml`
  - `lap_terlaris.jrxml`

- [ ] **Step 6: Verifikasi kompilasi laporan JasperReports**
  Jalankan `mvn test-compile` untuk memastikan sintaks XML semua 9 file `.jrxml` valid.

- [ ] **Step 7: Commit Task 4**
  ```powershell
  git add src/main/resources/reports/*.jrxml src/main/java/report/ReportHelper.java
  git commit -m "feat(report): standarisasi 9 template jasperreports dengan kop toko dan ttd ganda"
  ```

---

### Task 5: Dashboard FormLaporan UI (KPI Metric Cards, Live Table & JFreeChart Tabs, Action Bar)

**Files:**
- Modify: `src/main/java/view/FormLaporan.java`

**Interfaces:**
- Consumes: DAO methods (9 laporan), `ExcelExporter`, `ReportHelper`, JFreeChart
- Produces: Antarmuka terpadu `FormLaporan` di tab utama sistem

- [ ] **Step 1: Rombak struktur layout dan state `FormLaporan.java`**
  - Tambahkan konstanta 3 laporan baru:
    - `JENIS_RETUR = "Retur Penjualan"`
    - `JENIS_LABA_KOTOR = "Laba Kotor / Margin"`
    - `JENIS_SUPPLIER = "Pengadaan Supplier"`
  - Sediakan field komponen: `cmbJenis`, `dpDari`, `dpSampai`, `pnlKPI`, `tblPreview`, `modelPreview`, `txtCariLive`, `pnlChartContainer`, `tabbedCenter`.

- [ ] **Step 2: Bangun Strip Kartu Metrik KPI (Top Metric Cards)**
  - Card 1: `Total Data` (Jumlah transaksi/baris).
  - Card 2: `Total Volume (Pcs)` (Kuantitas buku keluar/masuk).
  - Card 3: `Total Nilai Finansial` (Omset / Pengeluaran / Refund / Laba).
  - Card 4: `Rata-rata / Margin` (Persentase margin atau rata-rata per transaksi).
  - Terapkan `NeoShadowBorder` dan font tebal neobrutalisme pada setiap kartu.

- [ ] **Step 3: Bangun Tab 1 — Live Table Preview**
  - Sediakan bar pencarian live (`txtCariLive`) dengan `DocumentListener`.
  - Pasang `JTable` dengan `DefaultTableModel` dinamis yang kolomnya otomatis menyesuaikan jenis laporan yang dipilih.
  - Format angka dan rupiah diatur rapi dan rata kanan.

- [ ] **Step 4: Bangun Tab 2 — Kurva & Grafik Visual (JFreeChart)**
  - Buat method `renderChart(String jenis, List<?> data)`:
    - Jika Penjualan/Pendapatan/Laba Kotor: Buat **Time Series / Line Chart** atau **Bar Chart** tren harian.
    - Jika Buku Terlaris: Buat **Horizontal Bar Chart** Top 10 Buku.
    - Jika Pengadaan Supplier: Buat **Bar Chart** perbandingan belanja antar supplier.
    - Jika Stok Menipis / Retur: Buat **Bar Chart** volume buku.
  - Bungkus chart dengan `ChartPanel` dan masukkan ke dalam tab kedua.

- [ ] **Step 5: Integrasikan Tombol Aksi di Bottom Bar**
  - Tombol **"Pratinjau PDF"** -> memanggil `ReportHelper.show(...)`.
  - Tombol **"Export PDF"** -> dialog `JFileChooser` lalu panggil `ReportHelper.exportPdf(...)`.
  - Tombol **"Export Excel (.xlsx)"** -> dialog `JFileChooser` lalu panggil `ExcelExporter.exportJTable(...)`.
  - Tombol **"Segarkan"** -> memuat ulang data dari database dan me-refresh kartu KPI, tabel, dan grafik.

- [ ] **Step 6: Verifikasi kompilasi dan keutuhan UI**
  Jalankan `mvn compile`.

- [ ] **Step 7: Commit Task 5**
  ```powershell
  git add src/main/java/view/FormLaporan.java
  git commit -m "feat(ui): dashboard form laporan dengan kpi cards, kurva jfreechart, dan live preview"
  ```

---

### Task 6: Verifikasi Akhir & Pengujian End-to-End

**Files:**
- Test all modified files

- [ ] **Step 1: Jalankan kompilasi penuh dan unit test**
  ```powershell
  mvn clean test
  ```
  Pastikan `BUILD SUCCESS` tanpa warning/error fatal.

- [ ] **Step 2: Verifikasi alur dashboard Form Laporan**
  - Jalankan aplikasi (`mvn exec:java -Dexec.mainClass="view.Login"`).
  - Buka menu Laporan sebagai Admin.
  - Ganti jenis laporan ke 9 pilihan yang ada, periksa perubahan kartu KPI dan tabel data.
  - Buka tab Kurva & Grafik Visual, periksa render chart JFreeChart.

- [ ] **Step 3: Verifikasi Export Excel (.xlsx)**
  - Klik tombol "Export Excel (.xlsx)", simpan file ke disk.
  - Buka file hasil export, pastikan header berwarna navy rapi, kolom angka dan rupiah terformat benar, dan freeze panes berfungsi.

- [ ] **Step 4: Verifikasi Dokumen Cetak JasperReports (PDF)**
  - Klik tombol "Pratinjau PDF", pastikan kop resmi Toko Buku Almira dan kolom tanda tangan (Dibuat Oleh & Menyetujui Kepala Toko) tampil sempurna di lembar laporan.

- [ ] **Step 5: Commit akhir dan dokumentasi**
  ```powershell
  git add .
  git commit -m "chore: finalisasi pengujian end-to-end modul laporan eksekutif, excel, dan grafik"
  ```
