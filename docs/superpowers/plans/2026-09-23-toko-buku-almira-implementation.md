# Sistem Toko Buku CV Almira Jaya Abadi — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Membangun aplikasi desktop Java Swing sistem informasi manajemen penjualan toko buku (CV Almira Jaya Abadi) dari Fase 0 sampai Fase 7 sesuai PRD, dengan UI neobrutalism sesuai DESIGN.md.

**Architecture:** Layered MVC + DAO: `view` (JFrame/JDialog Swing) → `dao` (CRUD per entity) → `koneksi.Koneksi` (JDBC singleton) → MySQL `db_toko_buku`; `report.ReportHelper` (JasperReports via `JRBeanCollectionDataSource`); `util` (Sesi, HashUtil SHA-256, Validasi, NotaGenerator, NeoBrutalTheme, NeoShadowBorder). Setiap fase berurutan dan terkunci DoD sebelum lanjut.

**Tech Stack:** Java 11 (JDK 25 terinstal, kompatibel), Maven (build user via Maven bawaan NetBeans; verifikasi agent via portable Maven di luar repo bila tersedia), FlatLaf 3.4, Ikonli 12.3.1, mysql-connector-j 8.4.0, JasperReports 6.21.3, LGoodDatePicker 11.2.1, MySQL/MariaDB 10.4.32 XAMPP (`root`/tanpa password, `localhost:3306`).

**Spec:** `docs/prd.md` (fungsional, fase, DoD) + `docs/design.md` (visual neobrutalism). Plan ini turunan langsung keduanya; executor wajib membaca keduanya.

## Global Constraints

- `pom.xml`: `com.tokobuku:sistem-toko-buku:1.0.0`, `packaging jar`, compiler source/target `11`, encoding `UTF-8`. Dependency HANYA: `flatlaf:3.4`, `flatlaf-extras:3.4`, `ikonli-swing:12.3.1`, `ikonli-materialdesign2-pack:12.3.1`, `mysql-connector-j:8.4.0`, `jasperreports:6.21.3`, `LGoodDatePicker:11.2.1`, plugin `maven-shade-plugin:3.5.1` dengan `mainClass view.Login`. DILARANG tambah dependency (termasuk JUnit/BCrypt).
- Schema: TEPAT 11 tabel sesuai `database/schema.sql` (PRD §3) — `users, kategori, penerbit, supplier, member, buku, penjualan, detail_penjualan, pembelian, detail_pembelian, retur`. DILARANG ubah nama tabel/kolom.
- Package flat di `src/main/java`: `koneksi, model, dao, view, util, report`. Resources di `src/main/resources/{images,reports}`.
- Password: hash SHA-256 stdlib (`java.security.MessageDigest`), tidak ada plain-text dari aplikasi.
- Neobrutalism: `arc = 0` semua komponen; border hitam tebal; `NeoShadowBorder` offset 4 di card/tombol penting; palet `bg #FFFBEA, surface #FFFFFF, ink #000000, primary #FF5A5F, secondary #4D9DE0, success #2EC4B6, warning #FFB703, danger #E63946`; header tabel hitam/teks putih; font `Segoe UI Black` judul 20–28pt, `Segoe UI Semibold` body 12–13pt; ikon HANYA Ikonli MaterialDesign2 sesuai tabel DESIGN §6; `NeoBrutalTheme.apply()` baris pertama `main()`.
- `APP_NAME = "CV Almira Jaya Abadi"` (konstanta `util.AppConfig`), dipakai judul window + laporan.
- Nota penjualan `NJ-YYYYMMDD-XXXX` (contoh `NJ-20260923-0001`); faktur pembelian `FB-YYYYMMDD-XXXX`; no retur `RT-YYYYMMDD-XXXX`; sequence per hari, unik (kolom UNIQUE di DB).
- Threshold stok menipis: `stok <= 5` (konstanta `STOK_MENIPIS = 5` di `util.AppConfig`).
- Role (PRD §5): Admin = semua; Kasir = Penjualan + Retur (input) + Laporan Penjualan miliknya saja. Menu terlarang disembunyikan/disable.
- Fase dikerjakan BERURUTAN 0→7; setiap task berakhir dengan laporan checklist DoD; dilarang loncat fase.
- Jika API library (JasperReports/LGoodDatePicker/FlatLaf) tidak pasti: query Context7 dulu sebelum tulis kode (resolve library ID → query-docs).
- Testing = verifikasi DoD per PRD (compile + run + cek DB manual); tidak ada framework unit test (dilarang tambah dep).

---

## File Map (dibuat selama Task 1–8)

```
pom.xml
database/schema.sql
README.md
src/main/java/koneksi/Koneksi.java
src/main/java/util/{AppConfig.java, Sesi.java, HashUtil.java, Validasi.java,
  NotaGenerator.java, NeoBrutalTheme.java, NeoShadowBorder.java}
src/main/java/model/{User.java, Kategori.java, Penerbit.java, Supplier.java,
  Member.java, Buku.java, Penjualan.java, DetailPenjualan.java, Pembelian.java,
  DetailPembelian.java, Retur.java,
  LapPenjualan.java, LapPembelian.java, LapStok.java, LapPendapatan.java, LapTerlaris.java}
src/main/java/dao/{UserDAO.java, UserDAOImpl.java, KategoriDAO.java, KategoriDAOImpl.java,
  PenerbitDAO.java, PenerbitDAOImpl.java, SupplierDAO.java, SupplierDAOImpl.java,
  MemberDAO.java, MemberDAOImpl.java, BukuDAO.java, BukuDAOImpl.java,
  PenjualanDAO.java, PenjualanDAOImpl.java, PembelianDAO.java, PembelianDAOImpl.java,
  ReturDAO.java, ReturDAOImpl.java}
src/main/java/view/{Login.java, MenuUtama.java, FormBuku.java, FormKategori.java,
  FormPenerbit.java, FormSupplier.java, FormMember.java, FormPenjualan.java,
  FormPembelian.java, FormRetur.java, FormLaporan.java, StrukDialog.java}
src/main/java/report/ReportHelper.java
src/main/resources/reports/{lap_data_buku.jrxml, lap_penjualan.jrxml, lap_pembelian.jrxml,
  lap_stok.jrxml, lap_pendapatan.jrxml, lap_terlaris.jrxml}
src/main/resources/images/login_bg.jpg   (diunduh MANUAL oleh user dari Unsplash;
  kode Login wajib fallback ke panel ikon bila file tidak ada)
```

---

### Task 1: Fase 0 — Setup Environment

**Files:**
- Create: `pom.xml`, `database/schema.sql`, `src/main/java/koneksi/Koneksi.java`, `src/main/java/view/TempCheck.java` (sementara, dihapus di Task 3)
- Test: console + `mysql` CLI + NetBeans Build output

**Interfaces:**
- Consumes: tidak ada (task pertama)
- Produces: `koneksi.Koneksi.getConnection() : java.sql.Connection` (dipakai semua DAO Task 2+); database `db_toko_buku` dengan 11 tabel + seed admin ter-hash

- [ ] **Step 1: Tulis `pom.xml` (salinan persis PRD §2)**

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0">
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.tokobuku</groupId>
    <artifactId>sistem-toko-buku</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>
    <properties>
        <maven.compiler.source>11</maven.compiler.source>
        <maven.compiler.target>11</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>
    <dependencies>
        <dependency><groupId>com.formdev</groupId><artifactId>flatlaf</artifactId><version>3.4</version></dependency>
        <dependency><groupId>com.formdev</groupId><artifactId>flatlaf-extras</artifactId><version>3.4</version></dependency>
        <dependency><groupId>org.kordamp.ikonli</groupId><artifactId>ikonli-swing</artifactId><version>12.3.1</version></dependency>
        <dependency><groupId>org.kordamp.ikonli</groupId><artifactId>ikonli-materialdesign2-pack</artifactId><version>12.3.1</version></dependency>
        <dependency><groupId>com.mysql</groupId><artifactId>mysql-connector-j</artifactId><version>8.4.0</version></dependency>
        <dependency><groupId>net.sf.jasperreports</groupId><artifactId>jasperreports</artifactId><version>6.21.3</version></dependency>
        <dependency><groupId>com.github.lgooddatepicker</groupId><artifactId>LGoodDatePicker</artifactId><version>11.2.1</version></dependency>
    </dependencies>
    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId><artifactId>maven-shade-plugin</artifactId><version>3.5.1</version>
                <executions><execution><phase>package</phase><goals><goal>shade</goal></goals>
                    <configuration><transformers>
                        <transformer implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
                            <mainClass>view.Login</mainClass>
                        </transformer>
                    </transformers></configuration>
                </execution></executions>
            </plugin>
        </plugins>
    </build>
</project>
```

- [ ] **Step 2: Tulis `database/schema.sql` (salinan persis PRD §3 — 11x `CREATE TABLE` + 1x `INSERT` seed admin)**

Isi = blok SQL PRD §3 verbatim (`users, kategori, penerbit, supplier, member, buku, penjualan, detail_penjualan, pembelian, detail_pembelian, retur` + `INSERT INTO users ... ('admin','admin123','Administrator','Admin')`).

- [ ] **Step 3: Buat database + import + hash seed admin**

```powershell
mysql -u root -e "CREATE DATABASE IF NOT EXISTS db_toko_buku;"
mysql -u root db_toko_buku < database/schema.sql
mysql -u root db_toko_buku -e "SHOW TABLES;"
mysql -u root db_toko_buku -e "UPDATE users SET password = SHA2('admin123', 256) WHERE username = 'admin';"
```

Expected: `SHOW TABLES` menampilkan tepat 11 tabel. (`SHA2(...,256)` MySQL = hex lowercase = identik dengan SHA-256 Java di `HashUtil`, sehingga login konsisten.)

- [ ] **Step 4: Tulis `src/main/java/koneksi/Koneksi.java`**

```java
package koneksi;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Koneksi {
    private static final String URL = "jdbc:mysql://localhost:3306/db_toko_buku?serverTimezone=Asia/Jakarta";
    private static final String USER = "root";
    private static final String PASS = "";
    private static Connection conn;

    private Koneksi() {}

    public static synchronized Connection getConnection() throws SQLException {
        if (conn == null || conn.isClosed()) {
            conn = DriverManager.getConnection(URL, USER, PASS);
        }
        return conn;
    }

    public static void main(String[] args) {
        try {
            getConnection();
            System.out.println("Connected");
        } catch (SQLException e) {
            System.err.println("Connection failed: " + e.getMessage());
        }
    }
}
```

- [ ] **Step 5: Tulis `src/main/java/view/TempCheck.java` (JFrame kosong sementara)**

```java
package view;

import javax.swing.JFrame;

public class TempCheck extends JFrame {
    public TempCheck() {
        super("TempCheck");
        setSize(400, 300);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> new TempCheck().setVisible(true));
    }
}
```

- [ ] **Step 6: Verifikasi compile + run**

Coba portable Maven di luar repo bila bisa diunduh (`$HOME/tools/apache-maven`), lalu `mvn -q compile`. Bila tidak ada network: verifikasi utama = NetBeans (Open Project → Build with Dependencies sukses). Run `TempCheck` (NetBeans Run File) → JFrame kosong tampil tanpa error. Run `Koneksi.main` → console `Connected`.

Expected: compile sukses, 0 error; `SHOW TABLES` = 11 tabel.

- [ ] **Step 7: Lapor DoD Fase 0**

```markdown
- [x]/[ ] pom.xml dibuat, compile sukses tanpa error
- [x]/[ ] Terbuka sebagai Maven Project di NetBeans
- [x]/[ ] JFrame kosong run tanpa error
- [x]/[ ] schema.sql di-import, SHOW TABLES = 11 tabel
- [x]/[ ] Koneksi.java print "Connected"
```

---

### Task 2: Fase 1 — Layer Model & DAO

**Files:**
- Create: 11 POJO `model/*.java`, 9 DAO interface + 9 impl (`User, Kategori, Penerbit, Supplier, Member, Buku, Penjualan, Pembelian, Retur`), 5 bean laporan `model/Lap*.java`, query laporan di `PenjualanDAOImpl/PembelianDAOImpl/BukuDAOImpl`
- Test: `main()` sementara per DAO-impl (atau satu `dao.DaoSmokeTest` sementara, dihapus akhir task)

**Interfaces:**
- Consumes: `koneksi.Koneksi.getConnection()`
- Produces: pola DAO seragam per entity — `insert(T):boolean`, `update(T):boolean`, `delete(int):boolean`, `getAll():List<T>`, `getById(int):T`, `search(String):List<T>`; plus:
  - `PenjualanDAO.saveWithDetail(Penjualan h, List<DetailPenjualan> d): int` (1 transaksi: insert header → ambil generated key → insert detail → update `buku.stok = stok - qty`; rollback bila gagal)
  - `PembelianDAO.saveWithDetail(Pembelian h, List<DetailPembelian> d): int` (sama, `stok + qty`)
  - `ReturDAO.saveRetur(Retur r): boolean` (insert retur + `stok + qty`)
  - `BukuDAO.getStokMenipis(): List<LapStok>`, `PenjualanDAO.lapPenjualan(LocalDate a, LocalDate b): List<LapPenjualan>`, `PenjualanDAO.lapPendapatan(a,b): List<LapPendapatan>`, `PenjualanDAO.lapTerlaris(a,b): List<LapTerlaris>`, `PembelianDAO.lapPembelian(a,b): List<LapPembelian>`, `BukuDAO.lapDataBuku(): List<LapStok>`

- [ ] **Step 1: Tulis 11 POJO sesuai kolom tabel (contoh penuh `model/Buku.java`, sisanya ikut pola)**

```java
package model;

public class Buku {
    private int idBuku;
    private String kodeBuku;
    private String judul;
    private String penulis;
    private Integer idPenerbit;   // nullable (FK)
    private Integer idKategori;   // nullable (FK)
    private String namaPenerbit;  // display hasil JOIN, bukan kolom
    private String namaKategori;  // display hasil JOIN, bukan kolom
    private double hargaBeli;
    private double hargaJual;
    private int stok;
    // + constructor kosong, constructor penuh, getter/setter semua field
    public int getIdBuku() { return idBuku; }
    public void setIdBuku(int v) { this.idBuku = v; }
    // ... dst untuk semua field
}
```

Field per POJO (wajib lengkap getter/setter): `User(idUser,username,password,namaLengkap,role)`, `Kategori(idKategori,namaKategori)`, `Penerbit(idPenerbit,namaPenerbit,alamat,noTelp)`, `Supplier(idSupplier,namaSupplier,alamat,noTelp)`, `Member(idMember,kodeMember,nama,alamat,noTelp)`, `Penjualan(idPenjualan,noNota,tanggal:LocalDateTime,idUser,username,idMember,kodeMember,total,bayar,kembalian)`, `DetailPenjualan(idDetail,idPenjualan,idBuku,kodeBuku,judul,qty,hargaJual,subtotal)`, `Pembelian(idPembelian,noFaktur,tanggal,idSupplier,namaSupplier,idUser,username,total)`, `DetailPembelian(idDetail,idPembelian,idBuku,kodeBuku,judul,qty,hargaBeli,subtotal)`, `Retur(idRetur,noRetur,tanggal,idPenjualan,noNota,idBuku,kodeBuku,judul,qty,alasan)`. Bean laporan: `LapPenjualan(noNota,tanggal,kasir,member,total)`, `LapPembelian(noFaktur,tanggal,supplier,kasir,total)`, `LapStok(kodeBuku,judul,kategori,penerbit,hargaBeli,hargaJual,stok)`, `LapPendapatan(noNota,tanggal,judul,qty,hargaBeli,hargaJual,laba)` dengan `laba=(hargaJual-hargaBeli)*qty`, `LapTerlaris(kodeBuku,judul,totalQty,totalOmzet)`.

- [ ] **Step 2: Tulis interface DAO per entity (contoh penuh, 8 lainnya ikut pola)**

```java
package dao;

import java.util.List;
import model.Buku;

public interface BukuDAO {
    boolean insert(Buku b);
    boolean update(Buku b);
    boolean delete(int id);
    List<Buku> getAll();
    Buku getById(int id);
    List<Buku> search(String keyword);
    List<model.LapStok> lapDataBuku();
    List<model.LapStok> getStokMenipis();
}
```

- [ ] **Step 3: Tulis impl DAO (`PreparedStatement`, try-with-resources, tutup resource) — contoh penuh `BukuDAOImpl.search`, pola yang sama untuk semua method**

```java
@Override
public List<Buku> search(String keyword) {
    List<Buku> list = new ArrayList<>();
    String sql = "SELECT b.*, k.nama_kategori, p.nama_penerbit FROM buku b "
        + "LEFT JOIN kategori k ON b.id_kategori=k.id_kategori "
        + "LEFT JOIN penerbit p ON b.id_penerbit=p.id_penerbit "
        + "WHERE b.kode_buku LIKE ? OR b.judul LIKE ? OR b.penulis LIKE ?";
    try (Connection c = Koneksi.getConnection();
         PreparedStatement ps = c.prepareStatement(sql)) {
        String kw = "%" + keyword + "%";
        ps.setString(1, kw); ps.setString(2, kw); ps.setString(3, kw);
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) { list.add(map(rs)); }
        }
    } catch (SQLException e) { throw new RuntimeException("search buku gagal: " + e.getMessage(), e); }
    return list;
}
```

`saveWithDetail` (Penjualan) wajib: `setAutoCommit(false)` → insert header (`Statement.RETURN_GENERATED_KEYS`) → insert tiap detail → `UPDATE buku SET stok=stok-? WHERE id_buku=? AND stok>=?` (gagal bila affected-rows=0 → rollback + throw "Stok tidak mencukupi") → `commit()`; `catch` → `rollback()`. Retur/pembelian analog (`stok+qty`, tanpa cek stok untuk tambah).

- [ ] **Step 4: Smoke test tiap DAO via `main()` sementara, lalu hapus file smoke test**

Run per impl: insert → getById → search → update → delete, tanpa exception ke DB sungguhan (pakai data dummy `TEST-xxx`, bersihkan setelahnya).

Expected: semua method lolos tanpa exception.

- [ ] **Step 5: Lapor DoD Fase 1**

```markdown
- [x]/[ ] Semua POJO sesuai kolom tabel
- [x]/[ ] Setiap entity punya DAO insert/update/delete/getAll/getById/search
- [x]/[ ] Semua DAO tested manual tanpa exception
```

---

### Task 3: Fase 2 — Login & Role

**Files:**
- Create: `util/{AppConfig.java, Sesi.java, HashUtil.java, NeoBrutalTheme.java, NeoShadowBorder.java}`, `view/{Login.java, MenuUtama.java}`
- Delete: `view/TempCheck.java`
- Test: run Login → login admin/admin123 → MenuUtama → logout

**Interfaces:**
- Consumes: `UserDAO.getByUsername(String): User` (tambah method ini ke `UserDAO`+impl di task ini: `SELECT * FROM users WHERE username=?`), `HashUtil.sha256(String):String`, `NeoBrutalTheme.apply()`
- Produces: `util.Sesi.userLogin: User`, `util.Sesi.role: String`, `Sesi.clear()`; `view.MenuUtama(User)` entry point aplikasi; `AppConfig.APP_NAME`

- [ ] **Step 1: Tulis `util/AppConfig.java`, `util/Sesi.java`, `util/HashUtil.java`**

```java
package util;
public class AppConfig {
    public static final String APP_NAME = "CV Almira Jaya Abadi";
    public static final int STOK_MENIPIS = 5;
}
```

```java
package util;
import model.User;
public class Sesi {
    public static User userLogin;
    public static String role;
    public static void clear() { userLogin = null; role = null; }
    public static boolean isAdmin() { return "Admin".equals(role); }
}
```

```java
package util;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
public class HashUtil {
    public static String sha256(String plain) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] h = md.digest(plain.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte x : h) sb.append(String.format("%02x", x));
            return sb.toString();
        } catch (Exception e) { throw new RuntimeException("SHA-256 gagal", e); }
    }
}
```

- [ ] **Step 2: Tulis `util/NeoBrutalTheme.java` + `util/NeoShadowBorder.java` (verbatim DESIGN.md §4–§5)**

Salin kode `NeoBrutalTheme` (token warna + `UIManager.put` arc 0, border tebal, header tabel hitam/putih, font default) dan `NeoShadowBorder` (offset 4, shadow solid, outline 2px) persis dari DESIGN.md.

- [ ] **Step 3: Tulis `view/Login.java`**

Spesifikasi: `main()` baris pertama `NeoBrutalTheme.apply()`; card tengah `surface` + `NeoShadowBorder`; kiri/atas card panel ilustrasi — coba load `/images/login_bg.jpg` dari classpath, bingkai `LineBorder(BLACK,4)`; bila `getResource` null → fallback panel `secondary` + ikon `BOOK_OPEN_PAGE_VARIANT` 120px; field username/password border hitam tebal; tombol Login `primary` full-width + ikon `MaterialDesignL.LOGIN` + efek tekan (mousePressed → border offset 0, mouseReleased → kembalikan); autentikasi `UserDAO.getByUsername` + bandingkan `HashUtil.sha256(input)` vs kolom password; gagal → `JOptionPane` error + tetap di Login; sukses → isi `Sesi` → buka `MenuUtama` → dispose Login. Judul window = `APP_NAME + " - Login"`.

- [ ] **Step 4: Tulis `view/MenuUtama.java` (sidebar + konten)**

Sidebar kiri: tombol besar bertumpuk + `NeoShadowBorder` + ikon DESIGN §6 (Buku `BOOK_OPEN_VARIANT`, Kategori `TAG`, Penerbit `DOMAIN`, Supplier `TRUCK`, Member `ACCOUNT_GROUP`, Penjualan `CASH_REGISTER`, Pembelian `CART_ARROW_DOWN`, Retur `KEYBOARD_RETURN`, Laporan `CHART_BAR`, Logout `LOGOUT`), 20–24px. Gating role: bila Kasir → `setVisible(false)` untuk Buku/Kategori/Penerbit/Supplier/Member/Pembelian/User + menu Laporan hanya buka `FormLaporan` mode kasir (hanya Laporan Penjualan miliknya, filter `id_user = Sesi.userLogin`). Konten kanan `bg`, form dibuka sebagai panel. Logout → `Sesi.clear()` → buka `Login`.

- [ ] **Step 5: Hapus `view/TempCheck.java`; verifikasi + lapor DoD Fase 2**

```markdown
- [x]/[ ] Login neobrutalism autentikasi ke users, password hash
- [x]/[ ] Login salah → error, tidak lanjut
- [x]/[ ] Sesi menyimpan user & role
- [x]/[ ] Menu tampil/sembunyi sesuai role §5
- [x]/[ ] Logout kosongkan sesi → Login
```

---

### Task 4: Fase 3 — Form Master (5 form)

**Files:**
- Create: `view/{FormBuku.java, FormKategori.java, FormPenerbit.java, FormSupplier.java, FormMember.java}`, `util/Validasi.java`
- Test: buka tiap form dari MenuUtama → CRUD + search live

**Interfaces:**
- Consumes: DAO entity terkait; `Validasi.wajib(JTextField...):boolean`, `Validasi.angka(JTextField):boolean`
- Produces: panel siap tanam di konten `MenuUtama` (constructor tanpa argumen + `loadTable()` publik)

- [ ] **Step 1: Tulis `util/Validasi.java`**

```java
package util;
import javax.swing.*;
public class Validasi {
    public static boolean wajib(JTextField... fs) {
        for (JTextField f : fs)
            if (f.getText().trim().isEmpty()) { JOptionPane.showMessageDialog(null, "Field wajib tidak boleh kosong: " + f.getName()); f.requestFocus(); return false; }
        return true;
    }
    public static boolean angka(JTextField f) {
        try { Double.parseDouble(f.getText().trim()); return true; }
        catch (NumberFormatException e) { JOptionPane.showMessageDialog(null, "Harus angka: " + f.getName()); f.requestFocus(); return false; }
    }
    public static boolean integer(JTextField f) {
        try { Integer.parseInt(f.getText().trim()); return true; }
        catch (NumberFormatException e) { JOptionPane.showMessageDialog(null, "Harus bilangan bulat: " + f.getName()); f.requestFocus(); return false; }
    }
}
```

- [ ] **Step 2–6: Tulis kelima form (satu step per form, pola identik)**

Layout dua kolom: kiri card input (`NeoShadowBorder`), kanan `JTable` (header hitam/putih dari tema) + search bar di atas (DocumentListener → `dao.search()` live). Tombol: Simpan `primary`, Edit `secondary`, Hapus `danger` (+ `JOptionPane.showConfirmDialog` sebelum delete). Klik baris → isi form → Edit/Update. `FormBuku`: combo penerbit+kategori (display nama, simpan id; opsi kosong = NULL), field kode_buku unik (tangkap `SQLIntegrityConstraintViolationException` → pesan "Kode sudah dipakai"), harga_beli/harga_jual `Validasi.angka`, stok `Validasi.integer`. `FormMember`: kode_member unik. Setiap simpan/update/delete → `loadTable()` refresh.

- [ ] **Step 7: Verifikasi + lapor DoD Fase 3 (centang per form: tabel load, tambah+validasi, edit via klik baris, hapus+konfirmasi, search live)**

---

### Task 5: Fase 4 — Transaksi (POS, Pembelian, Retur)

**Files:**
- Create: `util/NotaGenerator.java`, `view/{FormPenjualan.java, FormPembelian.java, FormRetur.java, StrukDialog.java}`
- Test: skenario POS penuh + pembelian + retur ke DB sungguhan (data uji, stok dicek sebelum/sesudah)

**Interfaces:**
- Consumes: `BukuDAO.search`, `PenjualanDAO.saveWithDetail`, `PembelianDAO.saveWithDetail`, `ReturDAO.saveRetur`, `PenjualanDAO.getByNoNota(String):Penjualan` + `getDetailByPenjualan(int):List<DetailPenjualan>` (tambah 2 method ini di task ini)
- Produces: stok konsisten; `no_nota/no_faktur/no_retur` unik

- [ ] **Step 1: Tulis `util/NotaGenerator.java`**

```java
package util;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import koneksi.Koneksi;
public class NotaGenerator {
    public static synchronized String next(String prefix, String table, String column) throws SQLException {
        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String sql = "SELECT " + column + " FROM " + table + " WHERE " + column + " LIKE ? ORDER BY " + column + " DESC LIMIT 1";
        // table/column hanya dari konstanta internal: ("NJ","penjualan","no_nota"),
        // ("FB","pembelian","no_faktur"), ("RT","retur","no_retur") — bukan input user.
        try (Connection c = Koneksi.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, prefix + "-" + date + "-%");
            try (ResultSet rs = ps.executeQuery()) {
                int seq = rs.next() ? Integer.parseInt(rs.getString(1).substring(rs.getString(1).lastIndexOf('-') + 1)) + 1 : 1;
                return String.format("%s-%s-%04d", prefix, date, seq);
            }
        }
    }
}
```

- [ ] **Step 2: Tulis `view/FormPenjualan.java` (POS)**

Kiri: field cari + tombol Cari (`BukuDAO.search`, hasil JList/JTable kecil, double-click/tombol Tambah → keranjang). Tengah: JTable keranjang (kode, judul, harga, qty editable spinner, subtotal). Kanan: panel ringkasan `NeoShadowBorder` — Total/Bayar/Kembalian 18–24pt bold `primary`; field Bayar → kembalian otomatis (`bayar-total`, tolak bayar<total); tombol Bayar/Simpan `primary` full-width: validasi qty tiap baris ≤ stok (cek ulang via `BukuDAO.getById`), `NotaGenerator.next("NJ","penjualan","no_nota")`, bangun `Penjualan(tanggal=now,idUser=Sesi,total,bayar,kembalian)` + detail list → `saveWithDetail` → buka `StrukDialog` → reset form. Combo member opsional (kosong = NULL).

- [ ] **Step 3: Tulis `view/StrukDialog.java` (JDialog ringkasan: APP_NAME, no nota, tanggal, kasir, tabel item, total/bayar/kembalian, tombol Tutup)**

- [ ] **Step 4: Tulis `view/FormPembelian.java`**

Combo supplier (wajib), tambah item (cari buku + qty + harga_beli), total otomatis, `NotaGenerator.next("FB","pembelian","no_faktur")` → `saveWithDetail` (stok bertambah) → konfirmasi.

- [ ] **Step 5: Tulis `view/FormRetur.java`**

Field no nota → Cari (`getByNoNota`; tidak ketemu → error "nota tidak ada") → tampilkan item nota di tabel → pilih baris + qty retur + alasan → validasi `qty_retur ≤ (qty_terjual - qty_sudah_diretur_nota_item_ini)` (query SUM retur per nota+buku) → `NotaGenerator.next("RT","retur","no_retur")` → `saveRetur` (stok bertambah).

- [ ] **Step 6: Verifikasi + lapor DoD Fase 4 (9 item PRD: cari+keranjang, qty≤stok, total/bayar/kembalian, nota unik, simpan kurang stok, struk, supplier+faktur+tambah stok, nota valid, qty retur≤terjual + stok kembali)**

---

### Task 6: Fase 5 — Laporan (JasperReports)

**Files:**
- Create: `src/main/resources/reports/*.jrxml` (6 file), `report/ReportHelper.java`, `view/FormLaporan.java`
- Test: tiap laporan dibuka dari menu dengan filter → JasperViewer tampil + export PDF

**Interfaces:**
- Consumes: bean `model/Lap*.java` + query DAO Task 2; `JRBeanCollectionDataSource`
- Produces: `ReportHelper.show(namaJrxml, List<?> data, Map<String,Object> params)` membuka `JasperViewer` non-modal

- [ ] **Step 1: Tulis `report/ReportHelper.java`**

```java
package report;
import java.util.List;
import java.util.Map;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.view.JasperViewer;

public class ReportHelper {
    public static void show(String jrxmlPath, List<?> data, Map<String, Object> params) throws JRException {
        JasperReport jr = JasperCompileManager.compileReport(
            ReportHelper.class.getResourceAsStream(jrxmlPath));
        JasperPrint jp = JasperFillManager.fillReport(jr, params, new JRBeanCollectionDataSource(data));
        JasperViewer viewer = new JasperViewer(jp, false);
        viewer.setTitle("Laporan - " + jrxmlPath);
        viewer.setVisible(true);
    }
}
```

`jrxmlPath` format `/reports/lap_penjualan.jrxml`. Params wajib: `APP_NAME`, `PERIODE` ("12 Jan 2026 s.d. 20 Jan 2026" / "Semua data" untuk Data Buku).

- [ ] **Step 2: Tulis 6 `.jrxml` (hand-written, kompatibel 6.21.3; pola penuh di bawah untuk Data Buku, 5 lainnya ikut struktur band yang sama dengan field-nya masing-masing)**

Struktur tiap file: `title` (APP_NAME + judul laporan + PERIODE) → `columnHeader` → `detail` → `summary` (total). Contoh field: Data Buku → `kodeBuku, judul, kategori, penerbit, hargaBeli, hargaJual, stok`; Penjualan → `noNota, tanggal, kasir, member, total` + summary SUM; Pembelian → `noFaktur, tanggal, supplier, kasir, total` + SUM; Stok → kolom Data Buku + highlight menipis (`stok <= 5` → background `warning` via `printWhenExpression`); Pendapatan → `noNota, tanggal, judul, qty, hargaBeli, hargaJual, laba` + SUM laba; Terlaris → `kodeBuku, judul, totalQty, totalOmzet` (data sudah terurut dari query). Query SQL final di DAO (bukan di jrxml — data masuk via bean, sesuai flow PRD 8.7).

- [ ] **Step 3: Tulis `view/FormLaporan.java`**

Combo jenis laporan (Admin: 6; Kasir: hanya Penjualan miliknya) + 2 `DatePicker` LGoodDatePicker (dari–sampai; disembunyikan untuk Data Buku) + tombol Tampilkan `secondary` (panggil DAO sesuai jenis + `ReportHelper.show`) + tombol Export PDF `success` (info: export via toolbar JasperViewer; tombol membuka viewer yang sama). Validasi: tanggal dari ≤ sampai.

- [ ] **Step 4: Verifikasi + lapor DoD Fase 5 (6 jrxml terpanggil, filter tanggal kecuali Data Buku, viewer tampil tanpa error, export PDF dari toolbar)**

Catatan Context7: bila API `DatePicker.getDate()`, `JasperViewer`, atau `JRBeanCollectionDataSource` bermasalah → resolve `lgooddatepicker`/`jasperreports` di Context7 dulu, jangan tebak.

---

### Task 7: Fase 6 — Finalisasi UI Neobrutalism

**Files:**
- Modify: semua file `view/*.java` yang belum konsisten
- Test: buka SEMUA screen satu per satu, cek visual

**Interfaces:** Consumes: `NeoBrutalTheme`, `NeoShadowBorder`, tabel ikon DESIGN §6. Produces: tidak ada (polish).

- [ ] **Step 1: Audit tiap form terhadap checklist DESIGN §9 (7 item): tanpa sudut membulat; shadow offset di card/tombol penting + efek tekan; hanya warna palet §2; header tabel hitam/putih; font bold judul & angka; ikon Ikonli sesuai tabel; Login punya ilustrasi (foto/panel fallback)**

- [ ] **Step 2: Perbaiki temuan audit (satu commit per form bila perlu)**

- [ ] **Step 3: Lapor DoD Fase 6 (tema di seluruh form; nol komponen default tersisa; konsisten warna/font/border/shadow)**

---

### Task 8: Fase 7 — Testing & Packaging

**Files:**
- Create: `README.md`
- Modify: `database/schema.sql` (final — sinkron dengan yang dipakai, bila ada perubahan minor selama fase; skema TETAP 11 tabel apa adanya)
- Test: E2E + `package` + `java -jar`

**Interfaces:** Consumes: seluruh hasil Task 1–7. Produces: deliverable akhir PRD §9.

- [ ] **Step 1: Uji E2E manual (login admin → 5 CRUD master → POS → pembelian → retur → 6 laporan → logout; ulangi login kasir → POS → retur → laporan sendiri → logout). Catat crash/bug → perbaiki → ulangi sampai bersih.**

- [ ] **Step 2: Build JAR standalone (NetBeans Clean and Build; hasil shade di `target/sistem-toko-buku-1.0.0.jar`), lalu `java -jar target/sistem-toko-buku-1.0.0.jar` di luar IDE → aplikasi jalan + connect DB.**

- [ ] **Step 3: Tulis `README.md` (setup XAMPP → start MySQL → `CREATE DATABASE` + import `database/schema.sql` → buka di NetBeans sebagai Maven Project → Build → Run; akun default `admin/admin123`; taruh `login_bg.jpg` Unsplash ke `src/main/resources/images/` (opsional, ada fallback); daftar fitur per role)**

- [ ] **Step 4: Lapor DoD Fase 7 + serah terima (source + schema.sql + 6 jrxml + README + JAR di `target/`)**

```markdown
- [x]/[ ] E2E tanpa crash
- [x]/[ ] mvn/NetBeans package → JAR standalone jalan via java -jar
- [x]/[ ] schema.sql final disertakan
- [x]/[ ] README berisi setup XAMPP, import DB, run
```

---

## Self-Review (diisi saat menulis plan)

1. **Spec coverage:** PRD §2 (Task 1) • §3 (Task 1) • §4 (Task 1+2) • §5 (Task 3) • §6 master (Task 4), transaksi (Task 5), report (Task 6) • §7 DoD tiap task • §8 diagram dipakai sebagai acuan alur (flow POS/login/beli/retur/laporan di Task 3/5/6) • §9 (Task 8). DESIGN §2–§9: token/tema (Task 3 Step 2), shadow (Task 3), ikon (Task 3–4), ilustrasi Opsi B+fallback (Task 3), spesifikasi layar (Task 3–6), checklist §9 (Task 7). Tidak ada gap.
2. **Placeholder scan:** tidak ada TBD/TODO; semua step punya kode/perintah konkret; jrxml mengikuti pola struktur band + daftar field eksplisit; validasi/error message konkret.
3. **Type consistency:** `tanggal` = `LocalDateTime` (model) ↔ `DATETIME` (DB) via `Timestamp.valueOf()`; `DECIMAL` ↔ `double` (konsisten dengan class diagram PRD); password = hex SHA-256 lowercase di Java dan MySQL; `Sesi.role` String `"Admin"/"Kasir"` = nilai ENUM DB; nama method DAO identik di interface/impl/task konsumen (`saveWithDetail`, `getByNoNota`, `lap*`).
