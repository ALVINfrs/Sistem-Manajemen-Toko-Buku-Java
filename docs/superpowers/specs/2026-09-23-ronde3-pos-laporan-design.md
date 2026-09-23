# Spec Desain Ronde 3 — POS beneran, laporan hidup, menu bernyawa

Status: disetujui user ("gas eksekusi"). Acuan: `docs/prd.md` + `docs/design.md` + plan
`docs/superpowers/plans/2026-09-23-toko-buku-almira-implementation.md`.
Aplikasi: Sistem Informasi Toko Buku, CV Almira Jaya Abadi. Stock: tidak ada yang TBD.

## 1. Schema (deviasi PRD yang disetujui — ALTER + update file SQL)

- `penjualan` tambah `metode_bayar VARCHAR(20) NOT NULL DEFAULT 'Tunai'`
  (nilai: `Tunai|Transfer|QRIS`) dan `diskon DECIMAL(12,2) NOT NULL DEFAULT 0`
  (total gabungan: diskon member + potongan manual).
- `kategori` tambah `deskripsi VARCHAR(255) NULL`.
- Berlaku untuk: `database/schema.sql` (definisi final), DB dev (ALTER),
  `database/seed_demo.sql` (sesuaikan baris transaksi).
- Migrasi DB user: file `database/migrasi_ronde3.sql` (ALTER TABLE ... ADD COLUMN
  bila belum ada, aman di-run ulang).

## 2. POS checkout beneran (view/FormPenjualan.java, rewrite alur bayar)

- Combo metode: Tunai / Transfer / QRIS.
- Tunai: field Bayar wajib angka dan `bayar >= total`; kembalian = bayar - total.
- Transfer/QRIS: Bayar otomatis = Total, kembalian 0, tombol simpan langsung aktif.
- Member dipilih: subtotal dihitung; bila total-qty >= 50 ATAU subtotal >= 750000
  maka diskon member 5% aktif (tampilkan status: "Diskon member aktif" atau
  "Belanja RpX / Y buku lagi untuk diskon"). Bila tidak memenuhi syarat: tanpa diskon.
- Potongan manual: input Rp, `0 <= potongan <= (subtotal - diskon_member)`;
  bebas Admin maupun Kasir, tanpa batas.
- Total = subtotal - diskon_member - potongan. Simpan memakai `saveWithDetail`
  yang diperluas (kolom metode_bayar + diskon ikut header).
- Gagal validasi: popup spesifik ("Uang bayar kurang", "Potongan melebihi sisa", ...),
  tidak ada lagi jebakan Bayar-kosong (tombol nonaktif sampai valid untuk non-tunai
  otomatis terisi).

## 3. Struk lengkap + cetak + riwayat (view/StrukDialog.java, view/FormRiwayat.java)

- Struk: APP_NAME, no nota, tanggal-jam, nama kasir (user login), member (atau -),
  tabel item (judul, qty, harga, subtotal), subtotal, diskon member, potongan manual,
  total, bayar, kembalian, metode bayar, "Terima kasih sudah berbelanja".
- Tombol Cetak: print beneran (`JasperPrintManager.printReport` atas print
  penjualan, atau `PrinterJob` untuk dialog; pilih yang terbukti jalan, catat).
- FormRiwayat ("Riwayat Nota", semua role yang boleh jual): tabel semua penjualan
  terbaru-dulu + filter tanggal + klik baris = StrukDialog isi ulang (cetak ulang).
  Terdaftar di MenuUtama (ikon `HISTORY`-sejenis, terverifikasi javap).

## 4. Retur, Member, Pembelian, Kategori

- Retur: daftar nota langsung tampil (50 terbaru) saat dibuka; field cari = filter live.
  Sisa logika (sisa = terjual - diretur) tetap.
- Member: diskon 5% bersyarat (lihat §2); info member tampil di POS;
  FormMember tambah kolom jumlah transaksi (COUNT penjualan per member, tanpa ubah schema).
- Pembelian: panel hint "Cara pakai: 1) pilih supplier 2) tambah item + qty + harga
  3) simpan — stok bertambah"; validasi pesan jelas.
- Kategori: field deskripsi (wajib? TIDAK — opsional, boleh kosong) + kolom jumlah buku
  (COUNT) + search live. CRUD tetap.

## 5. Menu bernyawa (view/MenuUtama.java)

- Sidebar seksi berlabel (Master / Transaksi / Laporan) + tombol aktif highlight.
- Kartu profil bawah: ikon avatar + nama + role + jam hidup (Timer 1 dtk, HH:mm:ss)
  + tanggal (`dd MMM yyyy`, locale id).
- Klik kartu: dialog Profil Saya (ganti nama_lengkap + ganti password dengan
  konfirmasi; username dikunci; hash SHA-256).
- Tambah tombol Dashboard (buka default saat login) + Riwayat + User tetap Admin-only.

## 6. Laporan cantik + chart (10 laporan)

- Template seragam: header APP_NAME + judul + periode + tanggal cetak; header tabel
  hitam/putih; baris belang; box total; footer halaman.
- Baru: Member (belanja per member per periode), Retur (daftar + alasan),
  Laba per Kategori (+ bar chart), Omzet per Kasir (+ bar chart, Admin saja).
- Chart di: Penjualan (omzet/hari), Pendapatan (laba/hari), Terlaris (top 10),
  Laba per Kategori, Omzet per Kasir. Chart ikut print + export (JasperReports native).
- Data chart dari DAO (`Map`/bean ringan, mis. `LapGrafik(label, nilai)`).

## 7. Dashboard (view/FormDashboard.java, konten default)

- Kartu angka: omzet hari ini, transaksi hari ini, item terjual hari ini, stok menipis
  (<= STOK_MENIPIS).
- Grafik batang: omzet 7 hari terakhir + top 5 buku terlaris bulan ini (JFreeChart
  tertanam via ChartPanel).
- Daftar stok menipis (klik = buka FormBuku). Kasir melihat dashboard sama
  (angka global toko — disetujui implisit; bila user protes, kunci belakangan).

## 8. Export Excel rapi (util/ExcelExporter.java + Apache POI)

- Tambah `org.apache.poi:poi-ooxml:5.2.5` (Java 11 OK) + `org.jfree:jfreechart:1.5.4`
  eksplisit (dashboard; versi final ikut dependency-tree JasperReports bila beda,
  catat di laporan).
- `ExcelExporter.export(namaSheet, header[], rows[][], totals, File)`:
  header bold + fill hitam font putih, border tipis semua sel, angka format `#,##0`,
  kolom autosize, baris TOTAL bold. Dipakai FormLaporan (tombol "Export Excel",
  warna success) untuk SEMUA 10 jenis (data = buildData yang sama dengan viewer).
- Format XLSX via `XSSFWorkbook`; JFileChooser filter `.xlsx`.

## 9. Role & proteksi (tetap + tambahan)

- Matriks lama tetap. Tambahan: Dashboard + Riwayat ikut hak transaksi
  (Kasir boleh; Riwayat Kasir difilter miliknya — reuse `lapPenjualanByUser`
  untuk daftar + detail tetap bisa dibuka dari barisnya).
- Profil Saya: semua role (ubah data sendiri). FormUser: Admin saja (tetap).
- Register: Kasir-only (tetap).

## 10. Testing & DoD ronde 3

- Probe per task (pattern ronde 1-2): format nota, syarat diskon (qty/nominal/batas),
  metode non-tunai auto, cetak (mock printer bila headless — minimal dialog+job
  terbentuk tanpa exception), riwayat filter, profil update, dashboard angka,
  compile+fill 10 jrxml, export xlsx terbuka (baca balik via POI).
- E2E ulang Task 16: skenario ronde 1 + skenario baru (POS 3 metode, diskon,
  cetak-mock, riwayat, profil, dashboard, excel) + `mvn package` + TESTING.md update.
- DB E2E: prefix TEST-R3, cleanup FK-aman + assert COUNT akhir 0 (pelajaran ronde 1).

## Self-review spec (2026-09-23)

1. Placeholder: tidak ada TBD/TODO; semua angka eksplisit (50 buku, 750000, 5%, 50 nota, 7 hari, top 5/10).
2. Konsistensi: diskon member hanya di POS (laporan laba per-item tidak dipotong nota-level — dinyatakan eksplisit, bukan kontradiksi); `diskon` = gabungan cocok dengan struk rincian; Riwayat Kasir difilter tapi detail baris miliknya tetap valid.
3. Scope: 5 task (12-16) sekuensial; dashboard chart butuh JFreeChart eksplisit — dicakup §8.
4. Ambiguitas: "potongan <= sisa" didefinisikan (§2); username profil dikunci (§5); dashboard kasir global (§7) — eksplisit semua.
