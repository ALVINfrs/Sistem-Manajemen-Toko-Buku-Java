package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import org.kordamp.ikonli.materialdesign2.MaterialDesignA;
import org.kordamp.ikonli.materialdesign2.MaterialDesignB;
import org.kordamp.ikonli.materialdesign2.MaterialDesignC;
import org.kordamp.ikonli.materialdesign2.MaterialDesignD;
import org.kordamp.ikonli.materialdesign2.MaterialDesignH;
import org.kordamp.ikonli.materialdesign2.MaterialDesignK;
import org.kordamp.ikonli.materialdesign2.MaterialDesignL;
import org.kordamp.ikonli.materialdesign2.MaterialDesignT;
import org.kordamp.ikonli.materialdesign2.MaterialDesignV;
import org.kordamp.ikonli.swing.FontIcon;
import util.AppConfig;
import util.NeoBrutalTheme;
import util.NeoShadowBorder;
import util.Sesi;

public class MenuUtama extends JFrame {

    private static final Color ACTIVE_BG = new Color(0xBD, 0xE0, 0xFE);
    private static final DateTimeFormatter FMT_JAM = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter FMT_TGL =
            DateTimeFormatter.ofPattern("dd MMM yyyy", new Locale("id", "ID"));

    private final JPanel content;
    private JButton currentActive;
    private final List<JButton> modulButtons = new ArrayList<>();
    private JButton btnDashboard;
    private JButton btnBuku;
    private JButton btnKategori;
    private JButton btnPenerbit;
    private JButton btnSupplier;
    private JButton btnMember;
    private JButton btnUser;
    private JButton btnPenjualan;
    private JButton btnPembelian;
    private JButton btnRetur;
    private JButton btnRiwayat;
    private JButton btnLaporan;
    private JButton btnLogout;
    private JLabel lblMaster;
    private JLabel lblNama;
    private JLabel lblRole;
    private JLabel lblJam;
    private JLabel lblTanggal;
    private JPanel profilCard;

    public MenuUtama() {
        super(AppConfig.APP_NAME + " - Menu Utama");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1050, 700);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);

        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(NeoBrutalTheme.SURFACE);
        sidebar.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        btnDashboard = modulButton("Dashboard", FontIcon.of(MaterialDesignV.VIEW_DASHBOARD, 22, Color.BLACK));
        btnBuku = modulButton("Buku", FontIcon.of(MaterialDesignB.BOOK_OPEN_VARIANT, 22, Color.BLACK));
        btnKategori = modulButton("Kategori", FontIcon.of(MaterialDesignT.TAG, 22, Color.BLACK));
        btnPenerbit = modulButton("Penerbit", FontIcon.of(MaterialDesignD.DOMAIN, 22, Color.BLACK));
        btnSupplier = modulButton("Supplier", FontIcon.of(MaterialDesignT.TRUCK, 22, Color.BLACK));
        btnMember = modulButton("Member", FontIcon.of(MaterialDesignA.ACCOUNT_GROUP, 22, Color.BLACK));
        btnUser = modulButton("User", FontIcon.of(MaterialDesignA.ACCOUNT_COG, 22, Color.BLACK));
        btnPenjualan = modulButton("Penjualan", FontIcon.of(MaterialDesignC.CASH_REGISTER, 22, Color.BLACK));
        btnPembelian = modulButton("Pembelian", FontIcon.of(MaterialDesignC.CART_ARROW_DOWN, 22, Color.BLACK));
        btnRetur = modulButton("Retur", FontIcon.of(MaterialDesignK.KEYBOARD_RETURN, 22, Color.BLACK));
        btnRiwayat = modulButton("Riwayat", FontIcon.of(MaterialDesignH.HISTORY, 22, Color.BLACK));
        btnLaporan = modulButton("Laporan", FontIcon.of(MaterialDesignC.CHART_BAR, 22, Color.BLACK));
        btnLogout = modulButton("Logout", FontIcon.of(MaterialDesignL.LOGOUT, 22, Color.BLACK));

        btnDashboard.addActionListener(e -> {
            setActive(btnDashboard);
            bukaModul("view.FormDashboard", "Dashboard");
        });
        btnBuku.addActionListener(e -> {
            setActive(btnBuku);
            bukaModul("view.FormBuku", "Buku");
        });
        btnKategori.addActionListener(e -> {
            setActive(btnKategori);
            bukaModul("view.FormKategori", "Kategori");
        });
        btnPenerbit.addActionListener(e -> {
            setActive(btnPenerbit);
            bukaModul("view.FormPenerbit", "Penerbit");
        });
        btnSupplier.addActionListener(e -> {
            setActive(btnSupplier);
            bukaModul("view.FormSupplier", "Supplier");
        });
        btnMember.addActionListener(e -> {
            setActive(btnMember);
            bukaModul("view.FormMember", "Member");
        });
        btnUser.addActionListener(e -> {
            setActive(btnUser);
            bukaModul("view.FormUser", "User");
        });
        btnPenjualan.addActionListener(e -> {
            setActive(btnPenjualan);
            bukaModul("view.FormPenjualan", "Penjualan");
        });
        btnPembelian.addActionListener(e -> {
            setActive(btnPembelian);
            bukaModul("view.FormPembelian", "Pembelian");
        });
        btnRetur.addActionListener(e -> {
            setActive(btnRetur);
            bukaModul("view.FormRetur", "Retur");
        });
        btnRiwayat.addActionListener(e -> {
            setActive(btnRiwayat);
            bukaModul("view.FormRiwayat", "Riwayat");
        });
        btnLaporan.addActionListener(e -> {
            setActive(btnLaporan);
            bukaModul("view.FormLaporan", "Laporan");
        });
        btnLogout.addActionListener(e -> {
            Sesi.clear();
            new Login().setVisible(true);
            dispose();
        });

        modulButtons.add(btnDashboard);
        modulButtons.add(btnBuku);
        modulButtons.add(btnKategori);
        modulButtons.add(btnPenerbit);
        modulButtons.add(btnSupplier);
        modulButtons.add(btnMember);
        modulButtons.add(btnUser);
        modulButtons.add(btnPenjualan);
        modulButtons.add(btnPembelian);
        modulButtons.add(btnRetur);
        modulButtons.add(btnRiwayat);
        modulButtons.add(btnLaporan);

        lblMaster = sectionLabel("MASTER");
        JLabel lblTransaksi = sectionLabel("TRANSAKSI");
        JLabel lblLaporan = sectionLabel("LAPORAN");

        sidebar.add(btnDashboard);
        sidebar.add(Box.createVerticalStrut(10));

        if (Sesi.isAdmin()) {
            sidebar.add(lblMaster);
            sidebar.add(Box.createVerticalStrut(6));
            sidebar.add(btnBuku);
            sidebar.add(Box.createVerticalStrut(8));
            sidebar.add(btnKategori);
            sidebar.add(Box.createVerticalStrut(8));
            sidebar.add(btnPenerbit);
            sidebar.add(Box.createVerticalStrut(8));
            sidebar.add(btnSupplier);
            sidebar.add(Box.createVerticalStrut(8));
            sidebar.add(btnMember);
            sidebar.add(Box.createVerticalStrut(8));
            sidebar.add(btnUser);
            sidebar.add(Box.createVerticalStrut(10));
        }

        sidebar.add(lblTransaksi);
        sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(btnPenjualan);
        sidebar.add(Box.createVerticalStrut(8));
        if (Sesi.isAdmin()) {
            sidebar.add(btnPembelian);
            sidebar.add(Box.createVerticalStrut(8));
        }
        sidebar.add(btnRetur);
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(btnRiwayat);
        sidebar.add(Box.createVerticalStrut(10));

        sidebar.add(lblLaporan);
        sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(btnLaporan);

        content = new JPanel(new BorderLayout());
        content.setBackground(NeoBrutalTheme.BG);

        // Header Brand di atas Sidebar
        JPanel pnlBrand = new JPanel(new BorderLayout(8, 0));
        pnlBrand.setBackground(NeoBrutalTheme.SURFACE);
        pnlBrand.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, Color.BLACK),
                BorderFactory.createEmptyBorder(14, 12, 14, 12)));
        JLabel iconBrand = new JLabel(FontIcon.of(MaterialDesignB.BOOK_OPEN_PAGE_VARIANT, 24, Color.BLACK));
        JLabel lblBrand = new JLabel(AppConfig.APP_NAME);
        lblBrand.setFont(new Font("Segoe UI Black", Font.BOLD, 13));
        lblBrand.setForeground(Color.BLACK);
        pnlBrand.add(iconBrand, BorderLayout.WEST);
        pnlBrand.add(lblBrand, BorderLayout.CENTER);

        // Sidebar scrollable di dalam viewport
        JPanel sidebarContent = new JPanel(new BorderLayout());
        sidebarContent.setBackground(NeoBrutalTheme.SURFACE);
        sidebarContent.add(sidebar, BorderLayout.NORTH);

        JScrollPane scrollSidebar = new JScrollPane(sidebarContent);
        scrollSidebar.setBorder(BorderFactory.createEmptyBorder());
        scrollSidebar.setBackground(NeoBrutalTheme.SURFACE);
        scrollSidebar.getViewport().setBackground(NeoBrutalTheme.SURFACE);
        scrollSidebar.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollSidebar.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollSidebar.getVerticalScrollBar().setUnitIncrement(16);

        // Tombol Logout dipin di bagian bawah sidebar agar selalu terlihat
        JPanel pnlLogout = new JPanel(new BorderLayout());
        pnlLogout.setBackground(NeoBrutalTheme.SURFACE);
        pnlLogout.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(2, 0, 0, 0, Color.BLACK),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        pnlLogout.add(btnLogout, BorderLayout.CENTER);

        JPanel sideWrap = new JPanel(new BorderLayout());
        sideWrap.setBackground(NeoBrutalTheme.SURFACE);
        sideWrap.setPreferredSize(new Dimension(230, 0));
        sideWrap.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 2, Color.BLACK));
        sideWrap.add(pnlBrand, BorderLayout.NORTH);
        sideWrap.add(scrollSidebar, BorderLayout.CENTER);
        sideWrap.add(pnlLogout, BorderLayout.SOUTH);

        // Top Bar di atas area konten
        JPanel topBar = buildTopBar();

        JPanel mainArea = new JPanel(new BorderLayout());
        mainArea.add(topBar, BorderLayout.NORTH);
        mainArea.add(content, BorderLayout.CENTER);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(sideWrap, BorderLayout.WEST);
        getContentPane().add(mainArea, BorderLayout.CENTER);

        refreshProfil();
        setActive(btnDashboard);
        bukaModul("view.FormDashboard", "Dashboard");
    }

    private JLabel sectionLabel(String teks) {
        JLabel l = new JLabel(teks);
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setForeground(new Color(0x64, 0x74, 0x8B));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        l.setBorder(BorderFactory.createEmptyBorder(6, 2, 2, 2));
        return l;
    }

    private void setActive(JButton b) {
        currentActive = b;
        for (JButton btn : modulButtons) {
            btn.setBackground(NeoBrutalTheme.SURFACE);
        }
        b.setBackground(ACTIVE_BG);
    }

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout(16, 0));
        bar.setBackground(NeoBrutalTheme.SURFACE);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, Color.BLACK),
                BorderFactory.createEmptyBorder(10, 18, 10, 18)));

        // Sisi Kiri: Ucapan Selamat Datang + Nama User + Badge Role
        JPanel pnlUser = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pnlUser.setOpaque(false);

        JLabel lblSapaan = new JLabel("Halo, Selamat Datang,");
        lblSapaan.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSapaan.setForeground(new Color(0x33, 0x33, 0x33));

        lblNama = new JLabel("-");
        lblNama.setName("profil_nama");
        lblNama.setFont(new Font("Segoe UI Black", Font.BOLD, 14));
        lblNama.setForeground(Color.BLACK);

        lblRole = new JLabel("-");
        lblRole.setName("profil_role");
        lblRole.setFont(new Font("Segoe UI Semibold", Font.BOLD, 11));
        lblRole.setOpaque(true);
        lblRole.setBackground(new Color(0xEE, 0xF2, 0xF6));
        lblRole.setForeground(Color.BLACK);
        lblRole.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.BLACK, 1),
                BorderFactory.createEmptyBorder(2, 8, 2, 8)));

        pnlUser.add(lblSapaan);
        pnlUser.add(lblNama);
        pnlUser.add(lblRole);

        // Sisi Kanan: Live Date & Clock Card + Profil Saya Button
        JPanel pnlKanan = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        pnlKanan.setOpaque(false);

        JPanel pnlWaktu = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        pnlWaktu.setBackground(NeoBrutalTheme.BG);
        pnlWaktu.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.BLACK, 1),
                BorderFactory.createEmptyBorder(2, 8, 2, 8)));

        JLabel iconTgl = new JLabel(FontIcon.of(MaterialDesignC.CALENDAR_MONTH, 16, Color.BLACK));
        lblTanggal = new JLabel("-");
        lblTanggal.setName("profil_tanggal");
        lblTanggal.setFont(new Font("Segoe UI Semibold", Font.BOLD, 13));
        lblTanggal.setForeground(Color.BLACK);

        JLabel sepWaktu = new JLabel("•");
        sepWaktu.setFont(new Font("Segoe UI", Font.BOLD, 13));
        sepWaktu.setForeground(Color.GRAY);

        JLabel iconJam = new JLabel(FontIcon.of(MaterialDesignC.CLOCK_OUTLINE, 16, Color.BLACK));
        lblJam = new JLabel("--:--:--");
        lblJam.setName("profil_jam");
        lblJam.setFont(new Font("Segoe UI Black", Font.BOLD, 13));
        lblJam.setForeground(Color.BLACK);

        pnlWaktu.add(iconTgl);
        pnlWaktu.add(lblTanggal);
        pnlWaktu.add(sepWaktu);
        pnlWaktu.add(iconJam);
        pnlWaktu.add(lblJam);

        JButton btnProfil = new JButton("Profil Saya", FontIcon.of(MaterialDesignA.ACCOUNT_CIRCLE, 18, Color.BLACK));
        btnProfil.setFont(new Font("Segoe UI Semibold", Font.BOLD, 12));
        btnProfil.setBackground(NeoBrutalTheme.SURFACE);
        btnProfil.setForeground(Color.BLACK);
        btnProfil.setBorder(new NeoShadowBorder());
        btnProfil.setFocusPainted(false);
        btnProfil.addActionListener(e -> new ProfilSaya(MenuUtama.this, MenuUtama.this::refreshProfil).setVisible(true));

        pnlKanan.add(pnlWaktu);
        pnlKanan.add(btnProfil);

        bar.add(pnlUser, BorderLayout.WEST);
        bar.add(pnlKanan, BorderLayout.EAST);

        lblJam.setText(FMT_JAM.format(LocalTime.now()));
        lblTanggal.setText(FMT_TGL.format(LocalDate.now()));
        new Timer(1000, e -> lblJam.setText(FMT_JAM.format(LocalTime.now()))).start();

        return bar;
    }

    public void refreshProfil() {
        String nama = Sesi.userLogin != null ? Sesi.userLogin.getNamaLengkap() : "-";
        String role = Sesi.role != null ? Sesi.role : "-";
        if (lblNama != null) {
            lblNama.setText(nama);
        }
        if (lblRole != null) {
            lblRole.setText(role.toUpperCase());
            if ("ADMIN".equalsIgnoreCase(role) || "ADMINISTRATOR".equalsIgnoreCase(role)) {
                lblRole.setBackground(new Color(0xFF, 0xE4, 0xE6));
                lblRole.setForeground(new Color(0xBE, 0x12, 0x3C));
            } else {
                lblRole.setBackground(new Color(0xDC, 0xFC, 0xE7));
                lblRole.setForeground(new Color(0x15, 0x80, 0x3D));
            }
        }
    }

    private JButton modulButton(String teks, FontIcon ikon) {
        JButton b = new JButton(teks, ikon);
        b.setIconTextGap(8);
        b.setFont(new Font("Segoe UI Semibold", Font.BOLD, 13));
        b.setBackground(NeoBrutalTheme.SURFACE);
        b.setForeground(Color.BLACK);
        b.setBorder(new NeoShadowBorder());
        b.setFocusPainted(false);
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        b.setPreferredSize(new Dimension(200, 38));
        return b;
    }

    private void setContent(JComponent comp) {
        content.removeAll();
        content.add(comp, BorderLayout.CENTER);
        content.revalidate();
        content.repaint();
    }

    private void bukaModul(String fqn, String nama) {
        try {
            Class<?> cls = Class.forName(fqn);
            Object obj = cls.getDeclaredConstructor().newInstance();
            if (obj instanceof JComponent) {
                setContent((JComponent) obj);
            } else {
                JLabel info = new JLabel("Modul " + nama + " belum tersedia", SwingConstants.CENTER);
                info.setFont(new Font("Segoe UI Semibold", Font.BOLD, 16));
                setContent(info);
            }
        } catch (ClassNotFoundException ex) {
            JLabel info = new JLabel("Modul " + nama + " belum tersedia", SwingConstants.CENTER);
            info.setFont(new Font("Segoe UI Semibold", Font.BOLD, 16));
            setContent(info);
        } catch (Exception ex) {
            JLabel info = new JLabel("Modul " + nama + " gagal dibuka", SwingConstants.CENTER);
            info.setFont(new Font("Segoe UI Semibold", Font.BOLD, 16));
            setContent(info);
        }
    }
}
