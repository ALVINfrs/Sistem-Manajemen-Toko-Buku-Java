# Spesifikasi Desain: Modernisasi Modul Laporan, Export Excel Native, Kurva Grafik Visual & Dokumen Cetak Formal

**Tanggal:** 2026-09-23  
**Status:** Approved  
**Topik:** Transformasi Modul Laporan Toko Buku Almira menjadi Dashboard Manajemen Eksekutif, Penambahan 3 Laporan Baru (Retur, Laba Kotor/Margin, Pengadaan Supplier), Standardisasi Template JasperReports dengan Kop Toko & TTD Ganda, Export Native Microsoft Excel (.xlsx), serta Kurva & Grafik Visual JFreeChart.

---

## 1. Latar Belakang & Tujuan

Modul laporan sebelumnya masih memiliki antarmuka yang sangat minim (hanya filter sederhana dan teks instruksi statis tanpa pratinjau tabel di layar). Selain itu, format dokumen cetak JasperReports masih polos tanpa identitas resmi toko (kop surat formal) maupun kolom tanda tangan pengesahan (Kepala Toko & Kasir/Admin). Fitur pengembalian buku (retur) dan analisis keuntungan margin belum memiliki laporan tersendiri, dan sistem belum memiliki fitur ekspor ke Microsoft Excel (.xlsx) yang rapi.

### Tujuan Utama:
1. **Dashboard UI Interaktif di `FormLaporan.java`**:
   - Kartu metrik KPI (Total Data, Total Kuantitas Pcs, Total Rupiah/Omset/Laba).
   - Tab 1: Tabel Live Data Preview dengan filter pencarian instan di layar.
   - Tab 2: Kurva & Grafik Visual interaktif (JFreeChart) menampilkan tren transaksi, omset, atau diagram batang performa buku.
2. **Standardisasi Seluruh 9 Dokumen Cetak JasperReports (`.jrxml`)**:
   - Kop Resmi Toko Buku Almira dengan alamat, kontak, dan garis pemisah ganda.
   - Desain tabel modern dengan header rapi dan pemisah baris yang bersih.
   - Kolom Tanda Tangan Ganda: Sisi kiri "Dibuat Oleh: Kasir/Admin" dan sisi kanan "Menyetujui: Kepala Toko" lengkap dengan tanggal dan kota.
3. **Penambahan 3 Jenis Laporan Baru**:
   - **Laporan Retur Penjualan**: Rekap buku kembali, alasan kerusakan/cacat, dan nominal refund dana kasir.
   - **Laporan Laba Kotor / Margin Keuntungan**: Analisis selisih harga jual vs harga modal beli per transaksi.
   - **Laporan Pengadaan Supplier**: Rekapitulasi pembelian per supplier (volume barang & total tagihan).
4. **Export Native Microsoft Excel (`.xlsx`) via Apache POI**:
   - Ekspor data bersih tanpa merge cell yang berantakan, header berlatar warna navy/slate, font bold, format angka mata uang rupiah (`Rp #,##0`), auto-fit lebar kolom, dan freeze panes baris judul.

---

## 2. Arsitektur Data & Model Baru

### 2.1 DTO (Data Transfer Objects) Laporan Baru

Untuk laporan baru yang memerlukan agregasi gabungan beberapa tabel, dibuat DTO yang ringkas:

1. **`model.LapRetur`**:
   - `String noRetur`
   - `LocalDateTime tanggal`
   - `String noNota`
   - `String kodeBuku`
   - `String judul`
   - `int qty`
   - `double hargaJual`
   - `double totalRefund`
   - `String alasan`

2. **`model.LapLabaKotor`**:
   - `String noNota`
   - `LocalDateTime tanggal`
   - `String kodeBuku`
   - `String judul`
   - `int qty`
   - `double hargaBeli` (Modal)
   - `double hargaJual` (Omset)
   - `double totalModal` (Harga Beli x Qty)
   - `double totalOmset` (Harga Jual x Qty)
   - `double labaKotor` (Total Omset - Total Modal)
   - `double marginPct` ((Laba Kotor / Total Omset) * 100)

3. **`model.LapSupplier`**:
   - `int idSupplier`
   - `String namaSupplier`
   - `String noTelp`
   - `String alamat`
   - `int totalFaktur`
   - `int totalPcs`
   - `double totalBiaya`

### 2.2 DAO Enhancements

1. **`ReturDAO` & `ReturDAOImpl`**:
   - `List<LapRetur> lapRetur(LocalDate dari, LocalDate sampai)`: Query data retur dengan rentang tanggal.
2. **`PenjualanDAO` & `PenjualanDAOImpl`**:
   - `List<LapLabaKotor> lapLabaKotor(LocalDate dari, LocalDate sampai)`: Query margin laba per item buku terjual.
3. **`PembelianDAO` & `PembelianDAOImpl`**:
   - `List<LapSupplier> lapSupplier(LocalDate dari, LocalDate sampai)`: Query agregasi pengadaan per mitra penerbit/supplier.

---

## 3. Standardisasi Dokumen Cetak JasperReports (`.jrxml`)

Semua 9 laporan menggunakan struktur layout formal:

```text
+-------------------------------------------------------------------+
|                        TOKO BUKU ALMIRA                           |
|         Jl. Merdeka No. 45 - Telp: 0812-3456-7890 - Jakarta       |
|===================================================================|
|                        [JUDUL LAPORAN]                            |
|             Periode: [Tanggal Mulai] s.d. [Tanggal Akhir]         |
+-------------------------------------------------------------------+
| No | Kode | Judul / Deskripsi | Qty | Harga Satuan | Subtotal Rp  |
|----+------+-------------------+-----+--------------+--------------|
| 1  | ...  | ...               | ... | ...          | ...          |
| 2  | ...  | ...               | ... | ...          | ...          |
+-------------------------------------------------------------------+
|                                        TOTAL KESELURUHAN: Rp ...  |
+-------------------------------------------------------------------+
|                                                                   |
|   Dibuat Oleh,                           Jakarta, [Tanggal Cetak] |
|   Petugas Kasir / Admin                  Menyetujui,              |
|                                          Kepala Toko              |
|                                                                   |
|   ( ____________________ )               ( ____________________ ) |
+-------------------------------------------------------------------+
| Halaman 1 dari 1                        Dicetak pada: dd-MM-yyyy  |
+-------------------------------------------------------------------+
```

Daftar 9 file `.jrxml`:
1. `/reports/lap_data_buku.jrxml`
2. `/reports/lap_penjualan.jrxml`
3. `/reports/lap_pembelian.jrxml`
4. `/reports/lap_stok.jrxml`
5. `/reports/lap_pendapatan.jrxml`
6. `/reports/lap_terlaris.jrxml`
7. `/reports/lap_retur.jrxml` *(Baru)*
8. `/reports/lap_laba_kotor.jrxml` *(Baru)*
9. `/reports/lap_supplier.jrxml` *(Baru)*

Parameter umum yang dipass:
- `APP_NAME`: "Toko Buku Almira"
- `PERIODE`: String rentang tanggal
- `KOTA`: "Jakarta"
- `PETUGAS`: Nama pengguna yang sedang login (`Sesi.userLogin.getNamaLengkap()`)
- `TGL_CETAK`: Tanggal hari ini dalam format `dd MMMM yyyy`

---

## 4. Engine Export Microsoft Excel (`.xlsx`)

Penambahan dependency `org.apache.poi:poi-ooxml:5.2.5` pada `pom.xml`.
Dibuat utility class `util.ExcelExporter`:

```java
public class ExcelExporter {
    public static void exportJTable(JTable table, String title, String periode, File outFile) throws IOException;
}
```

Fitur-fitur file Excel yang dihasilkan:
- **Sheet Title & Metadata**: Judul laporan dan periode di baris atas.
- **Header Table**: Background warna navy (`#1E293B`), teks putih tebal, rata tengah.
- **Data Rows**:
  - Kolom teks: rata kiri.
  - Kolom angka/qty: format integer rata kanan.
  - Kolom nominal rupiah: format currency Excel asli (`Rp #,##0`) sehingga bisa langsung dijumlahkan dengan rumus `=SUM(...)`.
  - Border sel halus warna abu-abu.
- **Freeze Panes**: Baris header dikunci (*freeze*), sehingga judul kolom tetap terlihat saat discroll ke bawah.
- **Auto-fit Column Width**: Lebar setiap kolom otomatis menyesuaikan panjang karakter data.

---

## 5. Antarmuka Pengguna (`FormLaporan.java`)

Layout `FormLaporan` ditata dengan gaya neobrutalisme yang kaya informasi:

```text
+--------------------------------------------------------------------------------+
| [Judul: Pusat Laporan & Analisis Bisnis]                                       |
| [Pilihan Jenis Laporan (9 Jenis)] [Dari: DatePicker] [Sampai: DatePicker]      |
| [Tombol: Terapkan Filter]                                                      |
+--------------------------------------------------------------------------------+
| [ Card KPI 1: Total Data ]  [ Card KPI 2: Total Pcs ]  [ Card KPI 3: Total Rp ]|
+--------------------------------------------------------------------------------+
|  [Tab 1: Tabel Data Preview]    |    [Tab 2: Kurva & Grafik Tren Visual]       |
|  ----------------------------------------------------------------------------  |
|  Filter Kata Kunci: [___________]                                              |
|  +--------------------------------------------------------------------------+  |
|  | No | Tanggal | Referensi | Item / Keterangan | Qty | Nilai Rp | Catatan      |  |
|  |----+---------+-----------+-------------------+-----+----------+--------------|  |
|  | 1  | ...     | ...       | ...               | ... | ...      | ...          |  |
|  +--------------------------------------------------------------------------+  |
+--------------------------------------------------------------------------------+
| [Pratinjau Cetak (Jasper)]  [Export PDF]  [Export Excel (.xlsx)]   [Segarkan]  |
+--------------------------------------------------------------------------------+
```

### Kurva & Grafik Visual (JFreeChart di Tab 2):
1. **Laporan Penjualan / Pendapatan / Laba Kotor**: Kurva garis (*Line Chart*) atau batang (*Bar Chart*) tren harian (Tanggal vs Nilai Rupiah).
2. **Laporan Buku Terlaris**: Grafik batang horizontal (*Horizontal Bar Chart*) Top 10 Buku Terlaris.
3. **Laporan Pembelian & Supplier**: Diagram batang pengeluaran per mitra supplier.
4. **Laporan Retur**: Diagram batang perbandingan retur buku.

---

## 6. Penanganan Hak Akses & Error Handling

1. **Hak Akses**:
   - `Admin`: Memiliki akses penuh ke seluruh 9 jenis laporan dan seluruh grafik toko.
   - `Kasir`: Hanya dapat mengakses laporan operasional (Penjualan miliknya dan Retur Penjualan), sedangkan laporan Laba Kotor dan Pembelian disembunyikan.
2. **Error Handling**:
   - Validasi tanggal jika tanggal awal lebih besar dari tanggal akhir.
   - Notifikasi ramah jika data yang dicari bernilai kosong (0 baris).
   - Penanganan error I/O saat ekspor Excel jika file tujuan sedang dikunci oleh program lain (misal Microsoft Excel).

---

## 7. Rencana Pengujian

1. **Unit & Build Testing**:
   - `mvn clean test` untuk memastikan semua dependensi POI dan JasperReports terkompilasi bersih.
2. **Verifikasi Visual**:
   - Buka `FormLaporan`, ubah-ubah jenis laporan, pastikan kartu KPI langsung bereaksi.
   - Buka Tab Kurva Grafik, pastikan grafik JFreeChart ter-render tajam dan dinamis.
   - Buka Pratinjau Cetak, periksa Kop Toko dan kolom tanda tangan di dokumen PDF.
   - Lakukan Export Excel (.xlsx), buka filenya di spreadsheet, pastikan format angka dan header rapi.
