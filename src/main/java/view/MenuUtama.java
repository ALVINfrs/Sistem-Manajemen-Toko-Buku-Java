package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
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
import org.kordamp.ikonli.materialdesign2.MaterialDesignS;
import org.kordamp.ikonli.materialdesign2.MaterialDesignT;
import org.kordamp.ikonli.materialdesign2.MaterialDesignV;
import org.kordamp.ikonli.swing.FontIcon;
import util.AppConfig;
import util.Motion;
import util.NeoBrutalTheme;
import util.Sesi;

public class MenuUtama extends JFrame {

    private static final int SIDEBAR_WIDTH = 240;
    private static final Color SECTION_FG = Color.decode("#E8D9C0");
    private static final DateTimeFormatter FMT_JAM = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter FMT_TGL =
            DateTimeFormatter.ofPattern("dd MMM yyyy", new Locale("id", "ID"));

    private final MotionPanel content;
    private Timer transitionTimer;
    private Timer clockTimer;
    private JButton currentActive;
    private final List<JButton> modulButtons = new ArrayList<>();
    private SidebarButton btnDashboard;
    private SidebarButton btnBuku;
    private SidebarButton btnKategori;
    private SidebarButton btnPenerbit;
    private SidebarButton btnSupplier;
    private SidebarButton btnAtk;
    private SidebarButton btnUser;
    private SidebarButton btnPenjualan;
    private SidebarButton btnPembelian;
    private SidebarButton btnRetur;
    private SidebarButton btnRiwayat;
    private SidebarButton btnLaporan;
    private NeoButton btnLogout;
    private NeoButton btnProfil;
    private JLabel lblNama;
    private JLabel lblRole;
    private JLabel lblJam;
    private JLabel lblTanggal;

    public MenuUtama() {
        super(AppConfig.APP_NAME + " - Menu Utama");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1050, 700);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
        getContentPane().setBackground(NeoBrutalTheme.BG);

        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(NeoBrutalTheme.BRAND_MAROON);
        sidebar.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        btnDashboard = modulButton("Dashboard",
                FontIcon.of(MaterialDesignV.VIEW_DASHBOARD, 18, Color.BLACK));
        btnBuku = modulButton("Buku",
                FontIcon.of(MaterialDesignB.BOOK_OPEN_VARIANT, 18, Color.BLACK));
        btnKategori = modulButton("Kategori",
                FontIcon.of(MaterialDesignT.TAG, 18, Color.BLACK));
        btnPenerbit = modulButton("Penerbit",
                FontIcon.of(MaterialDesignD.DOMAIN, 18, Color.BLACK));
        btnSupplier = modulButton("Supplier",
                FontIcon.of(MaterialDesignT.TRUCK, 18, Color.BLACK));
        btnAtk = modulButton("ATK",
                FontIcon.of(MaterialDesignS.STORE, 18, Color.BLACK));
        btnUser = modulButton("User",
                FontIcon.of(MaterialDesignA.ACCOUNT_COG, 18, Color.BLACK));
        btnPenjualan = modulButton("Penjualan",
                FontIcon.of(MaterialDesignC.CASH_REGISTER, 18, Color.BLACK));
        btnPembelian = modulButton("Pembelian",
                FontIcon.of(MaterialDesignC.CART_ARROW_DOWN, 18, Color.BLACK));
        btnRetur = modulButton("Retur",
                FontIcon.of(MaterialDesignK.KEYBOARD_RETURN, 18, Color.BLACK));
        btnRiwayat = modulButton("Riwayat",
                FontIcon.of(MaterialDesignH.HISTORY, 18, Color.BLACK));
        btnLaporan = modulButton("Laporan",
                FontIcon.of(MaterialDesignC.CHART_BAR, 18, Color.BLACK));

        btnLogout = new NeoButton("Logout",
                FontIcon.of(MaterialDesignL.LOGOUT, 18, Color.BLACK),
                NeoBrutalTheme.PRIMARY);
        btnLogout.setName("btnLogout");
        btnLogout.setFont(NeoBrutalTheme.display(12));

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
        btnAtk.addActionListener(e -> {
            setActive(btnAtk);
            bukaModul("view.FormAtk", "ATK");
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
        modulButtons.add(btnAtk);
        modulButtons.add(btnUser);
        modulButtons.add(btnPenjualan);
        modulButtons.add(btnPembelian);
        modulButtons.add(btnRetur);
        modulButtons.add(btnRiwayat);
        modulButtons.add(btnLaporan);

        JLabel lblMaster = sectionLabel("MASTER");
        JLabel lblTransaksi = sectionLabel("TRANSAKSI");
        JLabel lblLaporan = sectionLabel("LAPORAN");

        sidebar.add(btnDashboard);
        sidebar.add(Box.createVerticalStrut(6));

        if (Sesi.isAdmin()) {
            sidebar.add(lblMaster);
            sidebar.add(Box.createVerticalStrut(2));
            sidebar.add(btnBuku);
            sidebar.add(Box.createVerticalStrut(2));
            sidebar.add(btnKategori);
            sidebar.add(Box.createVerticalStrut(2));
            sidebar.add(btnPenerbit);
            sidebar.add(Box.createVerticalStrut(2));
            sidebar.add(btnSupplier);
            sidebar.add(Box.createVerticalStrut(2));
            sidebar.add(btnAtk);
            sidebar.add(Box.createVerticalStrut(2));
            sidebar.add(btnUser);
            sidebar.add(Box.createVerticalStrut(6));
        }

        sidebar.add(lblTransaksi);
        sidebar.add(Box.createVerticalStrut(2));
        sidebar.add(btnPenjualan);
        sidebar.add(Box.createVerticalStrut(2));
        if (Sesi.isAdmin()) {
            sidebar.add(btnPembelian);
            sidebar.add(Box.createVerticalStrut(2));
        }
        sidebar.add(btnRetur);
        sidebar.add(Box.createVerticalStrut(2));
        sidebar.add(btnRiwayat);
        sidebar.add(Box.createVerticalStrut(6));

        sidebar.add(lblLaporan);
        sidebar.add(Box.createVerticalStrut(2));
        sidebar.add(btnLaporan);

        JPanel pnlBrand = buildBrand();

        JPanel sidebarContent = new JPanel(new BorderLayout());
        sidebarContent.setBackground(NeoBrutalTheme.BRAND_MAROON);
        sidebarContent.add(sidebar, BorderLayout.NORTH);

        JScrollPane scrollSidebar = new JScrollPane(sidebarContent);
        scrollSidebar.setBorder(BorderFactory.createEmptyBorder());
        scrollSidebar.setBackground(NeoBrutalTheme.BRAND_MAROON);
        scrollSidebar.getViewport().setBackground(NeoBrutalTheme.BRAND_MAROON);
        scrollSidebar.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollSidebar.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollSidebar.getVerticalScrollBar().setUnitIncrement(16);

        JPanel pnlLogout = new JPanel(new BorderLayout());
        pnlLogout.setBackground(NeoBrutalTheme.BRAND_MAROON);
        pnlLogout.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(2, 0, 0, 0, NeoBrutalTheme.BRAND_MAROON_DARK),
                BorderFactory.createEmptyBorder(10, 12, 12, 12)));
        pnlLogout.add(btnLogout, BorderLayout.CENTER);

        JPanel sideWrap = new JPanel(new BorderLayout());
        sideWrap.setBackground(NeoBrutalTheme.BRAND_MAROON);
        sideWrap.setPreferredSize(new Dimension(SIDEBAR_WIDTH, 0));
        sideWrap.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 2, NeoBrutalTheme.INK));
        sideWrap.add(pnlBrand, BorderLayout.NORTH);
        sideWrap.add(scrollSidebar, BorderLayout.CENTER);
        sideWrap.add(pnlLogout, BorderLayout.SOUTH);

        content = new MotionPanel(new BorderLayout());
        content.setBackground(NeoBrutalTheme.BG);

        JPanel mainArea = new JPanel(new BorderLayout());
        mainArea.setBackground(NeoBrutalTheme.BG);
        mainArea.add(buildTopBar(), BorderLayout.NORTH);
        mainArea.add(content, BorderLayout.CENTER);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(sideWrap, BorderLayout.WEST);
        getContentPane().add(mainArea, BorderLayout.CENTER);

        refreshProfil();
        setActive(btnDashboard);
        bukaModul("view.FormDashboard", "Dashboard");
    }

    private JPanel buildBrand() {
        JPanel logoCard = new JPanel(new BorderLayout());
        logoCard.setBackground(NeoBrutalTheme.BG);
        logoCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(NeoBrutalTheme.INK, 2),
                BorderFactory.createEmptyBorder(7, 7, 7, 7)));
        logoCard.setPreferredSize(new Dimension(44, 44));
        logoCard.add(new JLabel(util.LogoUtil.icon(28)), BorderLayout.CENTER);

        JPanel wordmark = new JPanel();
        wordmark.setOpaque(false);
        wordmark.setLayout(new BoxLayout(wordmark, BoxLayout.Y_AXIS));
        JLabel line1 = new JLabel("CV. Almira Jaya");
        line1.setFont(NeoBrutalTheme.display(13));
        line1.setForeground(NeoBrutalTheme.BG);
        line1.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel line2 = new JLabel("Abadi");
        line2.setFont(NeoBrutalTheme.display(13));
        line2.setForeground(NeoBrutalTheme.BG);
        line2.setAlignmentX(Component.LEFT_ALIGNMENT);
        wordmark.add(line1);
        wordmark.add(line2);

        JPanel brand = new JPanel(new BorderLayout(10, 0));
        brand.setBackground(NeoBrutalTheme.BRAND_MAROON);
        brand.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, NeoBrutalTheme.BRAND_MAROON_DARK),
                BorderFactory.createEmptyBorder(14, 12, 14, 12)));
        brand.add(logoCard, BorderLayout.WEST);
        brand.add(wordmark, BorderLayout.CENTER);
        return brand;
    }

    private JLabel sectionLabel(String teks) {
        JLabel label = new JLabel(teks);
        label.setFont(NeoBrutalTheme.mono(9));
        label.setForeground(SECTION_FG);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setBorder(BorderFactory.createEmptyBorder(4, 8, 2, 8));
        return label;
    }

    private void setActive(JButton b) {
        currentActive = b;
        for (JButton btn : modulButtons) {
            if (btn instanceof SidebarButton) {
                ((SidebarButton) btn).setActive(btn == b);
            }
        }
    }

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout(16, 0));
        bar.setBackground(NeoBrutalTheme.BG);
        bar.setPreferredSize(new Dimension(0, 64));
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, NeoBrutalTheme.INK),
                BorderFactory.createEmptyBorder(8, 18, 8, 18)));

        JPanel pnlUser = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pnlUser.setOpaque(false);

        JLabel lblSapaan = new JLabel("Halo, Selamat Datang,");
        lblSapaan.setFont(NeoBrutalTheme.body(java.awt.Font.PLAIN, 13));
        lblSapaan.setForeground(NeoBrutalTheme.TEXT_MUTED);

        lblNama = new JLabel("-");
        lblNama.setName("profil_nama");
        lblNama.setFont(NeoBrutalTheme.display(14));
        lblNama.setForeground(NeoBrutalTheme.INK);

        lblRole = new JLabel("-");
        lblRole.setName("profil_role");
        lblRole.setFont(NeoBrutalTheme.mono(9));
        lblRole.setOpaque(true);
        lblRole.setBackground(NeoBrutalTheme.SURFACE);
        lblRole.setForeground(NeoBrutalTheme.INK);
        lblRole.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(NeoBrutalTheme.INK, 2),
                BorderFactory.createEmptyBorder(3, 8, 3, 8)));

        pnlUser.add(lblSapaan);
        pnlUser.add(lblNama);
        pnlUser.add(lblRole);

        JPanel pnlKanan = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        pnlKanan.setOpaque(false);

        JPanel pnlWaktu = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        pnlWaktu.setBackground(NeoBrutalTheme.SURFACE);
        pnlWaktu.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(NeoBrutalTheme.INK, 2),
                BorderFactory.createEmptyBorder(2, 8, 2, 8)));

        JLabel iconTgl = new JLabel(FontIcon.of(
                MaterialDesignC.CALENDAR_MONTH, 14, NeoBrutalTheme.INK));
        lblTanggal = new JLabel("-");
        lblTanggal.setName("profil_tanggal");
        lblTanggal.setFont(NeoBrutalTheme.mono(11));
        lblTanggal.setForeground(NeoBrutalTheme.INK);

        JLabel sepWaktu = new JLabel("•");
        sepWaktu.setFont(NeoBrutalTheme.mono(11));
        sepWaktu.setForeground(NeoBrutalTheme.BRAND_TEAL);

        JLabel iconJam = new JLabel(FontIcon.of(
                MaterialDesignC.CLOCK_OUTLINE, 14, NeoBrutalTheme.INK));
        lblJam = new JLabel("--:--:--");
        lblJam.setName("profil_jam");
        lblJam.setFont(NeoBrutalTheme.mono(11));
        lblJam.setForeground(NeoBrutalTheme.INK);

        pnlWaktu.add(iconTgl);
        pnlWaktu.add(lblTanggal);
        pnlWaktu.add(sepWaktu);
        pnlWaktu.add(iconJam);
        pnlWaktu.add(lblJam);

        btnProfil = new NeoButton("Profil Saya",
                FontIcon.of(MaterialDesignA.ACCOUNT_CIRCLE, 16, Color.BLACK),
                NeoBrutalTheme.WARNING);
        btnProfil.setName("btnProfil");
        btnProfil.setFont(NeoBrutalTheme.display(12));
        btnProfil.addActionListener(e ->
                new ProfilSaya(MenuUtama.this, MenuUtama.this::refreshProfil).setVisible(true));

        pnlKanan.add(pnlWaktu);
        pnlKanan.add(btnProfil);

        bar.add(pnlUser, BorderLayout.WEST);
        bar.add(pnlKanan, BorderLayout.EAST);

        lblJam.setText(FMT_JAM.format(LocalTime.now()));
        lblTanggal.setText(FMT_TGL.format(LocalDate.now()));
        if (clockTimer != null) {
            clockTimer.stop();
        }
        clockTimer = new Timer(1000, e -> lblJam.setText(FMT_JAM.format(LocalTime.now())));
        clockTimer.start();

        return bar;
    }

    public void refreshProfil() {
        String nama = Sesi.userLogin != null ? Sesi.userLogin.getNamaLengkap() : "-";
        String role = Sesi.role != null ? Sesi.role : "-";
        if (lblNama != null) {
            lblNama.setText(nama);
        }
        if (lblRole != null) {
            lblRole.setText(role.toUpperCase(Locale.ROOT));
            if ("ADMIN".equalsIgnoreCase(role) || "ADMINISTRATOR".equalsIgnoreCase(role)) {
                lblRole.setBackground(NeoBrutalTheme.WARNING);
            } else if ("KASIR".equalsIgnoreCase(role)) {
                lblRole.setBackground(NeoBrutalTheme.BRAND_TEAL);
            } else {
                lblRole.setBackground(NeoBrutalTheme.SURFACE);
            }
            lblRole.setForeground(NeoBrutalTheme.INK);
        }
    }

    private SidebarButton modulButton(String teks, FontIcon ikon) {
        SidebarButton button = new SidebarButton(teks, ikon);
        String key = "ATK".equals(teks) ? "Atk" : teks.replace(" ", "");
        button.setName("btn" + key);
        return button;
    }

    private void setContent(JComponent comp) {
        if (transitionTimer != null) {
            transitionTimer.stop();
        }
        content.removeAll();
        content.add(comp, BorderLayout.CENTER);
        content.setAlpha(0f);
        content.setOffset(0, 8);
        content.revalidate();
        content.repaint();
        transitionTimer = Motion.animate(160, Motion.EASE_OUT_CUBIC, p -> {
            content.setAlpha((float) p);
            content.setOffset(0, (int) Math.round(8 * (1 - p)));
        }, () -> {
            content.setAlpha(1f);
            content.setOffset(0, 0);
        });
    }

    private void bukaModul(String fqn, String nama) {
        try {
            Class<?> cls = Class.forName(fqn);
            Object obj = cls.getDeclaredConstructor().newInstance();
            if (obj instanceof JComponent) {
                setContent((JComponent) obj);
            } else {
                setContent(fallback("Modul " + nama + " belum tersedia"));
            }
        } catch (ClassNotFoundException ex) {
            setContent(fallback("Modul " + nama + " belum tersedia"));
        } catch (Exception ex) {
            setContent(fallback("Modul " + nama + " gagal dibuka"));
        }
    }

    private JLabel fallback(String text) {
        JLabel info = new JLabel(text, SwingConstants.CENTER);
        info.setFont(NeoBrutalTheme.display(16));
        info.setForeground(NeoBrutalTheme.INK);
        return info;
    }
}
