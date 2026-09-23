package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import org.kordamp.ikonli.materialdesign2.MaterialDesignA;
import org.kordamp.ikonli.materialdesign2.MaterialDesignB;
import org.kordamp.ikonli.materialdesign2.MaterialDesignC;
import org.kordamp.ikonli.materialdesign2.MaterialDesignD;
import org.kordamp.ikonli.materialdesign2.MaterialDesignK;
import org.kordamp.ikonli.materialdesign2.MaterialDesignL;
import org.kordamp.ikonli.materialdesign2.MaterialDesignT;
import org.kordamp.ikonli.swing.FontIcon;
import util.AppConfig;
import util.NeoBrutalTheme;
import util.NeoShadowBorder;
import util.Sesi;

public class MenuUtama extends JFrame {

    private final JPanel content;
    private JButton btnBuku;
    private JButton btnKategori;
    private JButton btnPenerbit;
    private JButton btnSupplier;
    private JButton btnMember;
    private JButton btnPenjualan;
    private JButton btnPembelian;
    private JButton btnRetur;
    private JButton btnLaporan;
    private JButton btnLogout;

    public MenuUtama() {
        super(AppConfig.APP_NAME + " - Menu Utama");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 650);
        setLocationRelativeTo(null);

        JPanel sidebar = new JPanel(new GridLayout(0, 1, 0, 10));
        sidebar.setBackground(NeoBrutalTheme.SURFACE);
        sidebar.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        btnBuku = modulButton("Buku", FontIcon.of(MaterialDesignB.BOOK_OPEN_VARIANT, 22, Color.BLACK));
        btnKategori = modulButton("Kategori", FontIcon.of(MaterialDesignT.TAG, 22, Color.BLACK));
        btnPenerbit = modulButton("Penerbit", FontIcon.of(MaterialDesignD.DOMAIN, 22, Color.BLACK));
        btnSupplier = modulButton("Supplier", FontIcon.of(MaterialDesignT.TRUCK, 22, Color.BLACK));
        btnMember = modulButton("Member", FontIcon.of(MaterialDesignA.ACCOUNT_GROUP, 22, Color.BLACK));
        btnPenjualan = modulButton("Penjualan", FontIcon.of(MaterialDesignC.CASH_REGISTER, 22, Color.BLACK));
        btnPembelian = modulButton("Pembelian", FontIcon.of(MaterialDesignC.CART_ARROW_DOWN, 22, Color.BLACK));
        btnRetur = modulButton("Retur", FontIcon.of(MaterialDesignK.KEYBOARD_RETURN, 22, Color.BLACK));
        btnLaporan = modulButton("Laporan", FontIcon.of(MaterialDesignC.CHART_BAR, 22, Color.BLACK));
        btnLogout = modulButton("Logout", FontIcon.of(MaterialDesignL.LOGOUT, 22, Color.BLACK));

        btnBuku.addActionListener(e -> bukaModul("view.FormBuku", "Buku"));
        btnKategori.addActionListener(e -> bukaModul("view.FormKategori", "Kategori"));
        btnPenerbit.addActionListener(e -> bukaModul("view.FormPenerbit", "Penerbit"));
        btnSupplier.addActionListener(e -> bukaModul("view.FormSupplier", "Supplier"));
        btnMember.addActionListener(e -> bukaModul("view.FormMember", "Member"));
        btnPenjualan.addActionListener(e -> bukaModul("view.FormPenjualan", "Penjualan"));
        btnPembelian.addActionListener(e -> bukaModul("view.FormPembelian", "Pembelian"));
        btnRetur.addActionListener(e -> bukaModul("view.FormRetur", "Retur"));
        btnLaporan.addActionListener(e -> bukaModul("view.FormLaporan", "Laporan"));
        btnLogout.addActionListener(e -> {
            Sesi.clear();
            new Login().setVisible(true);
            dispose();
        });

        sidebar.add(btnBuku);
        sidebar.add(btnKategori);
        sidebar.add(btnPenerbit);
        sidebar.add(btnSupplier);
        sidebar.add(btnMember);
        sidebar.add(btnPenjualan);
        sidebar.add(btnPembelian);
        sidebar.add(btnRetur);
        sidebar.add(btnLaporan);
        sidebar.add(btnLogout);

        if (!Sesi.isAdmin()) {
            btnBuku.setVisible(false);
            btnKategori.setVisible(false);
            btnPenerbit.setVisible(false);
            btnSupplier.setVisible(false);
            btnMember.setVisible(false);
            btnPembelian.setVisible(false);
        }

        content = new JPanel(new BorderLayout());
        content.setBackground(NeoBrutalTheme.BG);
        String nama = Sesi.userLogin != null ? Sesi.userLogin.getNamaLengkap() : "-";
        String role = Sesi.role != null ? Sesi.role : "-";
        JLabel lblWelcome = new JLabel("Selamat datang, " + nama + " (" + role + ")", SwingConstants.CENTER);
        lblWelcome.setFont(new Font("Segoe UI Black", Font.BOLD, 20));
        content.add(lblWelcome, BorderLayout.CENTER);

        JPanel sideWrap = new JPanel(new BorderLayout());
        sideWrap.setBackground(NeoBrutalTheme.SURFACE);
        sideWrap.setPreferredSize(new Dimension(220, 0));
        sideWrap.add(sidebar, BorderLayout.NORTH);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(sideWrap, BorderLayout.WEST);
        getContentPane().add(content, BorderLayout.CENTER);
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
