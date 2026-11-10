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
import java.io.File;
import java.text.NumberFormat;
import java.time.LocalDate;
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
import model.LapStok;
import model.LapSupplier;
import net.sf.jasperreports.engine.JRException;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
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
import util.NeoChartStyle;
import util.NeoShadowBorder;
import util.Sesi;

public class FormLaporan extends JPanel {

    private static final String JENIS_BUKU = "Data Master Barang";
    private static final String JENIS_PENJUALAN = "Penjualan";
    private static final String JENIS_PEMBELIAN = "Pembelian";
    private static final String JENIS_PENDAPATAN = "Pendapatan";
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
    private JComboBox<String> cmbFilterJenis;
    private DatePicker dpDari;
    private DatePicker dpSampai;
    private JPanel pnlTanggal;
    private JPanel pnlFilterJenis;
    private boolean sedangResetFilter;

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

        wrap.add(new NeoPageHeader("LAPORAN", "Pusat Laporan",
                "Rekap performa, cetak dokumen, dan ekspor data.", null),
                BorderLayout.NORTH);

        // Filter Card
        JPanel card = NeoFormStyle.card(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 8, 4, 8);
        gbc.anchor = GridBagConstraints.WEST;

        cmbJenis = NeoFormStyle.combo();
        cmbJenis.setName("jenis_laporan");

        if (Sesi.isAdmin()) {
            cmbJenis.addItem(JENIS_PENJUALAN);
            cmbJenis.addItem(JENIS_LABA_KOTOR);
            cmbJenis.addItem(JENIS_PENDAPATAN);
            cmbJenis.addItem(JENIS_PEMBELIAN);
            cmbJenis.addItem(JENIS_SUPPLIER);
            cmbJenis.addItem(JENIS_RETUR);
            cmbJenis.addItem(JENIS_BUKU);
        } else {
            // Kasir hanya memiliki akses ke Penjualan dan Retur
            cmbJenis.addItem(JENIS_PENJUALAN);
            cmbJenis.addItem(JENIS_RETUR);
        }
        cmbJenis.addActionListener(e -> {
            sedangResetFilter = true;
            cmbFilterJenis.setSelectedItem("Semua");
            sedangResetFilter = false;
            toggleTanggal();
            muatData();
        });

        cmbFilterJenis = new JComboBox<>(new String[]{"Semua", "Buku", "ATK"});
        cmbFilterJenis.setName("filter_jenis");
        NeoFormStyle.styleCombo(cmbFilterJenis);
        cmbFilterJenis.addActionListener(e -> {
            if (!sedangResetFilter) {
                muatData();
            }
        });

        pnlFilterJenis = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pnlFilterJenis.setOpaque(false);
        pnlFilterJenis.add(filterLabel("Jenis Barang"));
        pnlFilterJenis.add(cmbFilterJenis);

        dpDari = new DatePicker();
        dpDari.setName("tglDari");
        dpDari.setDate(LocalDate.now().minusDays(30)); // 30 hari ke belakang default
        dpSampai = new DatePicker();
        dpSampai.setName("tglSampai");
        dpSampai.setDateToToday();

        pnlTanggal = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pnlTanggal.setOpaque(false);
        pnlTanggal.add(filterLabel("Periode"));
        pnlTanggal.add(dpDari);
        pnlTanggal.add(filterLabel("s.d."));
        pnlTanggal.add(dpSampai);

        JButton btnTerapkan = styledButton("Terapkan",
                FontIcon.of(MaterialDesignC.CHECK_BOLD, 16, NeoBrutalTheme.INK),
                NeoBrutalTheme.PRIMARY);
        btnTerapkan.addActionListener(e -> muatData());

        cmbJenis.setMinimumSize(new Dimension(170, 34));

        gbc.gridx = 0; gbc.gridy = 0;
        card.add(filterLabel("Jenis"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 0.6;
        card.add(cmbJenis, gbc);

        gbc.gridx = 2; gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.WEST;
        card.add(pnlTanggal, gbc);

        gbc.gridx = 3;
        gbc.anchor = GridBagConstraints.EAST;
        card.add(btnTerapkan, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        gbc.gridwidth = 4;
        gbc.anchor = GridBagConstraints.WEST;
        card.add(pnlFilterJenis, gbc);

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

        strip.add(createKpiCard("kpi_data", "Total Data", lblKpiData, NeoBrutalTheme.SECONDARY));
        strip.add(createKpiCard("kpi_volume", "Total Volume", lblKpiVolume, NeoBrutalTheme.WARNING));
        strip.add(createKpiCard("kpi_finansial", "Total Finansial", lblKpiFinansial, NeoBrutalTheme.BRAND_TEAL));
        strip.add(createKpiCard("kpi_margin", "Rata-rata / Margin", lblKpiMargin, NeoBrutalTheme.PRIMARY));

        return strip;
    }

    private JPanel createKpiCard(String name, String title, JLabel valueLabel, Color bg) {
        JPanel card = new JPanel(new BorderLayout(2, 1));
        card.setName(name);
        card.setBackground(bg);
        card.setBorder(BorderFactory.createCompoundBorder(
                new NeoShadowBorder(),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));

        JLabel lblTitle = new JLabel(title, SwingConstants.LEFT);
        lblTitle.setFont(NeoBrutalTheme.mono(10));
        lblTitle.setForeground(NeoBrutalTheme.INK);

        valueLabel.setFont(NeoBrutalTheme.display(18));
        valueLabel.setForeground(NeoBrutalTheme.INK);
        valueLabel.setHorizontalAlignment(SwingConstants.LEFT);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildCenterTabs() {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(NeoBrutalTheme.SURFACE);
        wrap.setBorder(new NeoShadowBorder());

        tabCenter = new JTabbedPane();
        tabCenter.setFont(NeoBrutalTheme.display(12));
        tabCenter.setBackground(NeoBrutalTheme.SURFACE);
        tabCenter.setForeground(NeoBrutalTheme.INK);

        // 1. Tab Tabel Preview
        JPanel pnlTabTabel = new JPanel(new BorderLayout(6, 6));
        pnlTabTabel.setBackground(NeoBrutalTheme.SURFACE);
        pnlTabTabel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel pnlSearch = new JPanel(new BorderLayout(8, 0));
        pnlSearch.setOpaque(false);
        JLabel lblCari = NeoFormStyle.label("Pencarian Live");
        txtCariLive = NeoFormStyle.field("cari_live");
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
        tblPreview.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        NeoTableStyle.apply(tblPreview);
        JScrollPane scroll = NeoTableStyle.wrap(tblPreview);
        pnlTabTabel.add(scroll, BorderLayout.CENTER);

        lblStatusTabel = new JLabel("Memuat data...");
        lblStatusTabel.setFont(NeoBrutalTheme.body(java.awt.Font.PLAIN, 11));
        lblStatusTabel.setForeground(NeoBrutalTheme.TEXT_MUTED);
        pnlTabTabel.add(lblStatusTabel, BorderLayout.SOUTH);

        // 2. Tab Kurva & Grafik Visual (JFreeChart)
        pnlChartContainer = new JPanel(new BorderLayout(0, 6));
        pnlChartContainer.setBackground(NeoBrutalTheme.SURFACE);
        pnlChartContainer.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        tabCenter.addTab("Tabel Data Preview",
                FontIcon.of(MaterialDesignF.FORMAT_LIST_BULLETED, 16, NeoBrutalTheme.INK),
                pnlTabTabel);
        tabCenter.addTab("Kurva & Grafik Visual",
                FontIcon.of(MaterialDesignC.CHART_LINE, 16, NeoBrutalTheme.INK),
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
                FontIcon.of(MaterialDesignR.REFRESH, 16, NeoBrutalTheme.INK),
                NeoBrutalTheme.SECONDARY);
        btnSegarkan.addActionListener(e -> muatData());

        JButton btnPrint = styledButton("Pratinjau PDF",
                FontIcon.of(MaterialDesignM.MONITOR, 16, NeoBrutalTheme.INK),
                NeoBrutalTheme.BRAND_TEAL);
        btnPrint.addActionListener(e -> doTampilkanPdf());

        JButton btnExportPdf = styledButton("Export PDF",
                FontIcon.of(MaterialDesignE.EXPORT, 16, NeoBrutalTheme.INK),
                NeoBrutalTheme.WARNING);
        btnExportPdf.addActionListener(e -> doExportPdf());

        JButton btnExportExcel = styledButton("Export Excel (.xlsx)",
                FontIcon.of(MaterialDesignF.FILE_EXCEL_BOX, 16, NeoBrutalTheme.INK),
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
        // Guard init-order: listener combo bisa fire saat konstruksi
        // (setSelectedItem fire walau nilai sama) sebelum tabel dibangun.
        if (modelPreview == null || tblPreview == null) {
            return;
        }
        try {
            currentRawData = terapkanFilterJenis(buildData(), filterTerpilih());
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
                columns = new String[]{"No. Nota", "Tanggal", "Kasir", "Metode", "Diskon", "Total"};
                for (Object o : data) {
                    LapPenjualan p = (LapPenjualan) o;
                    String tgl = p.getTanggal() != null ? p.getTanggal().format(FMT_TGL) : "-";
                    allTableRows.add(new Object[]{
                            p.getNoNota(), tgl, p.getKasir(),
                            p.getMetodeBayar(), formatRp(p.getDiskon()), formatRp(p.getTotal())
                    });
                }
                break;

            case JENIS_LABA_KOTOR:
                columns = new String[]{"No. Nota", "Tanggal", "Kode", "Nama Barang", "Jenis", "Qty", "Modal Satuan", "Jual Satuan", "Total Modal", "Total Omset", "Laba Kotor", "Margin %"};
                for (Object o : data) {
                    LapLabaKotor l = (LapLabaKotor) o;
                    String tgl = l.getTanggal() != null ? l.getTanggal().format(FMT_TGL) : "-";
                    allTableRows.add(new Object[]{
                            l.getNoNota(), tgl, l.getKodeBuku(), l.getJudul(), labelJenis(l.getJenis()),
                            l.getQty() + " pcs", formatRp(l.getHargaBeli()), formatRp(l.getHargaJual()),
                            formatRp(l.getTotalModal()), formatRp(l.getTotalOmset()), formatRp(l.getLabaKotor()),
                            String.format(LOCALE_ID, "%.1f%%", l.getMarginPct())
                    });
                }
                break;

            case JENIS_PENDAPATAN:
                columns = new String[]{"No. Nota", "Tanggal", "Nama Barang", "Jenis", "Qty", "Harga Beli", "Harga Jual", "Keuntungan (Laba)"};
                for (Object o : data) {
                    LapPendapatan p = (LapPendapatan) o;
                    String tgl = p.getTanggal() != null ? p.getTanggal().format(FMT_TGL) : "-";
                    allTableRows.add(new Object[]{
                            p.getNoNota(), tgl, p.getJudul(), labelJenis(p.getJenis()), p.getQty() + " pcs",
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
                columns = new String[]{"No. Retur", "Tanggal", "No. Nota Asli", "Kode", "Nama Barang", "Qty", "Harga Satuan", "Refund Dana", "Alasan"};
                for (Object o : data) {
                    LapRetur r = (LapRetur) o;
                    String tgl = r.getTanggal() != null ? r.getTanggal().format(FMT_TGL) : "-";
                    allTableRows.add(new Object[]{
                            r.getNoRetur(), tgl, r.getNoNota(), r.getKodeBuku(), r.getJudul(),
                            r.getQty() + " pcs", formatRp(r.getHargaJual()), formatRp(r.getTotalRefund()), r.getAlasan()
                    });
                }
                break;

            case JENIS_BUKU:
            default:
                columns = new String[]{"Kode", "Nama Barang", "Jenis", "Kategori", "Penerbit", "Harga Jual", "Stok"};
                for (Object o : data) {
                    LapStok b = (LapStok) o;
                    allTableRows.add(new Object[]{
                            b.getKodeBuku(), b.getJudul(), labelJenis(b.getJenis()), b.getKategori(), b.getPenerbit(),
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
        if (modelPreview == null) {
            return;
        }
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

            case JENIS_BUKU:
            default:
                for (Object o : currentRawData) {
                    LapStok b = (LapStok) o;
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

        if (JENIS_SUPPLIER.equals(jenis)) {
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

        } else if (JENIS_PEMBELIAN.equals(jenis)) {
            // Agregasi harian untuk kurva tren pengeluaran pembelian
            Map<String, Double> mapHarian = new HashMap<>();
            List<String> listHari = new ArrayList<>();
            for (Object o : currentRawData) {
                LapPembelian p = (LapPembelian) o;
                if (p.getTanggal() != null) {
                    String key = p.getTanggal().format(FMT_TGL_PENDEK);
                    mapHarian.put(key, mapHarian.getOrDefault(key, 0.0) + p.getTotal());
                    if (!listHari.contains(key)) listHari.add(key);
                }
            }
            for (String h : listHari) {
                dataset.addValue(mapHarian.get(h) / 1000.0, "Pengeluaran", h);
            }
            chart = ChartFactory.createLineChart(
                    "Kurva Tren Pengeluaran Pembelian Harian (Ribuan Rupiah)",
                    "Tanggal", "Pengeluaran (Ribu Rp)",
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
            // Data Master Barang (Distribusi Kategori)
            Map<String, Integer> mapKat = new HashMap<>();
            for (Object o : currentRawData) {
                LapStok b = (LapStok) o;
                String kat = (b.getKategori() != null && !b.getKategori().isBlank()) ? b.getKategori() : "Lainnya";
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

        // Styling Chart neobrutalism
        NeoChartStyle.apply(chart);
        String judulChart = chart.getTitle() != null ? chart.getTitle().getText() : "";
        chart.setTitle((String) null);

        JLabel lblJudulChart = new JLabel(judulChart);
        lblJudulChart.setFont(NeoBrutalTheme.display(14));
        lblJudulChart.setForeground(NeoBrutalTheme.INK);

        JPanel chartCard = NeoChartStyle.card(chart);
        for (java.awt.Component isi : chartCard.getComponents()) {
            if (isi instanceof ChartPanel) {
                isi.setPreferredSize(new Dimension(0, 0));
                isi.setMinimumSize(new Dimension(0, 0));
            }
        }

        JPanel chartBody = new JPanel(new BorderLayout(0, 6));
        chartBody.setOpaque(false);
        chartBody.add(lblJudulChart, BorderLayout.NORTH);
        chartBody.add(chartCard, BorderLayout.CENTER);

        JPanel pnlChart = new JPanel(new BorderLayout());
        pnlChart.setOpaque(false);
        pnlChart.add(NeoChartStyle.centerCard(chartBody, 800), BorderLayout.CENTER);

        pnlChartContainer.add(pnlChart, BorderLayout.CENTER);
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
            default:
                throw new IllegalStateException("Jenis laporan tidak dikenal: " + jenis);
        }
    }

    private Map<String, Object> buildParams() {
        Map<String, Object> params = new HashMap<>();
        params.put("APP_NAME", AppConfig.APP_NAME);
        params.put("KOTA", "Bekasi");
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
            case JENIS_BUKU:
            default: return "/reports/lap_data_buku.jrxml";
        }
    }

    private String jenisTerpilih() {
        return (String) cmbJenis.getSelectedItem();
    }

    private String filterTerpilih() {
        return cmbFilterJenis == null ? "Semua" : (String) cmbFilterJenis.getSelectedItem();
    }

    private String labelJenis(String jenis) {
        return (jenis == null || jenis.isBlank()) ? "Buku" : jenis;
    }

    public static List<Object> terapkanFilterJenis(List<?> data, String filter) {
        List<Object> hasil = new ArrayList<>();
        if (filter == null || "Semua".equals(filter)) {
            hasil.addAll(data);
            return hasil;
        }
        for (Object o : data) {
            String j = null;
            if (o instanceof LapStok) j = ((LapStok) o).getJenis();
            else if (o instanceof LapPendapatan) j = ((LapPendapatan) o).getJenis();
            else if (o instanceof LapLabaKotor) j = ((LapLabaKotor) o).getJenis();
            if (j == null) j = "Buku";
            if (filter.equals(j)) hasil.add(o);
        }
        return hasil;
    }

    private boolean butuhTanggal() {
        String j = jenisTerpilih();
        return !JENIS_BUKU.equals(j);
    }

    private boolean perluFilterJenis() {
        String j = jenisTerpilih();
        return JENIS_BUKU.equals(j) || JENIS_PENDAPATAN.equals(j) || JENIS_LABA_KOTOR.equals(j);
    }

    private void toggleTanggal() {
        pnlTanggal.setVisible(butuhTanggal());
        boolean tampilFilter = perluFilterJenis();
        pnlFilterJenis.setVisible(tampilFilter);
        if (!tampilFilter) {
            cmbFilterJenis.setSelectedItem("Semua");
        }
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
        lbl.setFont(NeoBrutalTheme.body(java.awt.Font.BOLD, 12));
        lbl.setForeground(NeoBrutalTheme.INK);
        return lbl;
    }

    private NeoButton styledButton(String teks, FontIcon ikon, Color bg) {
        return NeoFormStyle.button(teks, ikon, bg);
    }

    private String formatRp(double val) {
        return "Rp " + FMT_RP.format((long) Math.round(val));
    }

    private String sanitizeName(String s) {
        if (s == null) return "Laporan";
        return s.replaceAll("[^a-zA-Z0-9_-]", "_");
    }
}
