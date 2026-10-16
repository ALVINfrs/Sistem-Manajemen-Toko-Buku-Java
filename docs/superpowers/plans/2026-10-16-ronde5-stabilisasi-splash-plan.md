# Ronde 5 Implementation Plan — Fix NPE Laporan, rename Barang, splash startup

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Laporan selalu terbuka tanpa dialog error, semua label campur-buku-ATK netral ("Barang"), app dibuka dengan splash tahap, lalu E2E + package + docs + review.

**Architecture:** Guard init-order di FormLaporan (tutup kelas defect tanpa ubah perilaku normal); rename murni-teks tampil (bean/SQL/jrxml-field utuh); SplashScreen JWindow + SwingWorker sebelum Login; Task 24 E2E sebagai gate akhir.

**Tech Stack:** Java 11, Swing, JasperReports (7 jrxml), MySQL/MariaDB, Maven portable.

**Spec:** `docs/superpowers/specs/2026-10-16-ronde5-stabilisasi-splash-design.md` (induk: `docs/superpowers/specs/2026-09-23-ronde4-atk-design.md`)

## Global Constraints

- Java 11, UTF-8. DILARANG tambah dependency. DILARANG ubah file di luar daftar Files tiap task.
- PowerShell 5.1 (tanpa `&&`; set workdir via param). Maven `C:\Users\alvin\tools\apache-maven-3.9.9\bin\mvn.cmd`. Display tersedia. DB `root`/"" (`db_toko_buku`, ber-seed; bila mati: `Start-Process C:\xampp\mysql\bin\mysqld.exe`).
- Probe di luar repo (`%TEMP%\opencode`), classpath FULL (`target/classes` + output `dependency:build-classpath` + dir probe), GUI akhiri `System.exit(0)`, file probe via write-tool lalu strip BOM bila perlu, hapus setelah selesai, prefix TEST-R5 + cleanup + assert COUNT 0. Ruling R4-3: set `Sesi.role`/`Sesi.userLogin` bila instantiate form.
- String rename verbatim tabel spec §2. `field` jrxml + bean + SQL TIDAK diubah.
- Neobrutalism untuk SplashScreen (arc 0, `NeoShadowBorder`, Ikonli MaterialDesign2).

## Review Focus

- Event listener prematur lain di FormLaporan (DatePicker/txtCariLive saat konstruksi) — harapannya tidak ada dialog error dalam kondisi apapun saat buka Laporan. (Task 21 probe watchdog.)
- Label "Buku" tersisa di konteks campur yang terlewat grep — harapannya nol (Task 22 grep `(?i)\bBuku\b` + review manual tiap hit). (Task 22.)
- Splash menutupi kegagalan DB tanpa hang — harapannya selalu ada jalan keluar ≤15 dtk (tombol Tutup). (Task 23, hanya jalur sukses di-probe; jalur gagal dibuktikan via review kode.)
- Total lebar kolom jrxml retur setelah rename — harapannya teks tidak terpotong (lebar kolom tak berubah, hanya teks; "Nama Barang" ≤ "Judul Buku"). (Task 22, mvn test fill.)
- POS campur + diskon persen+nominal masih benar setelah rename (rename tak sentuh logika) — harapannya total = round(subtotal*(100-persen)/100) − nominal. (Task 24 E2E.)

---

### Task 21: Fix NPE `modelPreview` FormLaporan

**Files:**
- Modify: `src/main/java/view/FormLaporan.java`
- Test: probe `%TEMP%\opencode\ProbeT21.java` (buang setelah hijau)

**Interfaces:**
- Consumes: `muatData()` (~397), `filterTabelLive()` (~523), ctor (~128-148), `buildHeaderAndFilter()` (~150-253).
- Produces: `muatData`/`filterTabelLive` no-op aman saat tabel belum dibangun (dipakai Task 24 E2E).

- [ ] **Step 1: Tulis probe RED** — `ProbeT21.java`: set `Sesi.role="admin"` + fake `User`; `new FormLaporan()` di EDT; via refleksi set field `modelPreview=null`; pasang watchdog `Timer` 500ms yang merekam `JDialog` tampil; panggil `muatData()` via refleksi (`setAccessible`); assert TIDAK ada dialog + assert `tblPreview.getRowCount()` tetap. Watchdog juga dipakai untuk upaya repro pemicu asli: klik programmatic tidak perlu — cukup catat bila dialog muncul saat konstruksi normal.
- [ ] **Step 2: Jalankan probe, pastikan RED** — Run: `javac/java -cp <FULL> ProbeT21`. Expected: FAIL — dialog "Gagal memuat data laporan … modelPreview … null" tercatat watchdog (atau AssertionError rowCount).
- [ ] **Step 3: Implementasikan guard** — di `FormLaporan.java`: tambah field `private boolean sedangInit = true;` set `true` di awal ctor, `false` di akhir ctor (setelah `muatData()` baris ~147). Awal `muatData()`: `if (sedangInit && (modelPreview == null || tblPreview == null)) return;` — SEDERHANA alternatif yang diterima: `if (modelPreview == null || tblPreview == null) return;` tanpa flag (pilih SATU, catat di laporan). Awal `filterTabelLive()`: guard sama (`if (modelPreview == null) return;`). NOL ubah lain.
- [ ] **Step 4: Jalankan probe, pastikan GREEN** — Expected: `PROBE-T21-OK`, tanpa dialog. Lalu `mvn test` Expected: `Tests run: 7, Failures: 0, Errors: 0` + BUILD SUCCESS.
- [ ] **Step 5: Commit** — `git add src/main/java/view/FormLaporan.java` + `git commit -m "fix(r5): guard init-order muatData FormLaporan"`.

### Task 22: Rename "Buku" → "Barang" konteks campur (gaya C)

**Files:**
- Modify: `src/main/java/view/FormLaporan.java` (konstanta `JENIS_BUKU`, 4 header "Judul Buku", 2 "Kode Buku", KPI "TOTAL VOLUME BUKU", komentar chart), `src/main/java/view/FormPenjualan.java` (label "Cari Buku"), `src/main/java/view/FormPembelian.java` (label "Cari Buku di Katalog:"), `src/main/java/view/FormRetur.java` (2 header tabel), `src/main/resources/reports/lap_data_buku.jrxml` (judul + kolom + summary), `lap_pendapatan.jrxml` + `lap_laba_kotor.jrxml` (kolom "Judul Buku"), `lap_retur.jrxml` (judul + 2 kolom).
- Test: `mvn test` (7 fill) + probe `%TEMP%\opencode\ProbeT22.java`.

**Interfaces:**
- Consumes: string lama di Files (lokasi di spec §2 + grep `Cari Buku|Judul Buku|Kode Buku|MASTER DATA BUKU|RETUR PENJUALAN BUKU|VOLUME BUKU`).
- Produces: teks final (Task 24 E2E + screenshot user).

- [ ] **Step 1: Tulis probe RED** — `ProbeT22.java`: instantiate `FormLaporan` (admin), `FormPenjualan`, `FormPembelian`, `FormRetur` (set `Sesi` dulu); via refleksi baca `cmbJenis` item + `tblPreview` column names setelah pilih tiap jenis; assert: combo memuat "Data Master Barang" dan TIDAK memuat "Data Master Buku"; kolom preview memuat "Nama Barang", TIDAK ada header mengandung "Buku". (Untuk jrxml: assert via `mvn test` fill + grep file.)
- [ ] **Step 2: Jalankan, pastikan RED** — Expected: FAIL pada assert "Data Master Barang" (belum ada).
- [ ] **Step 3: Terapkan rename** — string exact tabel spec §2 (contoh: `JENIS_BUKU = "Data Master Barang"`; header `"Judul Buku"` → `"Nama Barang"`; `"Kode Buku"` → `"Kode"`; `"TOTAL VOLUME BUKU"` → `"TOTAL VOLUME BARANG"`; jrxml `LAPORAN MASTER DATA BUKU` → `LAPORAN MASTER DATA BARANG`, `LAPORAN RETUR PENJUALAN BUKU` → `LAPORAN RETUR PENJUALAN`, `TOTAL MASTER BUKU:` → `TOTAL MASTER BARANG:`). Geometri kolom jrxml TETAP ("Nama Barang" 10 char ≤ "Judul Buku" 10 char). Setelah edit, grep `(?i)buku` di 4 jrxml + 4 file java — sisa hit yang valid HANYA: nilai data/kode (`BK-`, `b.jenis`, `getJenis`, field `judul`/`kodeBuku`, Wilkinson kata "Buku" pada baris ATK? tidak), teks murni-buku (FormBuku tak tersentuh), dan `filterTabelLive` generik. NILAI `filter` `"Buku"`/`"ATK"` dan `labelJenis` fallback `"Buku"` TIDAK diubah (nilai data, bukan label).
- [ ] **Step 4: Verifikasi GREEN** — probe `PROBE-T22-OK`; `mvn test` 7/7 BUILD SUCCESS; cleanup TEST-R5=0.
- [ ] **Step 5: Commit** — `git commit -m "feat(r5): rename label campur Buku-ATK jadi Barang"`.

### Task 23: SplashScreen startup detail per tahap

**Files:**
- Create: `src/main/java/view/SplashScreen.java`
- Modify: `src/main/java/view/Login.java` (method `main` saja)
- Test: probe `%TEMP%\opencode\ProbeT23.java`.

**Interfaces:**
- Consumes: `util.LogoUtil.icon(int)->Icon`, `util.NeoBrutalTheme` (+`NeoShadowBorder`), `koneksi.Koneksi.getConnection()`, `dao.KategoriDAO.getAll()`, Ikonli `FontIcon.of`, `view.Login` ctor.
- Produces: `public SplashScreen(Runnable onDone)` (dipakai `Login.main`; Task 24 E2E).

- [ ] **Step 1: Tulis probe RED** — `ProbeT23.java`: di EDT `new SplashScreen(() -> flag.set(true))`; watchdog 15 dtk; assert: splash visible, `lblTahap` teks berubah (poll 3x), akhirnya `flag==true` + splash disposed. Expected RED: `cannot find symbol: class SplashScreen` (compile FAIL).
- [ ] **Step 2: Pastikan RED** — `javac` gagal. (Catat output.)
- [ ] **Step 3: Implementasi `view.SplashScreen extends JWindow`** — ctor `public SplashScreen(Runnable onDone)` (Objects.requireNonNull): layout neobrutalism (`NeoShadowBorder`, bg `NeoBrutalTheme.BG`, kartu `SURFACE`): logo `LogoUtil.icon(120)` + nama `AppConfig.APP_NAME`, `JProgressBar` (0-5), `lblTahap`, `lblStatus`, `btnTutup` (hidden; teks "Tutup" → `System.exit(1)`). `SwingWorker<Void,String>`: tahap1 setProgress(1,"Menyiapkan tampilan…")+sleep 300; tahap2 "Menghubungkan database…" (`try (Connection c = Koneksi.getConnection(); PreparedStatement ps = c.prepareStatement("SELECT 1")) ps.executeQuery()`); tahap3 "Memuat data master…" (`new KategoriDAOImpl().getAll().size()`); tahap4 "Menyiapkan ikon…" (`FontIcon.of(MaterialDesignS.STORE, 16, Color.BLACK)`); tahap5 "Siap." + sleep 300 → `done()`: bila sukses `dispose(); onDone.run();`, bila exception `lblStatus` merah "Database tidak terhubung. Hidupkan MySQL lalu ulangi." + `btnTutup` visible. Update UI via `setProgress`/`publish-process` atau `SwingUtilities.invokeLater` (pilih satu). `setLocationRelativeTo(null)`, `setVisible(true)` + `worker.execute()` di akhir ctor.
- [ ] **Step 4: Wire `Login.main`** — `NeoBrutalTheme.apply();` tetap baris pertama; ganti `invokeLater(() -> new Login()...)` menjadi `new SplashScreen(() -> SwingUtilities.invokeLater(() -> new Login().setVisible(true)));` (+ `setVisible(true)` splash bila belum di ctor). NOL ubah lain di `Login.java`.
- [ ] **Step 5: Verifikasi GREEN** — `mvn -q compile` sukses; probe `PROBE-T23-OK` (onDone ≤15 dtk, disposed); `mvn test` 7/7.
- [ ] **Step 6: Commit** — `git commit -m "feat(r5): splash startup detail per tahap"`.

### Task 24: E2E ronde 4+5 + package + docs + review (pengganti Task 20)

**Files:**
- Modify: `README.md`, `TESTING.md`
- Test: probe `%TEMP%\opencode\ProbeT24.java` + `mvn package` + smoke-run JAR.

**Interfaces:**
- Consumes: semua kontrak Task 17-19 + 21-23.

- [ ] **Step 1: Probe E2E** — skenario berurutan (prefix `TEST-R5`, cleanup + COUNT 0 tiap akhir): (a) POS campur: cart 2×BK + 2 pak ATK, persen 10 + nominal 5000 → `hitungTotal`/`validasiDiskon` + `saveWithDetail` → assert `diskon`=round+nominal, satuan ATK di struk; (b) Pembelian: tambah stok ATK-001 via `PembelianDAO.saveWithDetail` → assert stok bertambah; (c) Retur 1 qty nota (a) → assert stok balik + refund; (d) `FormLaporan` tiap 7 jenis `muatData` tanpa dialog (watchdog) + filter ATK/Buku; (e) `FormDashboard` instantiate tanpa exception; (f) Splash `onDone` (reuse pola T23). Expected: `PROBE-T24-OK`.
- [ ] **Step 2: `mvn package`** — Expected: BUILD SUCCESS + `ReportCompilationTest` 7/7. Catat nama JAR.
- [ ] **Step 3: Smoke-run JAR** — `java -jar target/*.jar` background 15 dtk → assert window Login tampil (`APP-ALIVE-15S:True`) → kill proses. DB harus bersih TEST-R5=0 setelahnya.
- [ ] **Step 4: Docs** — `README.md` + `TESTING.md`: hapus sisa jejak "Member" bila ada; tambah seksi ATK (jenis+satuan), diskon persen+nominal, filter Jenis laporan, splash startup; update akun/angka (buku 18 = 12+6, 7 laporan).
- [ ] **Step 5: Commit docs** — `git commit -m "docs(r5): update README/TESTING ATK + laporan 7 jenis + splash"`.
- [ ] **Step 6: Final review** — coba dispatch reviewer fresh via subagent (model paling capable; paket: merge-base `ad514a0`..HEAD + plan + spec + ledger `Ruling:`); bila dispatch gagal, self-review مقابل `requesting-code-review/code-reviewer.md` + tulis `Final review: self-review (no subagent tool)` di ledger. Temuan Critical/Important → SATU fix pass (tiap fix RED→GREEN + suite hijau); Minor → ledger `Final: minor (deferred)`.
- [ ] **Step 7: Laporan akhir** — tulis `.superpowers/sdd/2026-09-23-ronde4-atk-plan/task-24-report.md` (atau workspace ronde 5 bila dibuat) + update ledger.
