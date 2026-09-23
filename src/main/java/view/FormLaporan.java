package view;

import com.github.lgooddatepicker.components.DatePicker;
import dao.BukuDAO;
import dao.BukuDAOImpl;
import dao.PembelianDAO;
import dao.PembelianDAOImpl;
import dao.PenjualanDAO;
import dao.PenjualanDAOImpl;
import dao.ReturDAO;
import dao.ReturDAOImpl;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import model.Buku;
import model.LapLabaKotor;
import model.LapPembelian;
import model.LapPendapatan;
import model.LapPenjualan;
import model.LapRetur;
import model.LapSupplier;
import model.LapTerlaris;
import net.sf.jasperreports.engine.JRException;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.kordamp.ikonli.materialdesign2.MaterialDesignC;
import org.kordamp.ikonli.materialdesign2.MaterialDesignE;
import org.kordamp.ikonli.materialdesign2.MaterialDesignF;
import org.kordamp.ikonli.materialdesign2.MaterialDesignM;
import org.kordamp.ikonli.materialdesign2.MaterialDesignR;
import org.kordamp.ikonli.swing.FontIcon;
import report.ReportHelper;
import util.AppConfig;
import util.ExcelExporter;
import util.NeoBrutalTheme;
import util.NeoShadowBorder;
import util.Sesi;

public class FormLaporan extends JPanel {

    private static final String JENIS_BUKU = "Data Master Buku";
    private static final String JENIS_PENJUALAN = "Penjualan";
    private static final String JENIS_PEMBELIAN = "Pembelian";
    private static final String JENIS_STOK = "Stok Menipis";
    private static final String JENIS_PENDAPATAN = "Pendapatan";
    private static final String JENIS_TERLARIS = "Buku Terlaris";
    private static final String JENIS_RETUR = "Retur Penjualan";
    private static final String JENIS_LABA_KOTOR = "Laba Kotor / Margin";
    private static final String JENIS_SUPPLIER = "Pengadaan Supplier";

    private static final Locale LOCALE_ID = new Locale("id", "ID");
    private static final DateTimeFormatter FMT_PERIODE = DateTimeFormatter.ofPattern("dd MMM yyyy", LOCALE_ID);
    private static final DateTimeFormatter FMT_TGL = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
    private static final DateTimeFormatter FMT_TGL_PENDEK = DateTimeFormatter.ofPattern("dd/MM");
    private static final NumberFormat FMT_RP = NumberFormat.getInstance(LOCALE_ID);

    private final BukuDAO bukuDAO = new BukuDAOImpl();
    private final PenjualanDAO penjualanDAO = new PenjualanDAOImpl();
    private final PembelianDAO pembelianDAO = new PembelianDAOImpl();
    private final ReturDAO returDAO = new ReturDAOImpl();

    private JComboBox<String> cmbJenis;
    private DatePicker dpDari;
    private DatePicker dpSampai;
    private JPanel pnlTanggal;

    // KPI Card Labels
    private JLabel lblKpiData;
    private JLabel lblKpiVolume;
    private JLabel lblKpiFinansial;
    private JLabel lblKpiMargin;

    // Center Tabs
    private JTabbedPane tabCenter;
    private JTable tblPreview;
    private DefaultTableModel modelPreview;
    private JTextField txtCariLive;
    private JLabel lblStatusTabel;
    private JPanel pnlChartContainer;

    // Data Cache
    private List<?> currentRawData = new ArrayList<>();
    private List<Object[]> allTableRows = new ArrayList<>();

    public FormLaporan() {
        setLayout(new BorderLayout(12, 12));
        setBackground(NeoBrutalTheme.BG);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // 1. Header & Filter Bar
        add(buildHeaderAndFilter(), BorderLayout.NORTH);

        // 2. Center Panel: KPI Cards + Tabs (Table & JFreeChart)
        JPanel pnlCenter = new JPanel(new BorderLayout(10, 10));
        pnlCenter.setOpaque(false);
        pnlCenter.add(buildKpiStrip(), BorderLayout.NORTH);
        pnlCenter.add(buildCenterTabs(), BorderLayout.CENTER);
        add(pnlCenter, BorderLayout.CENTER);

        // 3. Bottom Action Bar
        add(buildBottomActionBar(), BorderLayout.SOUTH);

        // Muat data awal otomatis
        muatData();
    }

    private JPanel buildHeaderAndFilter() {
        JPanel wrap = new JPanel(new BorderLayout(8, 8));
        wrap.setBackground(NeoBrutalTheme.BG);

        // Judul Dashboard
        JPanel pnlTitle = new JPanel(new BorderLayout(4, 2));
        pnlTitle.setOpaque(false);
        JLabel lblJudul = new JLabel("Pusat Laporan & Analisis Bisnis");
        lblJudul.setFont(new Font("Segoe UI Black", Font.BOLD, 22));
        lblJudul.setForeground(Color.BLACK);
        JLabel lblSub = new JLabel("Rekapitulasi performa finansial, volume stok gudang, tren kurva grafik, dan cetak dokumen resmi");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(new Color(90, 90, 90));
        pnlTitle.add(lblJudul, BorderLayout.NORTH);
        pnlTitle.add(lblSub, BorderLayout.SOUTH);
        wrap.add(pnlTitle, BorderLayout.NORTH);

        // Filter Card
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(NeoBrutalTheme.SURFACE);
        card.setBorder(new NeoShadowBorder());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 10, 6, 10);
        gbc.anchor = GridBagConstraints.WEST;

        cmbJenis = new JComboBox<>();
        cmbJenis.setFont(new Font("Segoe UI Semibold", Font.BOLD, 13));
        cmbJenis.setBackground(NeoBrutalTheme.SURFACE);
        cmbJenis.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));

        if (Sesi.isAdmin()) {
            cmbJenis.addItem(JENIS_PENJUALAN);
            cmbJenis.addItem(JENIS_LABA_KOTOR);
            cmbJenis.addItem(JENIS_PENDAPATAN);
            cmbJenis.addItem(JENIS_PEMBELIAN);
            cmbJenis.addItem(JENIS_SUPPLIER);
            cmbJenis.addItem(JENIS_RETUR);
            cmbJenis.addItem(JENIS_TERLARIS);
            cmbJenis.addItem(JENIS_STOK);
            cmbJenis.addItem(JENIS_BUKU);
        } else {
            // Kasir hanya memiliki akses ke Penjualan dan Retur
            cmbJenis.addItem(JENIS_PENJUALAN);
            cmbJenis.addItem(JENIS_RETUR);
        }
        cmbJenis.addActionListener(e -> {
            toggleTanggal();
            muatData();
        });

        dpDari = new DatePicker();
        dpDari.setName("tglDari");
        dpDari.setDate(LocalDate.now().minusDays(30)); // 30 hari ke belakang default
        dpSampai = new DatePicker();
        dpSampai.setName("tglSampai");
        dpSampai.setDateToToday();

        pnlTanggal = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pnlTanggal.setBackground(NeoBrutalTheme.SURFACE);
        pnlTanggal.add(filterLabel("Periode: Dari"));
        pnlTanggal.add(dpDari);
        pnlTanggal.add(filterLabel("s.d."));
        pnlTanggal.add(dpSampai);

        JButton btnTerapkan = styledButton("Terapkan Filter",
                FontIcon.of(MaterialDesignC.CHECK_BOLD, 16, Color.BLACK),
                NeoBrutalTheme.PRIMARY);
        btnTerapkan.addActionListener(e -> muatData());

        gbc.gridx = 0; gbc.gridy = 0;
        card.add(filterLabel("Jenis Laporan:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 0.5;
        card.add(cmbJenis, gbc);

        gbc.gridx = 3; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        card.add(pnlTanggal, gbc);

        gbc.gridx = 4;
        card.add(btnTerapkan, gbc);

        wrap.add(card, BorderLayout.CENTER);
        toggleTanggal();
        return wrap;
    }

    private JPanel buildKpiStrip() {
        JPanel strip = new JPanel(new GridLayout(1, 4, 10, 0));
        strip.setOpaque(false);

        lblKpiData = new JLabel("0 Data", SwingConstants.CENTER);
        lblKpiVolume = new JLabel("0 Pcs", SwingConstants.CENTER);
        lblKpiFinansial = new JLabel("Rp 0", SwingConstants.CENTER);
        lblKpiMargin = new JLabel("0%", SwingConstants.CENTER);

        strip.add(createKpiCard("TOTAL DATA", lblKpiData, new Color(224, 231, 255))); // Soft blue
        strip.add(createKpiCard("TOTAL VOLUME BUKU", lblKpiVolume, new Color(254, 240, 138))); // Soft yellow
        strip.add(createKpiCard("TOTAL NOMINAL FINANSIAL", lblKpiFinansial, NeoBrutalTheme.PRIMARY)); // Neo Mint
        strip.add(createKpiCard("RATA-RATA / MARGIN", lblKpiMargin, new Color(254, 205, 211))); // Soft rose

        return strip;
    }

    private JPanel createKpiCard(String title, JLabel valueLabel, Color bg) {
        JPanel card = new JPanel(new BorderLayout(4, 4));
        card.setBackground(bg);
        card.setBorder(new NeoShadowBorder());

        JLabel lblTitle = new JLabel(title, SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI Semibold", Font.BOLD, 11));
        lblTitle.setForeground(new Color(60, 60, 60));

        valueLabel.setFont(new Font("Segoe UI Black", Font.BOLD, 17));
        valueLabel.setForeground(Color.BLACK);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        card.setBorder(BorderFactory.createCompoundBorder(
                new NeoShadowBorder(),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        return card;
    }

    private JPanel buildCenterTabs() {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(NeoBrutalTheme.SURFACE);
        wrap.setBorder(new NeoShadowBorder());

        tabCenter = new JTabbedPane();
        tabCenter.setFont(new Font("Segoe UI Semibold", Font.BOLD, 13));

        // 1. Tab Tabel Preview
        JPanel pnlTabTabel = new JPanel(new BorderLayout(6, 6));
        pnlTabTabel.setBackground(NeoBrutalTheme.SURFACE);
        pnlTabTabel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel pnlSearch = new JPanel(new BorderLayout(8, 0));
        pnlSearch.setBackground(NeoBrutalTheme.SURFACE);
        JLabel lblCari = new JLabel("Pencarian Live di Tabel:");
        lblCari.setFont(new Font("Segoe UI Semibold", Font.BOLD, 12));
        txtCariLive = new JTextField();
        txtCariLive.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtCariLive.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        txtCariLive.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { filterTabelLive(); }
            @Override public void removeUpdate(DocumentEvent e) { filterTabelLive(); }
            @Override public void changedUpdate(DocumentEvent e) { filterTabelLive(); }
        });
        pnlSearch.add(lblCari, BorderLayout.WEST);
        pnlSearch.add(txtCariLive, BorderLayout.CENTER);
        pnlTabTabel.add(pnlSearch, BorderLayout.NORTH);

        modelPreview = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblPreview = new JTable(modelPreview);
        tblPreview.setRowHeight(25);
        tblPreview.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblPreview.getTableHeader().setFont(new Font("Segoe UI Semibold", Font.BOLD, 12));
        tblPreview.getTableHeader().setBackground(new Color(241, 245, 249));
        JScrollPane scroll = new JScrollPane(tblPreview);
        scroll.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        pnlTabTabel.add(scroll, BorderLayout.CENTER);

        lblStatusTabel = new JLabel("Memuat data...");
        lblStatusTabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnlTabTabel.add(lblStatusTabel, BorderLayout.SOUTH);

        // 2. Tab Kurva & Grafik Visual (JFreeChart)
        pnlChartContainer = new JPanel(new BorderLayout());
        pnlChartContainer.setBackground(Color.WHITE);
        pnlChartContainer.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        tabCenter.addTab("Tabel Data Preview",
                FontIcon.of(MaterialDesignF.FORMAT_LIST_BULLETED, 16, Color.BLACK),
                pnlTabTabel);
        tabCenter.addTab("Kurva & Grafik Visual",
                FontIcon.of(MaterialDesignC.CHART_LINE, 16, Color.BLACK),
                pnlChartContainer);

        wrap.add(tabCenter, BorderLayout.CENTER);
        return wrap;
    }

    private JPanel buildBottomActionBar() {
        JPanel bar = new JPanel(new BorderLayout(8, 8));
        bar.setOpaque(false);

        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlButtons.setOpaque(false);

        JButton btnSegarkan = styledButton("Segarkan",
                FontIcon.of(MaterialDesignR.REFRESH, 18, Color.BLACK),
                NeoBrutalTheme.SECONDARY);
        btnSegarkan.addActionListener(e -> muatData());

        JButton btnPrint = styledButton("Pratinjau PDF",
                FontIcon.of(MaterialDesignM.MONITOR, 18, Color.BLACK),
                new Color(199, 210, 254)); // Soft indigo
        btnPrint.addActionListener(e -> doTampilkanPdf());

        JButton btnExportPdf = styledButton("Export PDF",
                FontIcon.of(MaterialDesignE.EXPORT, 18, Color.BLACK),
                new Color(254, 205, 211)); // Soft rose
        btnExportPdf.addActionListener(e -> doExportPdf());

        JButton btnExportExcel = styledButton("Export Excel (.xlsx)",
                FontIcon.of(MaterialDesignF.FILE_EXCEL_BOX, 18, Color.BLACK),
                NeoBrutalTheme.PRIMARY);
        btnExportExcel.addActionListener(e -> doExportExcel());

        pnlButtons.add(btnSegarkan);
        pnlButtons.add(btnPrint);
        pnlButtons.add(btnExportPdf);
        pnlButtons.add(btnExportExcel);

        bar.add(pnlButtons, BorderLayout.EAST);
        return bar;
    }

    // =========================================================================
    // LOGIKA DATA & RENDERING
    // =========================================================================

    private void muatData() {
        try {
            currentRawData = buildData();
            setupTableColumnsAndData(currentRawData);
            updateKpiMetrics();
            updateChartVisual();
        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Validasi Filter", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal memuat data laporan: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void setupTableColumnsAndData(List<?> data) {
        String jenis = jenisTerpilih();
        allTableRows = new ArrayList<>();
        String[] columns;

        switch (jenis) {
            case JENIS_PENJUALAN:
                columns = new String[]{"No. Nota", "Tanggal", "Kasir", "Pelanggan", "Metode", "Diskon", "Total"};
                for (Object o : data) {
                    LapPenjualan p = (LapPenjualan) o;
                    String tgl = p.getTanggal() != null ? p.getTanggal().format(FMT_TGL) : "-";
                    allTableRows.add(new Object[]{
                            p.getNoNota(), tgl, p.getKasir(), p.getMember(),
                            p.getMetodeBayar(), formatRp(p.getDiskon()), formatRp(p.getTotal())
                    });
                }
                break;

            case JENIS_LABA_KOTOR:
                columns = new String[]{"No. Nota", "Tanggal", "Kode", "Judul Buku", "Qty", "Modal Satuan", "Jual Satuan", "Total Modal", "Total Omset", "Laba Kotor", "Margin %"};
                for (Object o : data) {
                    LapLabaKotor l = (LapLabaKotor) o;
                    String tgl = l.getTanggal() != null ? l.getTanggal().format(FMT_TGL) : "-";
                    allTableRows.add(new Object[]{
                            l.getNoNota(), tgl, l.getKodeBuku(), l.getJudul(),
                            l.getQty() + " pcs", formatRp(l.getHargaBeli()), formatRp(l.getHargaJual()),
                            formatRp(l.getTotalModal()), formatRp(l.getTotalOmset()), formatRp(l.getLabaKotor()),
                            String.format(LOCALE_ID, "%.1f%%", l.getMarginPct())
                    });
                }
                break;

            case JENIS_PENDAPATAN:
                columns = new String[]{"No. Nota", "Tanggal", "Judul Buku", "Qty", "Harga Beli", "Harga Jual", "Keuntungan (Laba)"};
                for (Object o : data) {
                    LapPendapatan p = (LapPendapatan) o;
                    String tgl = p.getTanggal() != null ? p.getTanggal().format(FMT_TGL) : "-";
                    allTableRows.add(new Object[]{
                            p.getNoNota(), tgl, p.getJudul(), p.getQty() + " pcs",
                            formatRp(p.getHargaBeli()), formatRp(p.getHargaJual()), formatRp(p.getLaba())
                    });
                }
                break;

            case JENIS_PEMBELIAN:
                columns = new String[]{"No. Faktur", "Tanggal", "Mitra Supplier", "Petugas Penerima", "Total Biaya"};
                for (Object o : data) {
                    LapPembelian p = (LapPembelian) o;
                    String tgl = p.getTanggal() != null ? p.getTanggal().format(FMT_TGL) : "-";
                    allTableRows.add(new Object[]{
                            p.getNoFaktur(), tgl, p.getSupplier(), p.getKasir(), formatRp(p.getTotal())
                    });
                }
                break;

            case JENIS_SUPPLIER:
                columns = new String[]{"ID", "Nama Supplier", "No. Telepon", "Alamat", "Total Faktur", "Total Buku", "Total Pembelian"};
                for (Object o : data) {
                    LapSupplier s = (LapSupplier) o;
                    allTableRows.add(new Object[]{
                            s.getIdSupplier(), s.getNamaSupplier(), s.getNoTelp(), s.getAlamat(),
                            s.getTotalFaktur() + " Faktur", s.getTotalPcs() + " pcs", formatRp(s.getTotalBiaya())
                    });
                }
                break;

            case JENIS_RETUR:
                columns = new String[]{"No. Retur", "Tanggal", "No. Nota Asli", "Kode Buku", "Judul Buku", "Qty", "Harga Satuan", "Refund Dana", "Alasan"};
                for (Object o : data) {
                    LapRetur r = (LapRetur) o;
                    String tgl = r.getTanggal() != null ? r.getTanggal().format(FMT_TGL) : "-";
                    allTableRows.add(new Object[]{
                            r.getNoRetur(), tgl, r.getNoNota(), r.getKodeBuku(), r.getJudul(),
                            r.getQty() + " pcs", formatRp(r.getHargaJual()), formatRp(r.getTotalRefund()), r.getAlasan()
                    });
                }
                break;

            case JENIS_TERLARIS:
                columns = new String[]{"Kode Buku", "Judul Buku", "Kuantitas Terjual", "Total Omset"};
                for (Object o : data) {
                    LapTerlaris t = (LapTerlaris) o;
                    allTableRows.add(new Object[]{
                            t.getKodeBuku(), t.getJudul(), t.getTotalQty() + " pcs", formatRp(t.getTotalOmzet())
                    });
                }
                break;

            case JENIS_STOK:
                columns = new String[]{"Kode Buku", "Judul Buku", "Kategori", "Penerbit", "Harga Jual", "Sisa Stok Fisik"};
                for (Object o : data) {
                    Buku b = (Buku) o;
                    allTableRows.add(new Object[]{
                            b.getKodeBuku(), b.getJudul(), b.getNamaKategori(), b.getNamaPenerbit(),
                            formatRp(b.getHargaJual()), b.getStok() + " pcs"
                    });
                }
                break;

            case JENIS_BUKU:
            default:
                columns = new String[]{"Kode Buku", "Judul Buku", "Kategori", "Penerbit", "Harga Jual", "Stok"};
                for (Object o : data) {
                    Buku b = (Buku) o;
                    allTableRows.add(new Object[]{
                            b.getKodeBuku(), b.getJudul(), b.getNamaKategori(), b.getNamaPenerbit(),
                            formatRp(b.getHargaJual()), b.getStok() + " pcs"
                    });
                }
                break;
        }

        modelPreview.setDataVector(allTableRows.toArray(new Object[0][0]), columns);
        alignTableCells();
        lblStatusTabel.setText("Menampilkan seluruh " + allTableRows.size() + " baris data laporan.");
    }

    private void alignTableCells() {
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);

        for (int i = 0; i < tblPreview.getColumnCount(); i++) {
            String colName = tblPreview.getColumnName(i).toLowerCase();
            if (colName.contains("total") || colName.contains("harga") || colName.contains("modal")
                    || colName.contains("jual") || colName.contains("omset") || colName.contains("laba")
                    || colName.contains("biaya") || colName.contains("refund") || colName.contains("qty")
                    || colName.contains("stok") || colName.contains("terjual") || colName.contains("margin")) {
                tblPreview.getColumnModel().getColumn(i).setCellRenderer(rightRenderer);
            }
        }
    }

    private void filterTabelLive() {
        String kw = txtCariLive != null ? txtCariLive.getText().trim().toLowerCase() : "";
        if (kw.isEmpty()) {
            modelPreview.setRowCount(0);
            for (Object[] r : allTableRows) {
                modelPreview.addRow(r);
            }
            lblStatusTabel.setText("Menampilkan seluruh " + allTableRows.size() + " baris data.");
            return;
        }

        modelPreview.setRowCount(0);
        int matched = 0;
        for (Object[] row : allTableRows) {
            boolean match = false;
            for (Object cell : row) {
                if (cell != null && cell.toString().toLowerCase().contains(kw)) {
                    match = true;
                    break;
                }
            }
            if (match) {
                modelPreview.addRow(row);
                matched++;
            }
        }
        lblStatusTabel.setText("Ditemukan " + matched + " dari " + allTableRows.size() + " baris (filter kata kunci: \"" + kw + "\").");
    }

    private void updateKpiMetrics() {
        int count = currentRawData.size();
        lblKpiData.setText(count + " Data");

        int totalPcs = 0;
        double totalFinansial = 0.0;
        String marginText = "-";

        String jenis = jenisTerpilih();
        switch (jenis) {
            case JENIS_PENJUALAN:
                for (Object o : currentRawData) {
                    LapPenjualan p = (LapPenjualan) o;
                    totalFinansial += p.getTotal();
                }
                lblKpiVolume.setText("-");
                lblKpiFinansial.setText(formatRp(totalFinansial));
                marginText = count > 0 ? formatRp(totalFinansial / count) + " /Trx" : "-";
                break;

            case JENIS_LABA_KOTOR:
                double sumOmset = 0.0;
                double sumLaba = 0.0;
                for (Object o : currentRawData) {
                    LapLabaKotor l = (LapLabaKotor) o;
                    totalPcs += l.getQty();
                    sumOmset += l.getTotalOmset();
                    sumLaba += l.getLabaKotor();
                }
                totalFinansial = sumLaba;
                lblKpiVolume.setText(totalPcs + " Pcs");
                lblKpiFinansial.setText(formatRp(totalFinansial));
                marginText = sumOmset > 0 ? String.format(LOCALE_ID, "%.1f%% Margin", (sumLaba / sumOmset) * 100.0) : "0%";
                break;

            case JENIS_PENDAPATAN:
                for (Object o : currentRawData) {
                    LapPendapatan p = (LapPendapatan) o;
                    totalPcs += p.getQty();
                    totalFinansial += p.getLaba();
                }
                lblKpiVolume.setText(totalPcs + " Pcs");
                lblKpiFinansial.setText(formatRp(totalFinansial));
                marginText = count > 0 ? formatRp(totalFinansial / count) + " /Item" : "-";
                break;

            case JENIS_PEMBELIAN:
                for (Object o : currentRawData) {
                    LapPembelian p = (LapPembelian) o;
                    totalFinansial += p.getTotal();
                }
                lblKpiVolume.setText("-");
                lblKpiFinansial.setText(formatRp(totalFinansial));
                marginText = count > 0 ? formatRp(totalFinansial / count) + " /Faktur" : "-";
                break;

            case JENIS_SUPPLIER:
                for (Object o : currentRawData) {
                    LapSupplier s = (LapSupplier) o;
                    totalPcs += s.getTotalPcs();
                    totalFinansial += s.getTotalBiaya();
                }
                lblKpiVolume.setText(totalPcs + " Pcs");
                lblKpiFinansial.setText(formatRp(totalFinansial));
                marginText = count > 0 ? formatRp(totalFinansial / count) + " /Mitra" : "-";
                break;

            case JENIS_RETUR:
                for (Object o : currentRawData) {
                    LapRetur r = (LapRetur) o;
                    totalPcs += r.getQty();
                    totalFinansial += r.getTotalRefund();
                }
                lblKpiVolume.setText(totalPcs + " Pcs");
                lblKpiFinansial.setText(formatRp(totalFinansial));
                marginText = count > 0 ? formatRp(totalFinansial / count) + " /Retur" : "-";
                break;

            case JENIS_TERLARIS:
                for (Object o : currentRawData) {
                    LapTerlaris t = (LapTerlaris) o;
                    totalPcs += t.getTotalQty();
                    totalFinansial += t.getTotalOmzet();
                }
                lblKpiVolume.setText(totalPcs + " Pcs");
                lblKpiFinansial.setText(formatRp(totalFinansial));
                marginText = "Top " + Math.min(10, count);
                break;

            case JENIS_STOK:
            case JENIS_BUKU:
            default:
                for (Object o : currentRawData) {
                    Buku b = (Buku) o;
                    totalPcs += b.getStok();
                    totalFinansial += (b.getStok() * b.getHargaJual());
                }
                lblKpiVolume.setText(totalPcs + " Pcs");
                lblKpiFinansial.setText(formatRp(totalFinansial));
                marginText = count > 0 ? formatRp(totalFinansial / count) + " /Judul" : "-";
                break;
        }

        lblKpiMargin.setText(marginText);
    }

    private void updateChartVisual() {
        pnlChartContainer.removeAll();
        String jenis = jenisTerpilih();
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        JFreeChart chart;

        if (JENIS_TERLARIS.equals(jenis)) {
            int limit = Math.min(10, currentRawData.size());
            for (int i = 0; i < limit; i++) {
                LapTerlaris t = (LapTerlaris) currentRawData.get(i);
                String judulPendek = t.getJudul().length() > 20 ? t.getJudul().substring(0, 18) + "..." : t.getJudul();
                dataset.addValue(t.getTotalQty(), "Terjual (Pcs)", judulPendek);
            }
            chart = ChartFactory.createBarChart(
                    "Top 10 Buku Paling Terlaris",
                    "Judul Buku", "Kuantitas Terjual (Pcs)",
                    dataset, PlotOrientation.HORIZONTAL, false, true, false);

        } else if (JENIS_SUPPLIER.equals(jenis)) {
            for (Object o : currentRawData) {
                LapSupplier s = (LapSupplier) o;
                dataset.addValue(s.getTotalBiaya() / 1_000_000.0, "Pengadaan (Juta Rp)", s.getNamaSupplier());
            }
            chart = ChartFactory.createBarChart(
                    "Total Belanja Pengadaan per Mitra Penerbit / Supplier",
                    "Nama Supplier", "Total Belanja (Juta Rupiah)",
                    dataset, PlotOrientation.VERTICAL, false, true, false);

        } else if (JENIS_LABA_KOTOR.equals(jenis) || JENIS_PENJUALAN.equals(jenis) || JENIS_PENDAPATAN.equals(jenis)) {
            // Agregasi harian untuk kurva tren
            Map<String, Double> mapHarian = new HashMap<>();
            List<String> listHari = new ArrayList<>();

            if (JENIS_LABA_KOTOR.equals(jenis)) {
                for (Object o : currentRawData) {
                    LapLabaKotor l = (LapLabaKotor) o;
                    if (l.getTanggal() != null) {
                        String key = l.getTanggal().format(FMT_TGL_PENDEK);
                        mapHarian.put(key, mapHarian.getOrDefault(key, 0.0) + l.getLabaKotor());
                        if (!listHari.contains(key)) listHari.add(key);
                    }
                }
            } else if (JENIS_PENJUALAN.equals(jenis)) {
                for (Object o : currentRawData) {
                    LapPenjualan p = (LapPenjualan) o;
                    if (p.getTanggal() != null) {
                        String key = p.getTanggal().format(FMT_TGL_PENDEK);
                        mapHarian.put(key, mapHarian.getOrDefault(key, 0.0) + p.getTotal());
                        if (!listHari.contains(key)) listHari.add(key);
                    }
                }
            } else {
                for (Object o : currentRawData) {
                    LapPendapatan p = (LapPendapatan) o;
                    if (p.getTanggal() != null) {
                        String key = p.getTanggal().format(FMT_TGL_PENDEK);
                        mapHarian.put(key, mapHarian.getOrDefault(key, 0.0) + p.getLaba());
                        if (!listHari.contains(key)) listHari.add(key);
                    }
                }
            }

            for (String h : listHari) {
                dataset.addValue(mapHarian.get(h) / 1000.0, "Nilai", h);
            }

            chart = ChartFactory.createLineChart(
                    "Kurva Tren Finansial Harian (Ribuan Rupiah)",
                    "Tanggal", "Nominal (Ribu Rp)",
                    dataset, PlotOrientation.VERTICAL, false, true, false);

        } else if (JENIS_RETUR.equals(jenis)) {
            for (Object o : currentRawData) {
                LapRetur r = (LapRetur) o;
                String judulPendek = r.getJudul().length() > 16 ? r.getJudul().substring(0, 14) + ".." : r.getJudul();
                dataset.addValue(r.getQty(), "Retur (Pcs)", judulPendek);
            }
            chart = ChartFactory.createBarChart(
                    "Volume Retur Buku Masuk Gudang",
                    "Buku", "Jumlah Diretur (Pcs)",
                    dataset, PlotOrientation.VERTICAL, false, true, false);

        } else {
            // Data Master Buku / Stok Menipis (Distribusi Kategori)
            Map<String, Integer> mapKat = new HashMap<>();
            for (Object o : currentRawData) {
                Buku b = (Buku) o;
                String kat = (b.getNamaKategori() != null && !b.getNamaKategori().isBlank()) ? b.getNamaKategori() : "Lainnya";
                mapKat.put(kat, mapKat.getOrDefault(kat, 0) + b.getStok());
            }
            for (Map.Entry<String, Integer> e : mapKat.entrySet()) {
                dataset.addValue(e.getValue(), "Stok", e.getKey());
            }
            chart = ChartFactory.createBarChart(
                    "Distribusi Stok Fisik Berdasarkan Kategori Buku",
                    "Kategori", "Jumlah Stok Fisik (Pcs)",
                    dataset, PlotOrientation.VERTICAL, false, true, false);
        }

        // Styling Chart
        chart.setBackgroundPaint(Color.WHITE);
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(new Color(248, 250, 252));
        plot.setDomainGridlinePaint(new Color(226, 232, 240));
        plot.setRangeGridlinePaint(new Color(203, 213, 225));

        if (plot.getRenderer() instanceof BarRenderer) {
            BarRenderer br = (BarRenderer) plot.getRenderer();
            br.setSeriesPaint(0, new Color(14, 165, 233)); // Sky blue
            br.setDrawBarOutline(true);
            br.setSeriesOutlinePaint(0, Color.BLACK);
        } else if (plot.getRenderer() instanceof LineAndShapeRenderer) {
            LineAndShapeRenderer lr = (LineAndShapeRenderer) plot.getRenderer();
            lr.setSeriesPaint(0, new Color(16, 185, 129)); // Emerald green
        }

        ChartPanel cp = new ChartPanel(chart);
        cp.setPreferredSize(new Dimension(500, 300));
        pnlChartContainer.add(cp, BorderLayout.CENTER);
        pnlChartContainer.revalidate();
        pnlChartContainer.repaint();
    }

    // =========================================================================
    // EXPORT & REPORT ACTIONS
    // =========================================================================

    private void doTampilkanPdf() {
        try {
            Map<String, Object> params = buildParams();
            ReportHelper.show(jrxmlPath(), currentRawData, params);
        } catch (JRException ex) {
            JOptionPane.showMessageDialog(this, "Gagal merender dokumen PDF: " + ex.getMessage(),
                    "Gagal Render", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doExportPdf() {
        if (currentRawData.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tidak ada data yang dapat diekspor.",
                    "Data Kosong", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Export Laporan Resmi ke PDF");
        fc.setFileFilter(new FileNameExtensionFilter("PDF Documents (*.pdf)", "pdf"));
        String defName = "Laporan_" + sanitizeName(jenisTerpilih()) + "_" + LocalDate.now() + ".pdf";
        fc.setSelectedFile(new File(defName));

        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File out = fc.getSelectedFile();
            if (!out.getName().toLowerCase().endsWith(".pdf")) {
                out = new File(out.getParentFile(), out.getName() + ".pdf");
            }
            try {
                ReportHelper.exportPdf(jrxmlPath(), currentRawData, buildParams(), out);
                JOptionPane.showMessageDialog(this, "File PDF berhasil disimpan:\n" + out.getAbsolutePath(),
                        "Export Berhasil", JOptionPane.INFORMATION_MESSAGE);
            } catch (JRException ex) {
                JOptionPane.showMessageDialog(this, "Gagal export PDF: " + ex.getMessage(),
                        "Error Export", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void doExportExcel() {
        if (tblPreview.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Tabel tidak memiliki data untuk diekspor ke Excel.",
                    "Data Kosong", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Export Laporan ke Microsoft Excel (.xlsx)");
        fc.setFileFilter(new FileNameExtensionFilter("Microsoft Excel Workbook (*.xlsx)", "xlsx"));
        String defName = "Laporan_" + sanitizeName(jenisTerpilih()) + "_" + LocalDate.now() + ".xlsx";
        fc.setSelectedFile(new File(defName));

        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File out = fc.getSelectedFile();
            if (!out.getName().toLowerCase().endsWith(".xlsx")) {
                out = new File(out.getParentFile(), out.getName() + ".xlsx");
            }
            try {
                String strPeriode = butuhTanggal() ? periodeString() : "Semua Data";
                ExcelExporter.exportJTable(tblPreview, jenisTerpilih(), strPeriode, out);
                JOptionPane.showMessageDialog(this, "File Excel berhasil disimpan:\n" + out.getAbsolutePath(),
                        "Export Excel Sukses", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Gagal membuat file Excel: " + ex.getMessage(),
                        "Error Export Excel", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // =========================================================================
    // QUERY BUILDER & PARAMETERS
    // =========================================================================

    private List<?> buildData() {
        String jenis = jenisTerpilih();
        if (JENIS_BUKU.equals(jenis)) {
            return bukuDAO.lapDataBuku();
        }
        if (JENIS_STOK.equals(jenis)) {
            return bukuDAO.getStokMenipis();
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
            case JENIS_LABA_KOTOR:
                return penjualanDAO.lapLabaKotor(dari, sampai);
            case JENIS_PENDAPATAN:
                return penjualanDAO.lapPendapatan(dari, sampai);
            case JENIS_PEMBELIAN:
                return pembelianDAO.lapPembelian(dari, sampai);
            case JENIS_SUPPLIER:
                return pembelianDAO.lapSupplier(dari, sampai);
            case JENIS_RETUR:
                return returDAO.lapRetur(dari, sampai);
            case JENIS_TERLARIS:
                return penjualanDAO.lapTerlaris(dari, sampai);
            default:
                throw new IllegalStateException("Jenis laporan tidak dikenal: " + jenis);
        }
    }

    private Map<String, Object> buildParams() {
        Map<String, Object> params = new HashMap<>();
        params.put("APP_NAME", AppConfig.APP_NAME);
        params.put("KOTA", "Jakarta");
        params.put("TGL_CETAK", LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy", LOCALE_ID)));
        String namaPetugas = (Sesi.userLogin != null && Sesi.userLogin.getNamaLengkap() != null)
                ? Sesi.userLogin.getNamaLengkap() : "Kasir / Admin";
        params.put("PETUGAS", namaPetugas);

        if (!butuhTanggal()) {
            params.put("PERIODE", "Semua Data Master");
        } else {
            params.put("PERIODE", periodeString());
        }
        return params;
    }

    private String periodeString() {
        LocalDate[] p = bacaPeriode();
        return p[0].format(FMT_PERIODE) + " s.d. " + p[1].format(FMT_PERIODE);
    }

    private String jrxmlPath() {
        switch (jenisTerpilih()) {
            case JENIS_PENJUALAN: return "/reports/lap_penjualan.jrxml";
            case JENIS_LABA_KOTOR: return "/reports/lap_laba_kotor.jrxml";
            case JENIS_PENDAPATAN: return "/reports/lap_pendapatan.jrxml";
            case JENIS_PEMBELIAN: return "/reports/lap_pembelian.jrxml";
            case JENIS_SUPPLIER: return "/reports/lap_supplier.jrxml";
            case JENIS_RETUR: return "/reports/lap_retur.jrxml";
            case JENIS_TERLARIS: return "/reports/lap_terlaris.jrxml";
            case JENIS_STOK: return "/reports/lap_stok.jrxml";
            case JENIS_BUKU:
            default: return "/reports/lap_data_buku.jrxml";
        }
    }

    private String jenisTerpilih() {
        return (String) cmbJenis.getSelectedItem();
    }

    private boolean butuhTanggal() {
        String j = jenisTerpilih();
        return !JENIS_BUKU.equals(j) && !JENIS_STOK.equals(j);
    }

    private void toggleTanggal() {
        pnlTanggal.setVisible(butuhTanggal());
        revalidate();
        repaint();
    }

    private LocalDate[] bacaPeriode() {
        if (!butuhTanggal()) {
            return null;
        }
        LocalDate dari = dpDari.getDate();
        LocalDate sampai = dpSampai.getDate();
        if (dari == null || sampai == null) {
            throw new IllegalStateException("Tanggal awal dan tanggal akhir wajib diisi.");
        }
        if (dari.isAfter(sampai)) {
            throw new IllegalStateException("Tanggal awal tidak boleh melebihi tanggal akhir.");
        }
        return new LocalDate[]{dari, sampai};
    }

    // =========================================================================
    // UI HELPERS
    // =========================================================================

    private JLabel filterLabel(String teks) {
        JLabel lbl = new JLabel(teks);
        lbl.setFont(new Font("Segoe UI Semibold", Font.BOLD, 13));
        return lbl;
    }

    private JButton styledButton(String teks, FontIcon ikon, Color bg) {
        JButton b = ikon == null ? new JButton(teks) : new JButton(teks, ikon);
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

    private String formatRp(double val) {
        return "Rp " + FMT_RP.format((long) Math.round(val));
    }

    private String sanitizeName(String s) {
        if (s == null) return "Laporan";
        return s.replaceAll("[^a-zA-Z0-9_-]", "_");
    }
}
