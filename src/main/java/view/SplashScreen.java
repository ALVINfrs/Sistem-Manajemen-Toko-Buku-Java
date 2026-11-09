package view;

import dao.KategoriDAOImpl;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Objects;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import koneksi.Koneksi;
import org.kordamp.ikonli.materialdesign2.MaterialDesignS;
import org.kordamp.ikonli.swing.FontIcon;
import util.LogoUtil;
import util.Motion;
import util.NeoBrutalTheme;
import util.NeoShadowBorder;

/**
 * Splash startup: panel brand maroon + form cream, ticker, progress blok.
 * Exit memakai curtain wipe ke atas setelah Login siap di belakang.
 */
public class SplashScreen extends JWindow {

    private final Runnable onDone;
    private JLabel lblTahap;
    private JLabel lblStatus;
    private JButton btnTutup;
    private SplashBrandPanel brand;
    private TickerPanel ticker;
    private SegmentProgress segments;
    private boolean selesai;

    public SplashScreen(Runnable onDone) {
        this.onDone = Objects.requireNonNull(onDone, "onDone wajib ada");
        setAlwaysOnTop(true);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(NeoBrutalTheme.BG);
        root.setBorder(BorderFactory.createLineBorder(NeoBrutalTheme.INK, 3));

        JPanel brandContent = new JPanel();
        brandContent.setOpaque(false);
        brandContent.setLayout(new BoxLayout(brandContent, BoxLayout.Y_AXIS));

        JPanel logoCard = new JPanel(new BorderLayout());
        logoCard.setBackground(NeoBrutalTheme.SURFACE);
        logoCard.setPreferredSize(new Dimension(240, 240));
        logoCard.setMinimumSize(new Dimension(240, 240));
        logoCard.setMaximumSize(new Dimension(240, 240));
        logoCard.setBorder(BorderFactory.createCompoundBorder(
                new NeoShadowBorder(8, NeoBrutalTheme.INK, NeoBrutalTheme.INK),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)));
        JLabel logo = new JLabel(LogoUtil.icon(160), SwingConstants.CENTER);
        logo.setHorizontalAlignment(SwingConstants.CENTER);
        logoCard.add(logo, BorderLayout.CENTER);
        logoCard.setAlignmentX(Component.CENTER_ALIGNMENT);
        brandContent.add(logoCard);
        brandContent.add(Box.createVerticalStrut(20));

        StickerLabel sticker = new StickerLabel("SISTEM INFORMASI",
                NeoBrutalTheme.BRAND_TEAL, NeoBrutalTheme.INK, -3);
        sticker.setAlignmentX(Component.CENTER_ALIGNMENT);
        brandContent.add(sticker);

        brand = new SplashBrandPanel(brandContent);
        brand.setPreferredSize(new Dimension(340, 0));
        root.add(brand, BorderLayout.WEST);

        JPanel right = new JPanel(new GridBagLayout());
        right.setBackground(NeoBrutalTheme.BG);
        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JLabel kicker = new JLabel("SISTEM INFORMASI MANAJEMEN TOKO");
        kicker.setFont(NeoBrutalTheme.mono(11));
        kicker.setForeground(NeoBrutalTheme.BRAND_MAROON);
        kicker.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(kicker);
        content.add(Box.createVerticalStrut(8));

        JLabel word1 = new JLabel("ALMIRA JAYA");
        word1.setFont(NeoBrutalTheme.display(36));
        word1.setForeground(NeoBrutalTheme.INK);
        word1.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(word1);

        JLabel word2 = new JLabel("ABADI");
        word2.setFont(NeoBrutalTheme.display(36));
        word2.setForeground(NeoBrutalTheme.INK);
        word2.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(word2);
        content.add(Box.createVerticalStrut(24));

        lblTahap = new JLabel("Menyiapkan tampilan...");
        lblTahap.setName("lblTahap");
        lblTahap.setFont(NeoBrutalTheme.mono(12));
        lblTahap.setForeground(new Color(70, 70, 70));
        lblTahap.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(lblTahap);
        content.add(Box.createVerticalStrut(10));

        segments = new SegmentProgress();
        segments.setAlignmentX(Component.LEFT_ALIGNMENT);
        segments.setMaximumSize(new Dimension(280, 18));
        content.add(segments);
        content.add(Box.createVerticalStrut(8));

        lblStatus = new JLabel(" ");
        lblStatus.setName("lblStatus");
        lblStatus.setFont(NeoBrutalTheme.mono(10));
        lblStatus.setForeground(new Color(90, 90, 90));
        lblStatus.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(lblStatus);
        content.add(Box.createVerticalStrut(18));

        btnTutup = new NeoButton("TUTUP", null, NeoBrutalTheme.PRIMARY);
        btnTutup.setName("btnTutup");
        btnTutup.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnTutup.setMaximumSize(new Dimension(140, 44));
        btnTutup.setVisible(false);
        btnTutup.addActionListener(e -> System.exit(1));
        content.add(btnTutup);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 48, 0, 36);
        right.add(content, gbc);
        root.add(right, BorderLayout.CENTER);

        ticker = new TickerPanel(
                "  TOKO BUKU  •  ALAT TULIS  •  SISTEM INFORMASI MANAJEMEN  •  ALMIRA JAYA ABADI  •  ",
                NeoBrutalTheme.BRAND_TEAL, NeoBrutalTheme.INK);
        root.add(ticker, BorderLayout.SOUTH);

        setContentPane(root);
        setSize(new Dimension(900, 560));
        setLocationRelativeTo(null);
    }

    /** Menampilkan splash dan memulai inisialisasi. */
    public void start() {
        setVisible(true);
        brand.start();
        ticker.start();
        jalankanTahap();
    }

    private void tahap(final int nilai, final String teks) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            segments.setValue(nilai);
            lblTahap.setText(teks);
        });
        Thread.sleep(300);
    }

    private void jalankanTahap() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                tahap(1, "Menyiapkan tampilan...");
                tahap(2, "Menghubungkan database...");
                try (Connection c = Koneksi.getConnection();
                     PreparedStatement ps = c.prepareStatement("SELECT 1");
                     ResultSet rs = ps.executeQuery()) {
                    rs.next();
                }
                tahap(3, "Memuat data master...");
                int nKat = new KategoriDAOImpl().getAll().size();
                lblStatusSet("Kategori: " + nKat);
                tahap(4, "Menyiapkan ikon...");
                FontIcon.of(MaterialDesignS.STORE, 16, Color.BLACK);
                tahap(5, "Siap.");
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    if (selesai) {
                        return;
                    }
                    selesai = true;
                    onDone.run();
                    brand.stop();
                    ticker.stop();
                    exitCurtain();
                } catch (Exception ex) {
                    lblStatus.setText("Database tidak terhubung. Hidupkan MySQL lalu ulangi.");
                    lblStatus.setForeground(NeoBrutalTheme.DANGER);
                    btnTutup.setVisible(true);
                    brand.stop();
                    ticker.stop();
                }
            }
        };
        worker.execute();
    }

    private void exitCurtain() {
        final int startY = getY();
        Motion.animate(520, Motion.EASE_IN_OUT_CUBIC,
                p -> setLocation(getX(), startY - (int) Math.round(p * (getHeight() + 40))),
                this::dispose);
    }

    private void lblStatusSet(final String teks) {
        SwingUtilities.invokeLater(() -> lblStatus.setText(teks));
    }
}
