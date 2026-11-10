# Spec Ronde 6 — Redesign Auth: Splash, Login, Register

Status: disetujui user via chat (dropdown 2026-11-09).
Base branch: `master` @ `c550438` (ronde 4+5 sudah masuk). Branch kerja: `impl/ronde6-auth`.
Induk konteks: `docs/superpowers/specs/2026-09-23-ronde4-atk-design.md`,
`docs/superpowers/specs/2026-10-16-ronde5-stabilisasi-splash-design.md`.

## 1. Latar & tujuan

Splash/Login/Register sekarang terasa "neobrutalism tapi jadul": semua permukaan
sama (cream datar, border hitam seragam, shadow di semua elemen), tanpa motion,
dan foto rak buku berwarna hangat yang bertabrakan dengan palet. User minta
redesign **modern + neobrutalism + animatif + colorful**, dengan **logo toko
dipertahankan**. Register dipilih sebagai **halaman penuh terpisah** (masih satu
window, transisi curtain), foto rak buku **dibuang**.

Tujuan: tiga layar pertama terasa hidup dan konsisten, tanpa mengubah alur
auth/DA/validasi yang sudah ada.

## 2. Goals

- Splash 900×560: logo AJB, wordmark, ticker berjalan, 5 blok progress,
  bentuk geometris mengambang, exit curtain wipe ke atas.
- Login 1080×680 split-screen: panel brand maroon + form cream, entrance
  animation, focus/press/error micro-interaction, password eye toggle.
- Register halaman penuh: panel brand teal + form cream, transisi curtain
  dari/ke Login, validasi & DAO sama seperti `RegisterDialog` lama.
- `util/Motion.java` reusable (easing + Timer + shake + delay), tanpa dependency.
- Palet tetap keluarga palet lama + warna logo (maroon/teal).
- Logo `logo_ajb_transparent.png` dipakai apa adanya (tidak diputar/distorsi).

## 3. Non-goals

- Tidak menyentuh modul di luar auth (MenuUtama, POS, laporan, master data).
- Tidak mengubah skema DB, seed, DAO, atau aturan validasi.
- Tidak menambah dependency Maven, file font, atau library animasi.
- Tidak mengubah `Login.main` sebagai entry point selain wiring splash.
- Tidak membuat halaman lupa password/remember me (tidak diminta).

## 4. Anti-goals (gagal walau teknis jalan)

- Animasi memblokir EDT / bikin UI freeze / CPU tinggi terus-menerus.
- Pindah Login↔Register berkedip, blank, atau stuck.
- Logo terpotong, gepeng, diputar, atau berubah warna.
- Warna kembali monoton atau muncul rounded corner/gradient.
- Validasi auth berubah diam-diam (username unik, password ≥4, konfirmasi sama,
  role Kasir, hash SHA-256).

## 5. Constraints

- Java 11 source/target, Swing, FlatLaf, Ikonli MaterialDesign2.
- Tanpa dependency baru. Font hanya font sistem.
- Semua animasi pakai `javax.swing.Timer` di EDT; pekerjaan DB tetap di
  `SwingWorker` (splash) atau aksi tombol (login/register).
- Timer komponen harus berhenti saat komponen dilepas (`removeNotify`) atau
  splash ditutup, supaya tidak ada timer hantu.
- File baru mengikuti gaya package `view`/`util` yang sudah ada (flat).
- Warna exact (lihat §6). Tidak ada warna baru selain dua warna brand logo.
- Build: `C:\Users\alvin\tools\apache-maven-3.9.9\bin\mvn.cmd`.
- Probe di `%TEMP%\opencode`, prefix data uji `TEST-R6`, cleanup + COUNT 0.

## 6. Bahasa visual

### 6.1 Palet (exact)

| Nama | Hex | Pemakaian |
|---|---|---|
| `BRAND_MAROON` (baru) | `#7A0B2E` | panel kiri login, curtain, splash kiri |
| `BRAND_TEAL` (baru) | `#00A7C4` | panel kiri register, ticker, aksen |
| `BG` / cream | `#FFFBEA` | kanvas kanan |
| `SURFACE` | `#FFFFFF` | kartu logo, field |
| `INK` | `#000000` | border, teks, shadow |
| `PRIMARY` coral | `#FF5A5F` | tombol utama login |
| `SECONDARY` cobalt | `#4D9DE0` | aksen shape |
| `SUCCESS` teal | `#2EC4B6` | info sukses |
| `WARNING` kuning | `#FFB703` | tombol daftar/kembali, shape |
| `DANGER` merah | `#E63946` | error |

Komposisi: tiap layar punya dominasi berbeda — splash maroon+teal, login
maroon+cream+kuning, register teal+cream+kuning. Tidak ada gradient.

### 6.2 Tipografi

- Display: `Arial Black` → fallback `Bahnschrift` → `Segoe UI Black` → SansSerif bold.
- Mono: `Consolas` → `Cascadia Mono` → `Courier New`.
- Body: `Segoe UI`.
- Ukuran: splash wordmark 36/30; login title 56; register title 52;
  kicker/label 11 mono uppercase; field 14 body; tombol 14 bold;
  ticker 12 mono; hint 10 mono.

Helper baru di `NeoBrutalTheme`: `display(int size)`, `mono(int size)`,
`BRAND_MAROON`, `BRAND_TEAL`. Konstanta lama tidak diubah.

### 6.3 Prinsip modern

- Hierarki: hero tebal (border 4px, shadow 8px), detail tipis (border 2px).
- Asimetris: panel brand 440px vs form fleksibel; sticker miring ±3°.
- Tekstur: grid dots halus + shape geometris, bukan foto.
- Motion: spring/overshoot halus, durasi 300–550ms, tidak ada animasi loop
  berat selain ticker & shape drift.
- Arc 0 dipertahankan. Tidak ada rounded corner.

## 7. Layar

### 7.1 Splash (`view.SplashScreen` + `view.SplashBrandPanel`)

- `JWindow` 900×560, center, `setAlwaysOnTop(true)`.
- Layout: WEST panel maroon 340px; CENTER cream; SOUTH ticker 34px full width.
- Kiri: kartu logo cream 240×240 (border hitam 4px + `NeoShadowBorder(8)`),
  `LogoUtil.icon(160)`; di bawahnya sticker mono "SISTEM INFORMASI".
  Background: 10 shape (bintang/plus/lingkaran/kotak) warna kuning/coral/
  cobalt/teal, drift vertikal sinus 6px, timer 33ms.
- Kanan: kicker mono "SISTEM INFORMASI MANAJEMEN TOKO"; wordmark dua baris
  "CV. Almira Jaya" / "Abadi" display 36; label tahap mono 12; `SegmentProgress`
  5 blok 280×18; label status mono 10; tombol "Tutup" (hidden, hanya saat DB gagal).
- Ticker teal: teks berulang
  `"  TOKO BUKU  •  ALAT TULIS  •  SISTEM INFORMASI MANAJEMEN  •  CV. ALMIRA JAYA ABADI  •"`,
  40px/detik.
- Logo stamp: scale 0.85 → 1.05 → 1.0, 420ms `EASE_OUT_BACK`.
- Tahap worker (tetap): (1) "Menyiapkan tampilan..." (2) "Menghubungkan database..."
  (`SELECT 1`) (3) "Memuat data master..." (`KategoriDAOImpl.getAll().size()`)
  (4) "Menyiapkan ikon..." (5) "Siap.". Jeda 300ms per tahap.
- Sukses: panggil `onDone` (Login tampil di belakang) → curtain wipe seluruh
  window ke atas 520ms `EASE_IN_OUT_CUBIC` → `dispose()`.
- DB gagal: status merah "Database tidak terhubung. Hidupkan MySQL lalu ulangi.",
  tombol "Tutup" muncul (klik → `System.exit(1)`), tanpa retry.
- `start()` menampilkan window + menjalankan worker; constructor hanya membangun UI.

### 7.2 Login (`view.Login` + `view.LoginPage` + `view.AuthScaffold`)

- `JFrame` 1080×680, minimum 960×600, center, title
  `"CV. Almira Jaya Abadi - Login"`.
- `AuthScaffold`: split; kiri 440px maroon, kanan cream, bawah ticker 34px.
- Kiri: sticker teal "TOKO BUKU & ALAT TULIS" rotasi -3°; kartu logo 260×260
  (logo 170); wordmark dua baris "CV. Almira Jaya" / "Abadi" display 30 putih;
  alamat toko mono 10 (dipotong satu baris).
- Kanan (max width 420, center):
  - kicker mono "SISTEM INFORMASI"
  - judul "MASUK" display 56
  - subtitle "Selamat datang kembali. Masuk untuk mengelola toko."
  - `NeoField` USERNAME (name `username`) dan PASSWORD (name `password`,
    eye toggle `EYE`/`EYE_OFF`)
  - label error merah (kosong default) + label info hijau
  - `NeoButton` "MASUK" coral + ikon LOGIN
  - `NeoButton` "DAFTAR AKUN" kuning + ikon ACCOUNT_PLUS
  - hint mono "Masuk sebagai Admin atau Kasir."
- Enter = tombol MASUK (`rootPane.setDefaultButton`). Tab order natural.
- Gagal validasi/DB: error inline + shake field 300ms; tanpa `JOptionPane`.
- Sukses: isi `Sesi`, buka `MenuUtama`, `dispose()`.
- Entrance: panel kiri alpha 0→1 + offsetX -40→0; form kanan alpha 0→1 +
  offsetY 24→0; 520ms `EASE_OUT_CUBIC`, kanan delay 80ms.

### 7.3 Register (`view.RegisterPage`)

- Halaman penuh di window yang sama; kiri 440px teal, kanan cream, ticker bawah.
- Kiri: sticker kuning "AKUN KASIR BARU" rotasi +3°; kartu logo; wordmark dua
  baris "CV. Almira Jaya" / "Abadi" display 30 hitam.
- Kanan: kicker "PENDAFTARAN"; judul "DAFTAR" display 52; subtitle
  "Buat akun kasir baru untuk mulai bekerja."; field USERNAME, NAMA LENGKAP,
  PASSWORD, KONFIRMASI (name `username`, `nama_lengkap`, `password`,
  `konfirmasi_password`); label error; tombol "BUAT AKUN" teal + ikon
  ACCOUNT_PLUS; tombol "KEMBALI" kuning + ikon ARROW_LEFT; hint mono
  "Akun baru otomatis menjadi Kasir."
- Validasi (teks error persis):
  - username/nama kosong → "Username dan nama lengkap wajib diisi"
  - password/konfirmasi kosong → "Password dan konfirmasi wajib diisi"
  - password < 4 → "Password minimal 4 karakter"
  - konfirmasi beda → "Password dan konfirmasi tidak sama"
  - username dipakai → "Username sudah dipakai"
  - exception insert → "Gagal daftar: " + pesan
- Sukses: callback ke Login → pindah halaman + info hijau
  "Akun berhasil dibuat. Silakan masuk."
- Role selalu `"Kasir"`; password `HashUtil.sha256`.

### 7.4 Transisi curtain (`view.Login`)

- Root `JLayeredPane`: base = `pageHost` (`CardLayout`, halaman `login`/`register`),
  overlay = panel curtain maroon (logo kecil + wordmark cream "CV. Almira Jaya Abadi" + strip teal).
- `showPage(nama)`:
  1. curtain set bounds `(0, -h, w, h)` + visible.
  2. slide down 340ms `EASE_IN_OUT_CUBIC` sampai menutup.
  3. `CardLayout.show`.
  4. slide up 340ms `EASE_OUT_CUBIC` sampai `-h`.
  5. curtain hidden, `transitioning=false`.
- Klik berulang saat transisi diabaikan (`transitioning` guard).
- Tidak ada window baru; tidak ada flicker.

## 8. Motion system (`util.Motion`)

```java
public final class Motion {
    public interface Easing { double apply(double t); }
    public interface Frame { void at(double progress); }

    public static final Easing LINEAR;
    public static final Easing EASE_OUT_CUBIC;
    public static final Easing EASE_IN_OUT_CUBIC;
    public static final Easing EASE_OUT_BACK; // overshoot

    public static Timer animate(int durationMs, Easing easing,
                                Frame frame, Runnable onDone);
    public static Timer delay(int delayMs, Runnable action);
    public static Timer shake(Component c, int amplitude, int cycles, Runnable onDone);
}
```

- `animate` wajib dipanggil di EDT; `frame` menerima progress 0..1 (sudah
  di-ease), frame terakhir tepat 1.0, lalu `onDone` (boleh null).
- `shake` menggeser komponen kiri-kanan dengan amplitudo mengecil, lalu
  mengembalikan lokasi awal.
- Easing: `EASE_OUT_BACK` overshoot >1 di tengah dan tepat 1 di akhir.
- Semua Timer `setRepeats(false)` untuk delay; `animate` repeat sampai selesai.

## 9. Komponen & API

| File | Tanggung jawab | API kunci |
|---|---|---|
| `util/Motion.java` | easing + Timer + shake + delay | lihat §8 |
| `util/NeoBrutalTheme.java` | palet brand + font helper | `BRAND_MAROON`, `BRAND_TEAL`, `display(int)`, `mono(int)` |
| `view/MotionPanel.java` | fade + offset children tanpa re-layout | `setAlpha(float)`, `setOffset(int,int)` |
| `view/NeoButton.java` | tombol hard-shadow + press physics | ctor `(String, Icon, Color)` |
| `view/NeoField.java` | label + field + focus border + eye toggle | ctor `(String label, boolean password, String name)`, `field()`, `text()`, `password()`, `shake()` |
| `view/TickerPanel.java` | marquee teks | ctor `(String, Color bg, Color fg)`, `start()`, `stop()`, `getOffset()` |
| `view/SegmentProgress.java` | progress 5 blok pop | ctor `(Color...)`, `setValue(int)`, `getValue()` |
| `view/StickerLabel.java` | label kotak miring | ctor `(String, Color bg, Color fg, double deg)` |
| `view/SplashBrandPanel.java` | panel maroon + shape drift | ctor `(JComponent content)`, `start()`, `stop()` |
| `view/SplashScreen.java` | splash window + worker + exit | ctor `(Runnable onDone)`, `start()` |
| `view/AuthScaffold.java` | split brand/form + ticker + intro | ctor `(Color leftBg, Color leftFg, String sticker, String[] titleLines, JComponent form)`, `playIntro()` |
| `view/LoginPage.java` | form login + error/info | ctor `(Runnable onLogin, Runnable onDaftar)`, `username()`, `password()`, `loginButton()`, `showError(String)`, `showInfo(String)`, `playIntro()` |
| `view/RegisterPage.java` | form register + validasi | ctor `(Runnable onBack, Runnable onRegistered)`, `username()`, `nama()`, `password()`, `konfirmasi()`, `playIntro()` |
| `view/Login.java` | frame + router + auth DAO | `main(String[])`, `showPage(String)` private |
| `view/RegisterDialog.java` | **dihapus** | — |

Catatan: `NeoField.field()` mengembalikan `JTextField` (JPasswordField juga
subclass-nya). `NeoField` menyimpan `setName` sesuai parameter agar probe bisa
mencari field lewat nama lama.

## 10. Alur

- **Startup:** `Login.main` → `NeoBrutalTheme.apply()` → `new SplashScreen(onDone)`
  → `start()` → sukses → `onDone` buka `Login` → curtain wipe → splash dispose.
- **Login:** isi field → Enter/klik MASUK → validasi → DAO/hash → `MenuUtama`
  → dispose. Gagal → error inline + shake; field tetap terisi.
- **Register:** klik DAFTAR AKUN → curtain → halaman register → isi → BUAT AKUN
  → validasi/DAO → sukses → curtain → login + info hijau. KEMBALI → curtain →
  login.
- **Logout:** `MenuUtama` tetap `Sesi.clear(); new Login().setVisible(true);`
  (frame baru, intro animation jalan lagi).
- **DB gagal saat splash:** tampil status merah + Tutup; tidak masuk Login.

## 11. Accessibility & robustness

- Semua field punya label visual + `setName`; tab order mengikuti urutan visual.
- Enter memicu aksi utama di login & register; Escape tidak dipakai (hindari
  konflik close).
- Fokus awal di username (login) / username (register).
- Minimum size 960×600; layout tidak pecah di 1366×768.
- Timer ticker/shape berhenti saat `removeNotify`/dispose.
- Tidak ada `Thread.sleep` di EDT.
- `onDone` splash tidak boleh dipanggil dua kali.
- Error DB login/register ditampilkan inline, bukan crash.

## 12. Testing

- `src/test/java/util/MotionTest.java`:
  - easing endpoints (0→0, 1→1), monotonic, `EASE_OUT_BACK` overshoot.
  - `animate` memanggil frame terakhir 1.0 + `onDone` di EDT.
- Probe `%TEMP%\opencode` (dihapus setelah hijau):
  - `ProbeR6Controls`: NeoButton/NeoField/MotionPanel paint + aksi + eye toggle.
  - `ProbeR6Decor`: Ticker offset bergerak, SegmentProgress value, StickerLabel paint.
  - `ProbeR6Splash`: start → tahap berubah → onDone → disposed ≤15 dtk.
  - `ProbeR6Login`: frame Login, judul MASUK, error inline tanpa dialog,
    default button, dialog register lama masih terbuka (Task 29).
  - `ProbeR6Auth`: Login↔Register curtain 3×, validasi register (kosong, <4,
    beda, duplikat admin), register sukses user `TEST-R6-*`, cleanup COUNT 0.
  - `ProbeR6Final`: splash→login nyata, login admin→MenuUtama, dashboard load,
    logout, cleanup DB.
  - `ProbeR6Shots`: Robot screenshot splash/login/register ke
    `%TEMP%\opencode\r6-shots\` untuk review visual.
- `mvn test`: ReportCompilationTest 7 + MotionTest 3 = 10 hijau.
- `mvn package` + smoke `java -jar` 15 detik (window Login tampil),
  lalu kill + DB seed.

## 13. DoD Ronde 6

```markdown
- [ ] Splash 900×560: logo AJB, ticker, 5 blok progress, shape drift, curtain exit.
- [ ] Login 1080×680 split-screen; focus/press/shake/eye toggle berfungsi.
- [ ] Register halaman penuh; Login↔Register 3× tanpa flicker/stuck.
- [ ] Validasi & DAO auth identik (username unik, password ≥4, konfirmasi, role Kasir).
- [ ] Logo tidak diputar/distorsi; palet & arc 0 konsisten.
- [ ] MotionTest + ReportCompilationTest hijau; semua probe hijau.
- [ ] Screenshot Robot dicek visual (splash/login/register).
- [ ] `mvn package` sukses + smoke JAR APP-ALIVE-15S:True.
- [ ] README/TESTING update; ledger + task report ditulis.
- [ ] Final review subagent: 0 Critical, 0 Important tersisa.
- [ ] DB seed utuh (tanpa TEST-R6); tree bersih.
```

## 14. File map

Create:
- `src/main/java/util/Motion.java`
- `src/main/java/view/MotionPanel.java`
- `src/main/java/view/NeoButton.java`
- `src/main/java/view/NeoField.java`
- `src/main/java/view/TickerPanel.java`
- `src/main/java/view/SegmentProgress.java`
- `src/main/java/view/StickerLabel.java`
- `src/main/java/view/SplashBrandPanel.java`
- `src/main/java/view/AuthScaffold.java`
- `src/main/java/view/LoginPage.java`
- `src/main/java/view/RegisterPage.java`
- `src/test/java/util/MotionTest.java`

Modify:
- `src/main/java/util/NeoBrutalTheme.java`
- `src/main/java/view/SplashScreen.java`
- `src/main/java/view/Login.java`
- `README.md`, `TESTING.md`

Delete:
- `src/main/java/view/RegisterDialog.java`

## 15. Self-review spec

1. Placeholder: tidak ada; semua warna/ukuran/teks exact.
2. Konsistensi: API komponen dipakai konsisten di §7, §9, §12.
3. Scope: 7 task (25–31), satu window auth, tanpa perubahan DB.
4. Anti-goals tertutup oleh Review Focus plan (EDT, flicker, auth, logo, DB gagal).
