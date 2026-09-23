# DESIGN.md — Neobrutalism UI (FlatLaf / Java Swing)

Dokumen ini jadi acuan visual untuk AI agent saat membangun tiap form. Semua form **wajib** ikut spesifikasi ini, bukan tampilan default FlatLaf/Swing.

---

## 1. Prinsip Neobrutalism

- Border tebal, hitam, solid — **tanpa** sudut membulat (`arc = 0` di semua komponen)
- Shadow **offset keras** (bukan blur) — efek "kartu terangkat", contoh: geser 4px ke kanan-bawah, warna hitam solid
- Warna flat & kontras tinggi — tidak ada gradient, tidak ada shadow blur/soft
- Tipografi tebal (bold/black weight) untuk judul, tegas dan besar
- Komponen terasa "ditempel", bukan melayang halus — saat tombol ditekan, shadow hilang & tombol bergeser sedikit (efek fisik ditekan)

---

## 2. Palet Warna

| Token | Hex | Pemakaian |
|---|---|---|
| `bg` | `#FFFBEA` | Background utama (krem, bukan putih polos) |
| `surface` | `#FFFFFF` | Background card/panel/form |
| `ink` | `#000000` | Semua border & teks utama |
| `primary` | `#FF5A5F` | Tombol aksi utama (Simpan, Login, Bayar) |
| `secondary` | `#4D9DE0` | Tombol aksi sekunder (Edit, Cari) |
| `success` | `#2EC4B6` | Status berhasil, stok aman |
| `warning` | `#FFB703` | Peringatan, stok menipis |
| `danger` | `#E63946` | Hapus, Retur, error |
| `header-table` | `#000000` bg / `#FFFFFF` teks | Header JTable |

Setiap warna dipakai **flat**, tanpa gradient. Kontras teks-vs-background wajib tinggi (teks hitam di atas warna terang, teks putih di atas warna gelap).

---

## 3. Tipografi

- Header/judul form: bold, ukuran besar (20–28pt), font `Segoe UI Black` / `Arial Black` (fallback bawaan OS, tidak perlu bundling font eksternal supaya tidak ribet di NetBeans)
- Body/label/isi tabel: `Segoe UI Semibold` atau `Segoe UI` bold, 12–13pt
- Angka penting (total, kembalian di POS): besar & bold, 18–24pt, warna `primary`

---

## 4. Setup FlatLaf — kode dasar (jalankan sebelum JFrame pertama dibuat)

```java
public class NeoBrutalTheme {
    public static final Color BG        = Color.decode("#FFFBEA");
    public static final Color SURFACE   = Color.WHITE;
    public static final Color INK       = Color.BLACK;
    public static final Color PRIMARY   = Color.decode("#FF5A5F");
    public static final Color SECONDARY = Color.decode("#4D9DE0");
    public static final Color SUCCESS   = Color.decode("#2EC4B6");
    public static final Color WARNING   = Color.decode("#FFB703");
    public static final Color DANGER    = Color.decode("#E63946");

    public static void apply() {
        FlatLightLaf.setup();

        // Sudut tegas, tanpa rounded corner
        UIManager.put("Button.arc", 0);
        UIManager.put("Component.arc", 0);
        UIManager.put("ProgressBar.arc", 0);
        UIManager.put("TextComponent.arc", 0);
        UIManager.put("CheckBox.arc", 0);
        UIManager.put("ScrollBar.thumbArc", 0);

        // Border tebal, tanpa efek focus glow bawaan
        UIManager.put("Component.focusWidth", 0);
        UIManager.put("Component.innerFocusWidth", 0);
        UIManager.put("Component.borderWidth", 2);
        UIManager.put("Button.borderWidth", 2);
        UIManager.put("Component.borderColor", INK);
        UIManager.put("Button.borderColor", INK);
        UIManager.put("Button.default.borderColor", INK);

        // Warna dasar
        UIManager.put("Panel.background", BG);
        UIManager.put("Button.background", SURFACE);
        UIManager.put("Button.foreground", INK);
        UIManager.put("Button.default.background", PRIMARY);
        UIManager.put("Button.default.foreground", INK);
        UIManager.put("TextField.background", SURFACE);
        UIManager.put("ComboBox.background", SURFACE);

        // Tabel
        UIManager.put("Table.gridColor", INK);
        UIManager.put("Table.showHorizontalLines", true);
        UIManager.put("Table.showVerticalLines", true);
        UIManager.put("TableHeader.background", INK);
        UIManager.put("TableHeader.foreground", Color.WHITE);
        UIManager.put("TableHeader.font", new Font("Segoe UI", Font.BOLD, 13));

        // Font default
        UIManager.put("defaultFont", new Font("Segoe UI Semibold", Font.PLAIN, 13));
    }
}
```

Panggil `NeoBrutalTheme.apply()` di baris pertama `main()`, sebelum `SwingUtilities.invokeLater(...)`.

---

## 5. Komponen Shadow Offset (wajib dipakai di card/panel/tombol penting)

FlatLaf tidak punya efek shadow-offset bawaan — buat `Border` custom:

```java
public class NeoShadowBorder extends AbstractBorder {
    private final int offset;
    private final Color shadowColor;
    private final Color lineColor;

    public NeoShadowBorder() {
        this(4, Color.BLACK, Color.BLACK);
    }

    public NeoShadowBorder(int offset, Color shadowColor, Color lineColor) {
        this.offset = offset;
        this.shadowColor = shadowColor;
        this.lineColor = lineColor;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
        Graphics2D g2 = (Graphics2D) g.create();
        // blok shadow solid di belakang, digeser offset
        g2.setColor(shadowColor);
        g2.fillRect(x + offset, y + offset, w - offset, h - offset);
        // outline utama komponen
        g2.setColor(lineColor);
        g2.setStroke(new BasicStroke(2));
        g2.drawRect(x, y, w - offset - 2, h - offset - 2);
        g2.dispose();
    }

    @Override
    public Insets getBorderInsets(Component c) {
        return new Insets(4, 4, offset + 4, offset + 4);
    }
}
```

**Pemakaian:** pasang `panel.setBorder(new NeoShadowBorder());` pada:
- Card form input (panel tempat JTextField dkk)
- Tombol utama (Simpan, Login, Bayar) — kombinasikan dengan `MouseListener` yang menghilangkan shadow (`offset=0`) saat `mousePressed`, kembalikan saat `mouseReleased`, supaya terasa "ditekan"
- Panel total/kembalian di POS

---

## 6. Icon Library

Neobrutalism butuh ikon **tebal & solid (filled)**, bukan outline tipis — pakai **Ikonli** (`org.kordamp.ikonli`), library icon font khusus Swing.

**Dependency:** `ikonli-swing-3.x.jar` + `ikonli-materialdesign2-pack-3.x.jar` (set ikon filled, cocok buat brutalist)

```java
import org.kordamp.ikonli.swing.FontIcon;
import org.kordamp.ikonli.materialdesign2.MaterialDesignB;

FontIcon icon = FontIcon.of(MaterialDesignB.BOOK_OPEN_VARIANT, 24, Color.BLACK);
JButton btnBuku = new JButton("Buku", icon);
btnBuku.setIconTextGap(8);
```

**Ikon per menu:**

| Menu | Ikon Ikonli (Material Design 2) |
|---|---|
| Buku | `MaterialDesignB.BOOK_OPEN_VARIANT` |
| Kategori | `MaterialDesignT.TAG` |
| Penerbit | `MaterialDesignD.DOMAIN` |
| Supplier | `MaterialDesignT.TRUCK` |
| Member | `MaterialDesignA.ACCOUNT_GROUP` |
| Penjualan (POS) | `MaterialDesignC.CASH_REGISTER` |
| Pembelian | `MaterialDesignC.CART_ARROW_DOWN` |
| Retur | `MaterialDesignK.KEYBOARD_RETURN` |
| Laporan | `MaterialDesignC.CHART_BAR` |
| Logout | `MaterialDesignL.LOGOUT` |

Semua ikon dirender solid hitam (di atas background terang) atau putih (di sidebar gelap), ukuran konsisten 20–24px, tanpa gradasi/shading.

---

## 7. Ilustrasi Halaman Login

Neobrutalism biasanya pakai ilustrasi flat/vector, bukan foto realistis — foto dengan lighting & gradient halus bentrok sama gaya flat-kontras-tinggi. Dua opsi, pilih satu:

**Opsi A (direkomendasikan) — Ilustrasi flat SVG dari unDraw**
- https://undraw.co — ilustrasi flat gratis, boleh dipakai komersial tanpa atribusi wajib, warnanya bisa di-custom langsung di situs ke warna `primary`/`secondary` (§2) sebelum di-download
- Cari ilustrasi tema "reading", "books", atau "library"
- Simpan sebagai `.svg` di `src/main/resources/images/login_illustration.svg`
- Render pakai `FlatSVGIcon` (dari `com.formdev:flatlaf-extras`) supaya tetap tajam di resolusi berapa pun:
```java
import com.formdev.flatlaf.extras.FlatSVGIcon;

FlatSVGIcon illustration = new FlatSVGIcon("images/login_illustration.svg", 320, 320);
JLabel lblIllustration = new JLabel(illustration);
```

**Opsi B — Foto dari Unsplash**
- Kalau tetap mau foto asli: cari di https://unsplash.com keyword "bookstore", "books shelf", atau "library interior" (gratis, lisensi Unsplash)
- Download manual — agent coding biasanya tidak punya akses download gambar dari internet saat build — lalu taruh di `src/main/resources/images/login_bg.jpg`
- Supaya tetap nyambung ke gaya brutalist: pasang **border hitam tebal 4px** di sekeliling foto, dan taruh foto di panel terpisah di samping form (bukan sebagai background penuh di belakang teks)

**Fallback tanpa internet:** kalau agent tidak bisa ambil aset dari luar sama sekali, buat placeholder — panel kotak solid warna `secondary` dengan `NeoShadowBorder`, isi satu ikon besar Ikonli (misal `MaterialDesignB.BOOK_OPEN_PAGE_VARIANT`, ukuran 120px) di tengahnya. Aset asli tinggal ganti belakangan.

---

## 8. Spesifikasi per Layar

### Login
- Card login di tengah layar, `NeoShadowBorder`, background `surface`
- Di samping/atas card: ilustrasi atau foto dari §7
- Judul aplikasi bold besar di atas card
- Field username/password border tebal hitam, sudut tegas
- Tombol "Login" warna `primary`, shadow border, full-width di dalam card, pakai ikon `MaterialDesignL.LOGIN` dari §6

### Menu Utama
- Sidebar kiri (bukan menu bar polos) — daftar menu sebagai tombol besar bertumpuk, tiap tombol `NeoShadowBorder`
- Menu yang tidak diizinkan untuk role aktif disembunyikan/disable (lihat PRD §5), bukan sekadar warna pudar
- Area konten kanan: background `bg`, form dibuka sebagai panel/JInternalFrame di sini

### Form Master (Buku, Kategori, Penerbit, Supplier, Member)
- Layout dua kolom: kiri form input (card `NeoShadowBorder`), kanan JTable data
- Search bar di atas tabel, border tebal
- Tombol aksi (Tambah/Edit/Hapus) berjajar di bawah form, warna sesuai palet (`primary` = simpan, `secondary` = edit, `danger` = hapus)

### Form Transaksi Penjualan (POS)
- Kiri: kolom pencarian buku + hasil pencarian
- Tengah: JTable keranjang belanja
- Kanan: panel ringkasan (`NeoShadowBorder`, background `primary` muda atau outline tebal) — Total, Bayar, Kembalian ditulis besar & bold
- Tombol "Bayar/Simpan" besar, warna `primary`, full-width di panel kanan

### Form Laporan
- Filter tanggal di atas (dari–sampai), tombol "Tampilkan" warna `secondary`
- Tombol "Cetak/Export PDF" warna `success`
- Setelah generate, buka `JasperViewer` (window terpisah) — tidak perlu di-restyle karena itu window bawaan JasperReports

---

## 9. Checklist Konsistensi (dicek di Fase 6 PRD)

- [ ] Tidak ada komponen dengan sudut membulat
- [ ] Semua card/tombol penting punya shadow offset
- [ ] Palet warna di §2 dipakai konsisten, tidak ada warna baru yang muncul asal
- [ ] Header tabel hitam-teks putih di semua form
- [ ] Font bold dipakai konsisten untuk judul & angka penting
- [ ] Semua menu/tombol pakai ikon Ikonli sesuai tabel §6, bukan campur-campur icon set
- [ ] Halaman Login punya ilustrasi/foto sesuai §7 (bukan kosong polos)
