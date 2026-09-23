package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
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
        setSize(1000, 650);
        setLocationRelativeTo(null);

        JPanel sidebar = new JPanel(new GridLayout(0, 1, 0, 10));
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
        sidebar.add(lblMaster);
        sidebar.add(btnBuku);
        sidebar.add(btnKategori);
        sidebar.add(btnPenerbit);
        sidebar.add(btnSupplier);
        sidebar.add(btnMember);
        sidebar.add(btnUser);
        sidebar.add(lblTransaksi);
        sidebar.add(btnPenjualan);
        sidebar.add(btnPembelian);
        sidebar.add(btnRetur);
        sidebar.add(btnRiwayat);
        sidebar.add(lblLaporan);
        sidebar.add(btnLaporan);
        sidebar.add(btnLogout);

        if (!Sesi.isAdmin()) {
            btnBuku.setVisible(false);
            btnKategori.setVisible(false);
            btnPenerbit.setVisible(false);
            btnSupplier.setVisible(false);
            btnMember.setVisible(false);
            btnUser.setVisible(false);
            btnPembelian.setVisible(false);
            lblMaster.setVisible(false);
        }

        content = new JPanel(new BorderLayout());
        content.setBackground(NeoBrutalTheme.BG);

        profilCard = buildProfilCard();

        JPanel sideWrap = new JPanel(new BorderLayout());
        sideWrap.setBackground(NeoBrutalTheme.SURFACE);
        sideWrap.setPreferredSize(new Dimension(220, 0));
        sideWrap.add(sidebar, BorderLayout.NORTH);
        sideWrap.add(profilCard, BorderLayout.SOUTH);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(sideWrap, BorderLayout.WEST);
        getContentPane().add(content, BorderLayout.CENTER);

        refreshProfil();
        setActive(btnDashboard);
        bukaModul("view.FormDashboard", "Dashboard");
    }

    private JLabel sectionLabel(String teks) {
        JLabel l = new JLabel(teks);
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setForeground(Color.BLACK);
        return l;
    }

    private void setActive(JButton b) {
        currentActive = b;
        for (JButton btn : modulButtons) {
            btn.setBackground(NeoBrutalTheme.SURFACE);
        }
        b.setBackground(ACTIVE_BG);
    }

    private JPanel buildProfilCard() {
        JPanel card = new JPanel(new BorderLayout(8, 0));
        card.setName("profil_card");
        card.setBackground(NeoBrutalTheme.SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new NeoShadowBorder(),
                BorderFactory.createEmptyBorder(6, 6, 6, 6)));

        JLabel avatar = new JLabel(FontIcon.of(MaterialDesignA.ACCOUNT_CIRCLE, 40, Color.BLACK));

        JPanel info = new JPanel(new GridLayout(0, 1, 0, 2));
        info.setBackground(NeoBrutalTheme.SURFACE);
        lblNama = new JLabel("-");
        lblNama.setName("profil_nama");
        lblNama.setFont(new Font("Segoe UI Semibold", Font.BOLD, 13));
        lblRole = new JLabel("-");
        lblRole.setName("profil_role");
        lblRole.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 12));
        lblJam = new JLabel("--:--:--");
        lblJam.setName("profil_jam");
        lblJam.setFont(new Font("Segoe UI Semibold", Font.BOLD, 13));
        lblTanggal = new JLabel("-");
        lblTanggal.setName("profil_tanggal");
        lblTanggal.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 12));
        info.add(lblNama);
        info.add(lblRole);
        info.add(lblJam);
        info.add(lblTanggal);

        card.add(avatar, BorderLayout.WEST);
        card.add(info, BorderLayout.CENTER);

        lblJam.setText(FMT_JAM.format(LocalTime.now()));
        lblTanggal.setText(FMT_TGL.format(LocalDate.now()));
        new Timer(1000, e -> lblJam.setText(FMT_JAM.format(LocalTime.now()))).start();

        attachProfilClick(card);
        return card;
    }

    private void attachProfilClick(Component c) {
        c.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                new ProfilSaya(MenuUtama.this, MenuUtama.this::refreshProfil).setVisible(true);
            }
        });
        if (c instanceof Container) {
            for (Component child : ((Container) c).getComponents()) {
                attachProfilClick(child);
            }
        }
    }

    public void refreshProfil() {
        String nama = Sesi.userLogin != null ? Sesi.userLogin.getNamaLengkap() : "-";
        String role = Sesi.role != null ? Sesi.role : "-";
        if (lblNama != null) {
            lblNama.setText(nama);
        }
        if (lblRole != null) {
            lblRole.setText(role);
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
