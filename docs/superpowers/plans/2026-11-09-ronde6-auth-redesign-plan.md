# Ronde 6 — Auth Redesign Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Redesign Splash, Login, dan Register menjadi neobrutalism modern yang colorful + animatif, tanpa mengubah alur auth/DAO/validasi dan tanpa dependency baru.

**Architecture:** Motion system kecil berbasis `javax.swing.Timer` (easing + delay + shake) dipakai komponen custom (`NeoButton`, `NeoField`, `TickerPanel`, `SegmentProgress`, `StickerLabel`, `MotionPanel`). Splash memakai `JWindow` + worker + curtain exit; Login/Register memakai satu `JFrame` dengan `JLayeredPane` + `CardLayout` + curtain overlay.

**Tech Stack:** Java 11, Swing, FlatLaf, Ikonli MaterialDesign2, Maven, MySQL/MariaDB.

**Spec:** `docs/superpowers/specs/2026-11-09-ronde6-auth-redesign-design.md`

## Global Constraints

- Branch: `impl/ronde6-auth`, base `master` @ `c550438`. Jangan menyentuh modul di luar auth/master data.
- Java 11 source/target, UTF-8. DILARANG menambah dependency Maven atau file font.
- Palet exact: `BRAND_MAROON #7A0B2E`, `BRAND_TEAL #00A7C4`, cream `#FFFBEA`, white `#FFFFFF`, ink `#000000`, coral `#FF5A5F`, cobalt `#4D9DE0`, teal `#2EC4B6`, kuning `#FFB703`, merah `#E63946`. Arc 0, tanpa gradient/rounded.
- Logo `src/main/resources/images/logo_ajb_transparent.png` dipakai apa adanya: tidak diputar, tidak di-crop, tidak diubah warna.
- Animasi hanya `javax.swing.Timer` di EDT. DILARANG `Thread.sleep` di EDT. DB hanya di `SwingWorker`/aksi tombol.
- Timer komponen wajib berhenti di `removeNotify`/dispose. `onDone` splash tidak boleh dobel.
- Validasi auth tidak berubah: username unik, password ≥4, konfirmasi sama, role `Kasir`, hash `HashUtil.sha256`.
- PowerShell 5.1: tanpa `&&`; Maven `C:\Users\alvin\tools\apache-maven-3.9.9\bin\mvn.cmd`.
- DB `root`/"" (`db_toko_buku`). Bila mati: `Start-Process C:\xampp\mysql\bin\mysqld.exe`.
- Probe di `%TEMP%\opencode`, classpath FULL: `target\classes` + output
  `mvn -q org.apache.maven.plugins:maven-dependency-plugin:3.6.1:build-classpath -Dmdep.outputFile=%TEMP%\opencode\r6-cp.txt`.
  File probe UTF-8 tanpa BOM; GUI probe `System.exit(0)`; hapus probe setelah hijau.
- Data uji prefix `TEST-R6`; cleanup + assert COUNT 0.
- Commit per task. Pesan commit memakai prefix `feat(r6):` / `test(r6):` / `docs(r6):`.

## Review Focus

1. **EDT responsiveness:** splash/login/register harus tetap merespons saat animasi berjalan — probe menunggu event lain/klik saat timer aktif; tidak boleh freeze.
2. **Curtain reliability:** Login↔Register 3× berturut-turut tidak boleh blank, stuck, atau dobel transisi — probe `ProbeR6Auth`.
3. **Auth behavior unchanged:** password salah, username duplikat (`admin`), password <4, konfirmasi beda tetap menghasilkan pesan yang sama dan tidak insert — probe Task 30.
4. **Logo fidelity:** logo tetap proporsional/transparan dan tidak diputar di splash/login/register — screenshot Robot Task 31 + cek `LogoUtil.icon`.
5. **Splash DB failure path:** DB mati → status merah + tombol Tutup, tidak hang — review kode Task 28 (tanpa retry).
6. **Timer leaks:** ticker/shape timer berhenti saat window ditutup — review `removeNotify`/`stop()` Task 27/28.
7. **Small screen:** 1366×768 muat (frame 1080×680 + title bar) — screenshot Task 31.

---

### Task 25: Motion system + theme foundations

**Files:**
- Create: `src/main/java/util/Motion.java`
- Create: `src/test/java/util/MotionTest.java`
- Modify: `src/main/java/util/NeoBrutalTheme.java`

**Interfaces:**
- Consumes: tidak ada.
- Produces: `Motion.LINEAR`, `Motion.EASE_OUT_CUBIC`, `Motion.EASE_IN_OUT_CUBIC`,
  `Motion.EASE_OUT_BACK`, `Motion.animate(int, Easing, Frame, Runnable)`,
  `Motion.delay(int, Runnable)`, `Motion.shake(Component, int, int, Runnable)`;
  `NeoBrutalTheme.BRAND_MAROON`, `NeoBrutalTheme.BRAND_TEAL`,
  `NeoBrutalTheme.display(int)`, `NeoBrutalTheme.mono(int)`.

- [ ] **Step 1: Tulis test RED** — `MotionTest.java`:

```java
package util;

import static org.junit.jupiter.api.Assertions.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;

public class MotionTest {
    @Test void easingEndpointsAndMonotonic() {
        assertEquals(0.0, Motion.EASE_OUT_CUBIC.apply(0), 1e-9);
        assertEquals(1.0, Motion.EASE_OUT_CUBIC.apply(1), 1e-9);
        double a = Motion.EASE_OUT_CUBIC.apply(0.25);
        double b = Motion.EASE_OUT_CUBIC.apply(0.50);
        double c = Motion.EASE_OUT_CUBIC.apply(0.75);
        assertTrue(a < b && b < c && c < 1.0);
    }
    @Test void easeOutBackOvershootsThenEndsAtOne() {
        assertTrue(Motion.EASE_OUT_BACK.apply(0.7) > 1.0);
        assertEquals(1.0, Motion.EASE_OUT_BACK.apply(1.0), 1e-6);
    }
    @Test void animateEndsAtOneOnEdt() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Double> last = new AtomicReference<>(0.0);
        AtomicBoolean edt = new AtomicBoolean(false);
        Motion.animate(120, Motion.LINEAR,
                p -> last.set(p),
                () -> { edt.set(SwingUtilities.isEventDispatchThread()); latch.countDown(); });
        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertEquals(1.0, last.get(), 1e-9);
        assertTrue(edt.get());
    }
}
```

- [ ] **Step 2: Jalankan, pastikan RED**

Run: `& "C:\Users\alvin\tools\apache-maven-3.9.9\bin\mvn.cmd" -q -Dtest=MotionTest test`

Expected: COMPILATION ERROR `cannot find symbol: class Motion` (atau method belum ada).

- [ ] **Step 3: Implementasikan `Motion` + theme**

`Motion`:
- `LINEAR = t -> t`.
- `EASE_OUT_CUBIC = t -> 1 - Math.pow(1 - t, 3)`.
- `EASE_IN_OUT_CUBIC`: `t < 0.5 ? 4*t*t*t : 1 - Math.pow(-2*t + 2, 3) / 2`.
- `EASE_OUT_BACK`: `c1 = 1.70158`, `c3 = c1 + 1`, `1 + c3*Math.pow(t-1,3) + c1*Math.pow(t-1,2)`.
- `animate`: `javax.swing.Timer` interval 16ms, hitung `elapsed = System.nanoTime() - start`,
  `progress = clamp(elapsed / (durationMs*1e6))`, `frame.at(easing.apply(progress))`;
  saat progress >= 1: stop, panggil frame 1.0 sekali lagi, lalu `onDone` (bila non-null).
- `delay`: Timer sekali jalan (`setRepeats(false)`), `action.run()` di EDT.
- `shake`: simpan lokasi awal, timer 16ms 300ms, `dx = sin(progress*PI*cycles) * amplitude * (1-progress)`,
  `c.setLocation(origX + (int)dx, origY)`, akhir kembalikan lokasi awal + `onDone`.

`NeoBrutalTheme`:
- Tambah `public static final Color BRAND_MAROON = Color.decode("#7A0B2E");`
  dan `BRAND_TEAL = Color.decode("#00A7C4");`.
- Tambah `private static String pickFont(String... names)` yang memakai
  `GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()`
  (case-insensitive), fallback `Font.SANS_SERIF`/`Font.MONOSPACED`.
- `public static Font display(int size)` → family pertama dari
  `"Arial Black", "Bahnschrift", "Segoe UI Black"`, style `Font.BOLD`.
- `public static Font mono(int size)` → `"Consolas", "Cascadia Mono", "Courier New"`, style `Font.BOLD`.
- Jangan ubah konstanta/UIManager lama.

- [ ] **Step 4: Verifikasi GREEN**

Run: `mvn -q -Dtest=MotionTest test` → `Tests run: 3, Failures: 0, Errors: 0`.
Run: `mvn test` → `ReportCompilationTest` 7 + `MotionTest` 3 = `Tests run: 10, Failures: 0`.

- [ ] **Step 5: Commit**

```powershell
git add src/main/java/util/Motion.java src/test/java/util/MotionTest.java src/main/java/util/NeoBrutalTheme.java
git commit -m "feat(r6): motion system + brand theme foundation"
```

**DoD Task 25:** MotionTest 3/3 hijau, total test 10/10, tidak ada dependency baru, theme lama utuh.

---

### Task 26: MotionPanel + NeoButton + NeoField

**Files:**
- Create: `src/main/java/view/MotionPanel.java`
- Create: `src/main/java/view/NeoButton.java`
- Create: `src/main/java/view/NeoField.java`
- Test: probe `%TEMP%\opencode\ProbeR6Controls.java` (buang setelah hijau)

**Interfaces:**
- Consumes: `Motion`, `NeoBrutalTheme.BRAND_*`, `display`, `mono`; Ikonli `MaterialDesignE.EYE/EYE_OFF`.
- Produces:
  - `MotionPanel(LayoutManager)` + `setAlpha(float)` + `setOffset(int,int)`.
  - `NeoButton(String, Icon, Color)`.
  - `NeoField(String label, boolean password, String name)` + `field()`,
    `text()`, `password()`, `shake()`.

- [ ] **Step 1: Tulis probe RED** — `ProbeR6Controls.java`:
  - `NeoButton b = new NeoButton("MASUK", null, NeoBrutalTheme.PRIMARY);`
    `b.setName("btn");` pasang `ActionListener` set flag, `b.doClick()`,
    assert flag true, `b.getPreferredSize().width > 0`.
  - `NeoField f = new NeoField("USERNAME", false, "username"); f.field().setText("admin");`
    assert `f.text().equals("admin")` dan `f.field().getName().equals("username")`.
  - `NeoField p = new NeoField("PASSWORD", true, "password");`
    `p.field().setText("rahasia");` assert `new String(p.password()).equals("rahasia")`;
    `JPasswordField pf = (JPasswordField) p.field();` assert echo char awal `'•'`;
    klik tombol eye (cari `JButton` di dalam `p`), assert echo char `(char)0`, klik lagi assert `'•'`.
  - Paint `MotionPanel`, `NeoButton`, `NeoField` ke `BufferedImage`
    (`new BufferedImage(300,80,TYPE_INT_ARGB)`, `comp.setSize(...)`, `comp.paint(g)`),
    assert tidak ada exception.
  - Program `main` di EDT, akhir `System.exit(0)`; cetak `PROBE-R6-CONTROLS-OK`.

- [ ] **Step 2: Jalankan, pastikan RED**

Run: compile probe dengan classpath penuh.
Expected: `cannot find symbol: class NeoButton` (atau `NeoField`).

- [ ] **Step 3: Implementasikan**

`MotionPanel`:
- `extends JPanel`; `alpha = 1f`, `offsetX`, `offsetY`.
- `setAlpha`/`setOffset` clamp + `repaint()`.
- `paintChildren`: `Graphics2D`, `AlphaComposite.SrcOver.derive(alpha)`,
  `translate(offsetX, offsetY)`, `super.paintChildren(g2)`.
- Default `setOpaque(false)`.

`NeoButton`:
- `extends JButton`, constructor `(String text, Icon icon, Color bg)`.
- `setContentAreaFilled(false)`, `setFocusPainted(false)`, `setOpaque(false)`,
  `setBorder(new EmptyBorder(10,16,10,16))`, font `Segoe UI Black` 13,
  foreground `INK`, icon gap 8, cursor hand.
- Field `press` 0..1; `paintComponent`:
  - shadow hitam offset 5 (saat idle), bg `bg`, border hitam 3px, lalu
    `translate(press*3, press*3)` dan `super.paintComponent`.
  - `getPreferredSize` tambah 5px shadow di kanan/bawah.
- Mouse listener: press → `Motion.animate(110, EASE_OUT_CUBIC, p -> press=p, null)`;
  release → animate balik. Hover shadow 5→7 opsional (tetap pakai Timer).

`NeoField`:
- `extends JPanel`; `BorderLayout`; label mono 11 uppercase di NORTH;
  `fieldWrap` (`JPanel` BorderLayout, border `FocusBorder`) di CENTER.
- `FocusBorder` inner class `AbstractBorder`: insets konstan 10, `paintBorder`
  tebal `2 + 2*focus`, focus dianimasikan 180ms `EASE_OUT_CUBIC`.
- Di dalam `fieldWrap`: `JTextField`/`JPasswordField` (password) di CENTER,
  `setName(name)`, font Segoe UI 14, background `SURFACE`, border kosong,
  caret/selection `INK`/kuning. `field()` mengembalikan field tersebut.
- Password: `setEchoChar('•')`; tombol eye 28×28 di EAST `fieldWrap`,
  background transparan, tanpa border, ikon `EYE`/`EYE_OFF` 16px, klik toggle
  echo char `(char)0` ↔ `'•'` + ganti ikon.
- `shake()` → `Motion.shake(fieldWrap, 8, 4, null)`.

- [ ] **Step 4: Verifikasi GREEN**

Run probe → `PROBE-R6-CONTROLS-OK`.
Run `mvn -q compile` → BUILD SUCCESS.
Run `mvn test` → 10/10.

- [ ] **Step 5: Commit**

```powershell
git add src/main/java/view/MotionPanel.java src/main/java/view/NeoButton.java src/main/java/view/NeoField.java
git commit -m "feat(r6): motion panel + neo button/field"
```

**DoD Task 26:** probe kontrol OK, eye toggle bekerja, paint tanpa exception, test 10/10.

---

### Task 27: TickerPanel + SegmentProgress + StickerLabel

**Files:**
- Create: `src/main/java/view/TickerPanel.java`
- Create: `src/main/java/view/SegmentProgress.java`
- Create: `src/main/java/view/StickerLabel.java`
- Test: probe `%TEMP%\opencode\ProbeR6Decor.java`

**Interfaces:**
- Consumes: `NeoBrutalTheme.mono`, `Motion`.
- Produces:
  - `TickerPanel(String text, Color bg, Color fg)` + `start()`, `stop()`, `getOffset()`.
  - `SegmentProgress(Color... colors)` + `setValue(int)`, `getValue()`.
  - `StickerLabel(String text, Color bg, Color fg, double degrees)`.

- [ ] **Step 1: Tulis probe RED** — `ProbeR6Decor.java`:
  - `TickerPanel t = new TickerPanel("TES", WARNING, INK); t.setSize(400,34); t.start();`
    sleep 350ms, assert `t.getOffset() > 0`; `t.stop()`.
  - `SegmentProgress s = new SegmentProgress(); s.setValue(4);` assert `getValue()==4`;
    paint ke `BufferedImage(300,24)` tanpa exception.
  - `StickerLabel st = new StickerLabel("TOKO BUKU", BRAND_TEAL, INK, -3);`
    assert `getPreferredSize().width > 0`; paint ke image tanpa exception.
  - Cetak `PROBE-R6-DECOR-OK`, `System.exit(0)`.

- [ ] **Step 2: Jalankan, pastikan RED** → compile gagal `cannot find symbol: class TickerPanel`.

- [ ] **Step 3: Implementasikan**

`TickerPanel`:
- `extends JComponent`; tinggi preferred 34; font `mono(12)`, fg `INK`.
- Field `double offset`; Timer 30ms, `offset += 1.2` (≈40px/detik).
- `start()` idempoten; `stop()`; `addNotify` start, `removeNotify` stop.
- `paintComponent`: fill bg; garis hitam 2px atas/bawah; clip; `FontMetrics.stringWidth`;
  gambar teks berulang dari `-offset % textWidth` sampai lebar komponen.
- `getOffset()` mengembalikan `(int) offset` untuk probe.

`SegmentProgress`:
- `extends JComponent`; default 5 warna:
  `BRAND_MAROON, BRAND_TEAL, WARNING, PRIMARY, SECONDARY`.
- Preferred 280×18; gap 6; border hitam 2; blok terisi warna, kosong `SURFACE`.
- `setValue(int)`: clamp 0..n; blok baru memicu timestamp pop 150ms;
  `paintComponent` menggambar skala 1.0 + 0.12*(sisa) untuk blok yang pop.
- `getValue()`.

`StickerLabel`:
- `extends JComponent`; font `mono(11)` uppercase; padding 16×8 + ruang rotasi 10.
- `getPreferredSize` dari `FontMetrics.stringWidth(text)+32`, tinggi font+16+10.
- `paintComponent`: `Graphics2D` rotate `Math.toRadians(degrees)` di center;
  fill rect bg, border hitam 2, teks fg center.

- [ ] **Step 4: Verifikasi GREEN** → `PROBE-R6-DECOR-OK`, `mvn test` 10/10.

- [ ] **Step 5: Commit**

```powershell
git add src/main/java/view/TickerPanel.java src/main/java/view/SegmentProgress.java src/main/java/view/StickerLabel.java
git commit -m "feat(r6): ticker, segmented progress, sticker components"
```

**DoD Task 27:** ticker bergerak, progress value benar, sticker paint tanpa exception, timer berhenti di `removeNotify`.

---

### Task 28: Splash redesign + curtain exit

**Files:**
- Create: `src/main/java/view/SplashBrandPanel.java`
- Modify: `src/main/java/view/SplashScreen.java` (rewrite)
- Test: probe `%TEMP%\opencode\ProbeR6Splash.java`

**Interfaces:**
- Consumes: `LogoUtil.icon(int)`, `AppConfig.APP_NAME`, `KategoriDAOImpl.getAll()`,
  `Koneksi.getConnection()`, `TickerPanel`, `SegmentProgress`, `Motion`, `NeoShadowBorder`.
- Produces:
  - `SplashBrandPanel(JComponent content)` + `start()`, `stop()`.
  - `SplashScreen(Runnable onDone)` + `start()`.
  - Field nama: `lblTahap`, `lblStatus`, `btnTutup` (untuk probe).

- [ ] **Step 1: Tulis probe RED** — `ProbeR6Splash.java`:
  - EDT: `SplashScreen sp = new SplashScreen(() -> done.set(true)); sp.start();`
  - assert `sp.isShowing()` dan `sp.getWidth()==900 && sp.getHeight()==560`.
  - Refleksi `lblTahap`: catat teks awal, poll ≤3 dtk sampai berubah.
  - Tunggu `done` ≤15 dtk; tunggu `!sp.isDisplayable()` ≤16 dtk; assert keduanya.
  - `System.exit(0)`, cetak `PROBE-R6-SPLASH-OK`.

- [ ] **Step 2: Jalankan, pastikan RED** → `SplashScreen` belum punya `start()` / ukuran lama 460×420.

- [ ] **Step 3: Implementasikan**

`SplashBrandPanel`:
- `extends JPanel`, bg `BRAND_MAROON`, layout `GridBagLayout`, tambahkan `content` di center.
- 10 shape (4 bintang, 3 plus, 3 lingkaran) posisi fixed relatif, warna
  `WARNING/PRIMARY/SECONDARY/BRAND_TEAL`, drift `sin(phase+i)*6`.
- Timer 33ms `phase += 0.05`, `repaint()`; `start()/stop()` idempoten.

`SplashScreen`:
- `JWindow`, size 900×560, center, `setAlwaysOnTop(true)`.
- Root `BorderLayout`: WEST `SplashBrandPanel` (preferred 340×0) berisi
  kartu logo cream 240×240 (border compound `NeoShadowBorder(8)` + empty 16)
  dengan `JLabel(LogoUtil.icon(160))` + sticker "SISTEM INFORMASI";
  CENTER panel cream (GridBag) berisi kicker mono, dua label wordmark display 36
  ("ALMIRA JAYA", "ABADI"), `lblTahap` mono 12, `SegmentProgress`,
  `lblStatus` mono 10, `btnTutup` (hidden, background PRIMARY, border 2,
  aksi `System.exit(1)`); SOUTH `TickerPanel` teks §7.1.
- Constructor hanya build UI; `start()` → `setVisible(true)`, `brand.start()`,
  `ticker.start()`, `worker.execute()`.
- Worker: 5 tahap seperti spec; update UI via `SwingUtilities.invokeAndWait`
  (progress + label), `Thread.sleep(300)` di worker (bukan EDT).
- `done()`:
  - sukses: guard `selesai` sekali; `onDone.run()`; stop timer; animasi
    `setLocation(x, y - p*(height+40))` 520ms `EASE_IN_OUT_CUBIC`; `dispose()`.
  - gagal: `lblStatus` merah "Database tidak terhubung. Hidupkan MySQL lalu ulangi.",
    `btnTutup.setVisible(true)`, stop timer.
- Jangan panggil `onDone` saat gagal.

- [ ] **Step 4: Verifikasi GREEN** → `PROBE-R6-SPLASH-OK`, `mvn test` 10/10.

- [ ] **Step 5: Review jalur DB gagal** — baca kode: tidak ada retry, tombol Tutup
  muncul, `onDone` tidak dipanggil. Catat di commit body/ledger.

- [ ] **Step 6: Commit**

```powershell
git add src/main/java/view/SplashBrandPanel.java src/main/java/view/SplashScreen.java
git commit -m "feat(r6): splash redesign with curtain exit"
```

**DoD Task 28:** splash 900×560, logo utuh, ticker+segmen+shape jalan, exit curtain ≤1 dtk setelah onDone, dispose, DB gagal tidak hang.

---

### Task 29: AuthScaffold + LoginPage + Login frame

**Files:**
- Create: `src/main/java/view/AuthScaffold.java`
- Create: `src/main/java/view/LoginPage.java`
- Modify: `src/main/java/view/Login.java` (rewrite)
- Keep: `src/main/java/view/RegisterDialog.java` (sementara; dihapus Task 30)
- Test: probe `%TEMP%\opencode\ProbeR6Login.java`

**Interfaces:**
- Consumes: `NeoButton`, `NeoField`, `StickerLabel`, `TickerPanel`, `MotionPanel`,
  `Motion`, `LogoUtil`, `AppConfig`, `UserDAO`, `HashUtil`, `Sesi`.
- Produces:
  - `AuthScaffold(Color leftBg, Color leftFg, String sticker, String[] titleLines,
    JComponent formContent)` + `playIntro()`.
  - `LoginPage(Runnable onLogin, Runnable onDaftar)` + `username()`, `password()`,
    `loginButton()`, `showError(String)`, `showInfo(String)`, `playIntro()`.
  - `Login` frame baru; `Login.main` memakai `new SplashScreen(...).start()`.

- [ ] **Step 1: Tulis probe RED** — `ProbeR6Login.java`:
  - EDT: `Login l = new Login(); l.setVisible(true);`
  - assert `l.getTitle().contains("Login")`, `l.getWidth()==1080`,
    title label "MASUK" ada (rekursif cari JLabel).
  - `l.getRootPane().getDefaultButton()` non-null.
  - Panggil `doLogin()` via refleksi dengan field kosong → assert label
    bernama `loginError` berisi "Username dan password wajib diisi",
    dan watchdog 600ms TIDAK melihat `JDialog`.
  - Klik tombol bernama `btnDaftar` → watchdog mendeteksi `RegisterDialog`
    lalu dispose; assert dialog muncul.
  - Cetak `PROBE-R6-LOGIN-OK`, `System.exit(0)`.

- [ ] **Step 2: Jalankan, pastikan RED** → judul/label/ukuran lama tidak cocok.

- [ ] **Step 3: Implementasikan `AuthScaffold`**

- `extends JPanel`, `BorderLayout(0,0)`.
- WEST panel 440px `MotionPanel` bg `leftBg`, isi vertical (BoxLayout/GridBag):
  `StickerLabel(sticker, aksen, leftFg, -3)` → spacer → kartu logo 260×260
  (cream, border hitam 4 + `NeoShadowBorder(8)`, `LogoUtil.icon(170)`) →
  dua label titleLines `display(30)` fg `leftFg` → alamat toko mono 10
  (`AppConfig.ALAMAT_TOKO`, satu baris, ellipsis manual bila panjang).
- CENTER panel cream `MotionPanel`, `GridBagLayout`, `formContent` di center
  dengan lebar maksimum 420 (pakai `setPreferredSize(new Dimension(420, ...))`
  dan wrapper `GridBag`).
- SOUTH `TickerPanel` teks §7.1, tinggi 34.
- `playIntro()`: set left alpha 0/offsetX -40, right alpha 0/offsetY 24;
  `Motion.animate(520, EASE_OUT_CUBIC, ...)` untuk left; delay 80ms untuk right.
  Simpan timer agar bisa di-stop? Cukup sekali jalan.

`LoginPage`:
- `extends JPanel`, `BorderLayout`; field final `NeoField fUsername/fPassword`,
  `NeoButton btnLogin/btnDaftar`, `JLabel lblError/lblInfo`, `AuthScaffold scaffold`.
- Build form: kicker mono "SISTEM INFORMASI", judul `display(56)` "MASUK",
  subtitle body 13, fields, error (`setName("loginError")`, merah, teks " "),
  info (`setName("loginInfo")`, hijau), tombol MASUK coral + ikon LOGIN,
  tombol DAFTAR AKUN kuning + ikon ACCOUNT_PLUS, hint mono 10.
- `btnLogin.setName("btnLogin")`, `btnDaftar.setName("btnDaftar")`.
- `showError(msg)`: error merah + info kosong + `fUsername.shake()`,
  delay 60ms `fPassword.shake()`.
- `showInfo(msg)`: info hijau + error kosong.
- `playIntro()` delegasi `scaffold.playIntro()`; `SwingUtilities.invokeLater`
  memfokuskan `fUsername.field().requestFocusInWindow()`.

`Login`:
- `extends JFrame`, `EXIT_ON_CLOSE`, 1080×680, min 960×600, center.
- `loginPage = new LoginPage(this::doLogin, () -> new RegisterDialog(this).setVisible(true));`
- `setContentPane(loginPage)`; default button `loginPage.loginButton()`;
  `SwingUtilities.invokeLater(loginPage::playIntro)`.
- `doLogin()`: username/password; kosong → error; DAO `getByUsername`;
  cek `HashUtil.sha256(password).equals(u.getPassword())`; gagal → error
  "Username atau password salah"; sukses → `Sesi.userLogin`, `Sesi.role`,
  `new MenuUtama().setVisible(true)`, `dispose()`; catch `RuntimeException`
  → error "Gagal terhubung database: " + pesan.
- `main`: `NeoBrutalTheme.apply(); new SplashScreen(() -> new Login().setVisible(true)).start();`
  (`onDone` sudah di EDT; tanpa `invokeLater` ganda).

- [ ] **Step 4: Verifikasi GREEN** → `PROBE-R6-LOGIN-OK`; `mvn -q compile` sukses;
  `mvn test` 10/10.

- [ ] **Step 5: Commit**

```powershell
git add src/main/java/view/AuthScaffold.java src/main/java/view/LoginPage.java src/main/java/view/Login.java
git commit -m "feat(r6): auth scaffold + login page redesign"
```

**DoD Task 29:** Login 1080×680 tampil, form berfungsi, error inline tanpa dialog, default button ada, RegisterDialog lama masih bisa dibuka, test 10/10.

---

### Task 30: RegisterPage full-page + curtain router + hapus RegisterDialog

**Files:**
- Create: `src/main/java/view/RegisterPage.java`
- Modify: `src/main/java/view/Login.java` (tambah `CardLayout` + curtain)
- Delete: `src/main/java/view/RegisterDialog.java`
- Test: probe `%TEMP%\opencode\ProbeR6Auth.java`

**Interfaces:**
- Consumes: `AuthScaffold`, `NeoField`, `NeoButton`, `Motion`, `UserDAO`, `HashUtil`.
- Produces:
  - `RegisterPage(Runnable onBack, Runnable onRegistered)` + `username()`,
    `nama()`, `password()`, `konfirmasi()`, `playIntro()`.
  - `Login` dengan `showPage(String)` private, halaman `"login"`/`"register"`.

- [ ] **Step 1: Tulis probe RED** — `ProbeR6Auth.java`:
  - EDT: `Login l = new Login(); l.setVisible(true);` tunggu 700ms.
  - Cari label "MASUK"; klik `btnDaftar`; tunggu 900ms; assert label "DAFTAR" tampil
    (halaman register).
  - Ulangi `btnBack` → "MASUK", lalu `btnDaftar` lagi → "DAFTAR" (total 3× bolak-balik),
    tiap transisi tunggu 900ms; assert tidak ada dialog/exception.
  - Validasi register (panggil method via refleksi atau klik `btnRegister`):
    - semua kosong → error "Username dan nama lengkap wajib diisi".
    - password "123" → "Password minimal 4 karakter".
    - konfirmasi beda → "Password dan konfirmasi tidak sama".
    - username "admin" + password valid → "Username sudah dipakai".
    - sukses: username `TEST-R6-<epoch>`, nama "TEST R6", password "test123",
      konfirmasi sama → callback pindah login + info hijau.
  - Assert user `TEST-R6-*` ada via `UserDAO.getByUsername`; cleanup
    `DELETE FROM users WHERE username LIKE 'TEST-R6-%'`; assert COUNT 0.
  - Cetak `PROBE-R6-AUTH-OK`, `System.exit(0)`.

- [ ] **Step 2: Jalankan, pastikan RED** → `RegisterPage` belum ada / masih dialog.

- [ ] **Step 3: Implementasikan `RegisterPage`**

- `extends JPanel`; `AuthScaffold(BRAND_TEAL, Color.BLACK, "AKUN KASIR BARU",
  {"ALMIRA JAYA","ABADI"}, form)`.
- Form: kicker "PENDAFTARAN", judul "DAFTAR" `display(52)`, subtitle,
  4 `NeoField` (`username`, `nama_lengkap`, `password`, `konfirmasi_password`),
  error (`setName("registerError")`), tombol "BUAT AKUN" teal + ikon
  ACCOUNT_PLUS (`setName("btnRegister")`), tombol "KEMBALI" kuning + ikon
  ARROW_LEFT (`setName("btnBack")`), hint mono.
- `doDaftar()`: validasi teks persis spec §7.3; `userDAO.getByUsername` untuk
  duplikat; `User` role "Kasir", password hash; `insert`; catch RuntimeException
  → "Gagal daftar: " + pesan; sukses → `onRegistered.run()`.
- `showError` + shake field terkait; `playIntro()` delegasi scaffold +
  `SwingUtilities.invokeLater` memfokuskan `fUsername.field()`.

- [ ] **Step 4: Implementasikan router + curtain di `Login`**

- Field: `CardLayout cardLayout`, `JPanel pageHost`, `RegisterPage registerPage`,
  `JLayeredPane layered`, `JPanel curtain`, `boolean transitioning`.
- `pageHost.add(loginPage, "login"); pageHost.add(registerPage, "register");`
- `curtain`: panel `BRAND_MAROON` + `LogoUtil.icon(72)` + label "ALMIRA JAYA ABADI"
  `display(20)` cream + strip bawah `BRAND_TEAL` 12px; border hitam 3.
- `AuthLayers extends JLayeredPane`: `doLayout` set `pageHost` full bounds,
  `curtain` bounds `(0, curtain.getY(), w, h)`; add base DEFAULT_LAYER,
  curtain PALETTE_LAYER; curtain awalnya invisible.
- `showPage(nama)` persis spec §7.4 (cover 340ms `EASE_IN_OUT_CUBIC`,
  `cardLayout.show`, reveal 340ms `EASE_OUT_CUBIC`, guard `transitioning`).
  Bila `layered.getHeight()==0` (belum tampil), pakai `getHeight()` sebagai `h`.
- Wiring: `loginPage` `onDaftar` → `showPage("register")`; `registerPage`
  `onBack` → `showPage("login")`; `onRegistered` → `showPage("login")` +
  `loginPage.showInfo("Akun berhasil dibuat. Silakan masuk.")`.
- Hapus `RegisterDialog` + import/referensinya. Grep `RegisterDialog` harus 0.
- `Login.main` tetap memakai splash `start()`.

- [ ] **Step 5: Verifikasi GREEN** → `PROBE-R6-AUTH-OK`; `mvn test` 10/10;
  `grep -r "RegisterDialog" src` → 0; DB `TEST-R6` COUNT 0.

- [ ] **Step 6: Commit**

```powershell
git add src/main/java/view/RegisterPage.java src/main/java/view/Login.java
git rm src/main/java/view/RegisterDialog.java
git commit -m "feat(r6): full-page register + curtain router"
```

**DoD Task 30:** Login↔Register 3× mulus, validasi register persis, user uji dibuat+ dibersihkan, `RegisterDialog` hilang, test 10/10.

---

### Task 31: E2E visual + package + docs + review

**Files:**
- Modify: `README.md`, `TESTING.md`
- Create: `.superpowers/sdd/2026-11-09-ronde6-auth-plan/progress.md`
- Create: `.superpowers/sdd/2026-11-09-ronde6-auth-plan/task-31-report.md`
- Test: `%TEMP%\opencode\ProbeR6Final.java`, `%TEMP%\opencode\ProbeR6Shots.java`

**Interfaces:**
- Consumes: semua kontrak Task 25–30.

- [ ] **Step 1: Probe E2E final** — `ProbeR6Final.java`:
  - Splash nyata: `new SplashScreen(() -> { loginRef[0] = new Login(); loginRef[0].setVisible(true); }).start();`
    tunggu ≤15 dtk sampai Login tampil + splash disposed.
  - Login admin/admin123 via field + klik `btnLogin` → tunggu MenuUtama tampil;
    assert `Sesi.role` = "Admin".
  - Buka Dashboard (`new FormDashboard()`), tunggu 500ms, tanpa dialog error.
  - Logout: refleksi `btnLogout` di MenuUtama → `doClick()`; tunggu 800ms;
    assert `Sesi.userLogin == null` dan window Login baru tampil.
  - Register user `TEST-R6-E2E`, assert ada, cleanup SQL, COUNT 0.
  - Cetak `PROBE-R6-FINAL-OK`, `System.exit(0)`.

- [ ] **Step 2: Screenshot visual** — `ProbeR6Shots.java`:
  - Splash stage 2–3 → `Robot.createScreenCapture` ke
    `%TEMP%\opencode\r6-shots\splash.png`; Login → `login.png`;
    klik DAFTAR → tunggu → `register.png`.
  - Capture memakai bounds window (`Window.getBounds()`) setelah `toFront()`
    dan jeda 300ms; simpan PNG lewat `ImageIO.write`.
  - Baca ketiganya dengan tool `read` (image), cek: logo utuh, warna tidak
    monoton, teks tidak terpotong, tidak ada komponen tumpang tindih.
  - Bila ada masalah visual → perbaiki di task ini (red→green via screenshot ulang).

- [ ] **Step 3: Test + package + smoke**

Run: `mvn test` → ReportCompilationTest 7 + MotionTest 3 = 10 hijau.
Run: `mvn package` → BUILD SUCCESS; catat JAR `target/sistem-toko-buku-1.0.0.jar`.
Run: `Start-Process java -ArgumentList "-jar","target\sistem-toko-buku-1.0.0.jar"` →
tunggu 15 dtk → assert window Login tampil (`APP-ALIVE-15S=True`) → kill java.
Cek DB seed: `TEST-R6` COUNT 0; stok BK-001/ATK-001/ATK-002 tidak berubah.

- [ ] **Step 4: Docs**

- `README.md`: update bagian UI/desain — splash baru, login split-screen, register
  full-page, motion system, palet maroon/teal; hapus referensi foto rak buku.
- `TESTING.md`: langkah manual baru — splash (ticker/progress/curtain), login
  (error inline, eye toggle, Enter), register (halaman penuh, validasi, kembali),
  catatan animasi tidak menghambat input.
- `.superpowers/sdd/2026-11-09-ronde6-auth-plan/progress.md`: isi base commit,
  spec/plan path, status tiap task, DoD, ruling.
- `.superpowers/sdd/2026-11-09-ronde6-auth-plan/task-31-report.md`: ringkasan
  probe, screenshot, package, smoke, review, deviasi/ruling.

- [ ] **Step 5: Final review**

Dispatch fresh subagent `general` dengan paket: diff `c550438..HEAD`, spec ronde 6,
plan ronde 6, ledger rulings. Fokus Review Focus §1–7 + “apakah validasi auth
berubah?”. Output ke `.superpowers/sdd/2026-11-09-ronde6-auth-plan/review-r6.md`.
Temuan Critical/Important → satu fix pass (RED→GREEN + suite hijau + commit).
Minor → ledger `Final: minor (deferred)`. Bila dispatch gagal, self-review dan
catat `Final review: self-review (no subagent tool)`.

- [ ] **Step 6: Commit docs + report**

```powershell
git add README.md TESTING.md .superpowers/sdd/2026-11-09-ronde6-auth-plan
git commit -m "docs(r6): auth redesign screenshots, testing, report"
```

**DoD Task 31:** `PROBE-R6-FINAL-OK`, screenshot dicek visual, 10/10 test,
package + smoke sukses, docs+ledger+report ada, final review 0 Critical/Important,
DB seed dan tree bersih.

---

## Final DoD Ronde 6

```markdown
- [ ] Splash 900×560 dengan logo, ticker, 5 segmen, shape drift, curtain exit.
- [ ] Login 1080×680 split-screen; focus/press/shake/eye toggle bekerja.
- [ ] Register halaman penuh; Login↔Register 3× tanpa flicker/stuck.
- [ ] Validasi & DAO auth identik; error inline; sukses kembali ke login.
- [ ] Logo utuh; palet exact; arc 0; tanpa dependency/font file baru.
- [ ] MotionTest 3 + ReportCompilationTest 7 hijau.
- [ ] Probe R6 (controls/decor/splash/login/auth/final) hijau; screenshot OK.
- [ ] `mvn package` + smoke JAR 15 dtk sukses; DB seed utuh.
- [ ] README/TESTING/ledger/report update; final review bersih.
- [ ] Tree bersih, branch `impl/ronde6-auth` siap merge.
```
