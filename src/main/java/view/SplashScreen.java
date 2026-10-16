package view;

import dao.KategoriDAO;
import dao.KategoriDAOImpl;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Objects;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JWindow;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import koneksi.Koneksi;
import org.kordamp.ikonli.materialdesign2.MaterialDesignS;
import org.kordamp.ikonli.swing.FontIcon;
import util.AppConfig;
import util.LogoUtil;
import util.NeoBrutalTheme;
import util.NeoShadowBorder;

/**
 * Splash startup: JWindow neobrutalism berisi logo, progress tahap, dan status.
 * Menjalankan inisialisasi di worker: tampilan, koneksi DB, data master, ikon.
 * Bila database gagal: status merah + tombol Tutup (System.exit(1)).
 */
public class SplashScreen extends JWindow {

    private final Runnable onDone;
    private JProgressBar progress;
    private JLabel lblTahap;
    private JLabel lblStatus;
    private JButton btnTutup;

    public SplashScreen(Runnable onDone) {
        this.onDone = Objects.requireNonNull(onDone, "onDone wajib ada");

        JPanel kartu = new JPanel(new BorderLayout(0, 10));
        kartu.setBackground(NeoBrutalTheme.SURFACE);
        kartu.setBorder(BorderFactory.createCompoundBorder(
                new NeoShadowBorder(),
                BorderFactory.createEmptyBorder(24, 32, 24, 32)));

        JLabel logo = new JLabel(LogoUtil.icon(120));
        logo.setHorizontalAlignment(SwingConstants.CENTER);
        kartu.add(logo, BorderLayout.NORTH);

        JPanel tengah = new JPanel(new BorderLayout(0, 6));
        tengah.setBackground(NeoBrutalTheme.SURFACE);
        JLabel nama = new JLabel(AppConfig.APP_NAME, SwingConstants.CENTER);
        nama.setFont(new Font("Segoe UI Black", Font.BOLD, 20));
        nama.setForeground(Color.BLACK);
        tengah.add(nama, BorderLayout.NORTH);

        lblTahap = new JLabel("Menyiapkan…", SwingConstants.CENTER);
        lblTahap.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblTahap.setForeground(new Color(90, 90, 90));
        tengah.add(lblTahap, BorderLayout.CENTER);

        progress = new JProgressBar(0, 5);
        progress.setValue(0);
        progress.setStringPainted(true);
        progress.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        tengah.add(progress, BorderLayout.SOUTH);
        kartu.add(tengah, BorderLayout.CENTER);

        JPanel bawah = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        bawah.setBackground(NeoBrutalTheme.SURFACE);
        lblStatus = new JLabel(" ", SwingConstants.CENTER);
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblStatus.setForeground(new Color(90, 90, 90));
        bawah.add(lblStatus);

        btnTutup = new JButton("Tutup");
        btnTutup.setFont(new Font("Segoe UI Semibold", Font.BOLD, 13));
        btnTutup.setBackground(NeoBrutalTheme.PRIMARY);
        btnTutup.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        btnTutup.setVisible(false);
        btnTutup.addActionListener(e -> System.exit(1));
        bawah.add(btnTutup);
        kartu.add(bawah, BorderLayout.SOUTH);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(NeoBrutalTheme.BG);
        root.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        root.add(kartu, BorderLayout.CENTER);
        setContentPane(root);
        setSize(new Dimension(460, 420));
        setLocationRelativeTo(null);

        setVisible(true);
        jalankanTahap();
    }

    private void tahap(final int nilai, final String teks) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            progress.setValue(nilai);
            lblTahap.setText(teks);
        });
        Thread.sleep(300);
    }

    private void jalankanTahap() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                tahap(1, "Menyiapkan tampilan…");
                tahap(2, "Menghubungkan database…");
                try (Connection c = Koneksi.getConnection();
                     PreparedStatement ps = c.prepareStatement("SELECT 1");
                     ResultSet rs = ps.executeQuery()) {
                    rs.next();
                }
                tahap(3, "Memuat data master…");
                int nKat = new KategoriDAOImpl().getAll().size();
                lblStatusSet("Kategori: " + nKat);
                tahap(4, "Menyiapkan ikon…");
                FontIcon.of(MaterialDesignS.STORE, 16, Color.BLACK);
                tahap(5, "Siap.");
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    dispose();
                    onDone.run();
                } catch (Exception ex) {
                    lblStatus.setText("Database tidak terhubung. Hidupkan MySQL lalu ulangi.");
                    lblStatus.setForeground(new Color(200, 30, 30));
                    btnTutup.setVisible(true);
                }
            }
        };
        worker.execute();
    }

    private void lblStatusSet(final String teks) {
        SwingUtilities.invokeLater(() -> lblStatus.setText(teks));
    }
}
