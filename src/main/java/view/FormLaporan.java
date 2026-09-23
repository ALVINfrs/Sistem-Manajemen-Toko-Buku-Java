package view;

import com.github.lgooddatepicker.components.DatePicker;
import dao.BukuDAO;
import dao.BukuDAOImpl;
import dao.PembelianDAO;
import dao.PembelianDAOImpl;
import dao.PenjualanDAO;
import dao.PenjualanDAOImpl;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;
import net.sf.jasperreports.engine.JRException;
import org.kordamp.ikonli.materialdesign2.MaterialDesignE;
import org.kordamp.ikonli.materialdesign2.MaterialDesignM;
import org.kordamp.ikonli.swing.FontIcon;
import report.ReportHelper;
import util.AppConfig;
import util.NeoBrutalTheme;
import util.NeoShadowBorder;
import util.Sesi;

public class FormLaporan extends JPanel {

    private static final String JENIS_BUKU = "Data Buku";
    private static final String JENIS_PENJUALAN = "Penjualan";
    private static final String JENIS_PEMBELIAN = "Pembelian";
    private static final String JENIS_STOK = "Stok Menipis";
    private static final String JENIS_PENDAPATAN = "Pendapatan";
    private static final String JENIS_TERLARIS = "Buku Terlaris";

    private static final Locale LOCALE_ID = new Locale("id", "ID");
    private static final DateTimeFormatter FMT_PERIODE = DateTimeFormatter.ofPattern("dd MMM yyyy", LOCALE_ID);

    private final BukuDAO bukuDAO = new BukuDAOImpl();
    private final PenjualanDAO penjualanDAO = new PenjualanDAOImpl();
    private final PembelianDAO pembelianDAO = new PembelianDAOImpl();

    private JComboBox<String> cmbJenis;
    private DatePicker dpDari;
    private DatePicker dpSampai;
    private JPanel pnlTanggal;

    public FormLaporan() {
        setLayout(new BorderLayout(12, 12));
        setBackground(NeoBrutalTheme.BG);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        add(buildNorth(), BorderLayout.NORTH);

        JLabel lblHint = new JLabel(
                "<html><center>Pilih jenis laporan, tentukan periode (kecuali Data Buku),<br>"
                + "lalu klik Tampilkan atau Export PDF.</center></html>",
                SwingConstants.CENTER);
        lblHint.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 14));
        add(lblHint, BorderLayout.CENTER);
    }

    private JPanel buildNorth() {
        JPanel wrap = new JPanel(new BorderLayout(8, 8));
        wrap.setBackground(NeoBrutalTheme.BG);

        JLabel lblJudul = new JLabel("Laporan");
        lblJudul.setFont(new Font("Segoe UI Black", Font.BOLD, 22));
        lblJudul.setForeground(Color.BLACK);
        wrap.add(lblJudul, BorderLayout.NORTH);

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(NeoBrutalTheme.SURFACE);
        card.setBorder(new NeoShadowBorder());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.anchor = GridBagConstraints.WEST;

        cmbJenis = new JComboBox<>();
        cmbJenis.setBackground(NeoBrutalTheme.SURFACE);
        cmbJenis.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        if (Sesi.isAdmin()) {
            cmbJenis.addItem(JENIS_BUKU);
            cmbJenis.addItem(JENIS_PENJUALAN);
            cmbJenis.addItem(JENIS_PEMBELIAN);
            cmbJenis.addItem(JENIS_STOK);
            cmbJenis.addItem(JENIS_PENDAPATAN);
            cmbJenis.addItem(JENIS_TERLARIS);
        } else {
            cmbJenis.addItem(JENIS_PENJUALAN);
            cmbJenis.setEnabled(false);
        }
        cmbJenis.addActionListener(e -> toggleTanggal());

        dpDari = new DatePicker();
        dpDari.setName("tglDari");
        dpDari.setDateToToday();
        dpSampai = new DatePicker();
        dpSampai.setName("tglSampai");
        dpSampai.setDateToToday();

        pnlTanggal = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pnlTanggal.setBackground(NeoBrutalTheme.SURFACE);
        pnlTanggal.add(filterLabel("Dari"));
        pnlTanggal.add(dpDari);
        pnlTanggal.add(filterLabel("Sampai"));
        pnlTanggal.add(dpSampai);

        JButton btnTampil = styledButton("Tampilkan",
                FontIcon.of(MaterialDesignM.MONITOR, 18, Color.BLACK),
                NeoBrutalTheme.SECONDARY);
        btnTampil.addActionListener(e -> doTampilkan());
        JButton btnExport = styledButton("Export PDF",
                FontIcon.of(MaterialDesignE.EXPORT, 18, Color.BLACK),
                NeoBrutalTheme.SUCCESS);
        btnExport.addActionListener(e -> doExport());

        gbc.gridx = 0;
        gbc.gridy = 0;
        card.add(filterLabel("Jenis Laporan"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        card.add(cmbJenis, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 4;
        gbc.weightx = 1.0;
        card.add(pnlTanggal, gbc);

        JPanel pnlBtn = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlBtn.setBackground(NeoBrutalTheme.SURFACE);
        pnlBtn.add(btnTampil);
        pnlBtn.add(btnExport);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 4;
        card.add(pnlBtn, gbc);

        wrap.add(card, BorderLayout.CENTER);
        toggleTanggal();
        return wrap;
    }

    private JLabel filterLabel(String teks) {
        JLabel lbl = new JLabel(teks);
        lbl.setFont(new Font("Segoe UI Semibold", Font.BOLD, 13));
        return lbl;
    }

    private JButton styledButton(String teks, FontIcon ikon, Color bg) {
        JButton b = new JButton(teks, ikon);
        b.setIconTextGap(8);
        b.setFont(new Font("Segoe UI Semibold", Font.BOLD, 13));
        b.setBackground(bg);
        b.setForeground(Color.BLACK);
        b.setBorder(new NeoShadowBorder());
        b.setFocusPainted(false);
        b.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                b.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                b.setBorder(new NeoShadowBorder());
            }
        });
        return b;
    }

    private String jenisTerpilih() {
        return (String) cmbJenis.getSelectedItem();
    }

    private boolean butuhTanggal() {
        return !JENIS_BUKU.equals(jenisTerpilih());
    }

    private void toggleTanggal() {
        pnlTanggal.setVisible(butuhTanggal());
        revalidate();
        repaint();
    }

    private String jrxmlPath() {
        switch (jenisTerpilih()) {
            case JENIS_PENJUALAN: return "/reports/lap_penjualan.jrxml";
            case JENIS_PEMBELIAN: return "/reports/lap_pembelian.jrxml";
            case JENIS_STOK: return "/reports/lap_stok.jrxml";
            case JENIS_PENDAPATAN: return "/reports/lap_pendapatan.jrxml";
            case JENIS_TERLARIS: return "/reports/lap_terlaris.jrxml";
            case JENIS_BUKU:
            default: return "/reports/lap_data_buku.jrxml";
        }
    }

    private LocalDate[] bacaPeriode() {
        if (!butuhTanggal()) {
            return null;
        }
        LocalDate dari = dpDari.getDate();
        LocalDate sampai = dpSampai.getDate();
        if (dari == null || sampai == null) {
            throw new IllegalStateException("Tanggal dari dan sampai wajib diisi");
        }
        if (dari.isAfter(sampai)) {
            throw new IllegalStateException("Tanggal dari tidak boleh setelah tanggal sampai");
        }
        return new LocalDate[]{dari, sampai};
    }

    private List<?> buildData() {
        String jenis = jenisTerpilih();
        if (JENIS_BUKU.equals(jenis)) {
            return bukuDAO.lapDataBuku();
        }
        LocalDate[] periode = bacaPeriode();
        LocalDate dari = periode[0];
        LocalDate sampai = periode[1];
        switch (jenis) {
            case JENIS_PENJUALAN:
                if (Sesi.isAdmin()) {
                    return penjualanDAO.lapPenjualan(dari, sampai);
                }
                return penjualanDAO.lapPenjualanByUser(dari, sampai, Sesi.userLogin.getIdUser());
            case JENIS_PEMBELIAN:
                return pembelianDAO.lapPembelian(dari, sampai);
            case JENIS_STOK:
                return bukuDAO.getStokMenipis();
            case JENIS_PENDAPATAN:
                return penjualanDAO.lapPendapatan(dari, sampai);
            case JENIS_TERLARIS:
                return penjualanDAO.lapTerlaris(dari, sampai);
            default:
                throw new IllegalStateException("Jenis laporan tidak dikenal: " + jenis);
        }
    }

    private Map<String, Object> buildParams() {
        Map<String, Object> params = new HashMap<>();
        params.put("APP_NAME", AppConfig.APP_NAME);
        if (!butuhTanggal()) {
            params.put("PERIODE", "Semua data");
        } else {
            LocalDate[] periode = bacaPeriode();
            params.put("PERIODE", periode[0].format(FMT_PERIODE) + " s.d. " + periode[1].format(FMT_PERIODE));
        }
        return params;
    }

    private void doTampilkan() {
        try {
            List<?> data = buildData();
            Map<String, Object> params = buildParams();
            ReportHelper.show(jrxmlPath(), data, params);
        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Validasi", JOptionPane.WARNING_MESSAGE);
        } catch (JRException ex) {
            JOptionPane.showMessageDialog(this, "Gagal menampilkan laporan: " + ex.getMessage(),
                    "Gagal", JOptionPane.ERROR_MESSAGE);
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "Gagal mengambil data: " + ex.getMessage(),
                    "Gagal", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doExport() {
        List<?> data;
        Map<String, Object> params;
        try {
            data = buildData();
            params = buildParams();
        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "Gagal mengambil data: " + ex.getMessage(),
                    "Gagal", JOptionPane.ERROR_MESSAGE);
            return;
        }
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Export PDF");
        fc.setFileFilter(new FileNameExtensionFilter("PDF (*.pdf)", "pdf"));
        fc.setSelectedFile(new File("laporan.pdf"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File out = fc.getSelectedFile();
        if (!out.getName().toLowerCase().endsWith(".pdf")) {
            out = new File(out.getParentFile(), out.getName() + ".pdf");
        }
        try {
            ReportHelper.exportPdf(jrxmlPath(), data, params, out);
            JOptionPane.showMessageDialog(this, "PDF tersimpan: " + out.getAbsolutePath());
        } catch (JRException ex) {
            JOptionPane.showMessageDialog(this, "Gagal export PDF: " + ex.getMessage(),
                    "Gagal", JOptionPane.ERROR_MESSAGE);
        }
    }
}
