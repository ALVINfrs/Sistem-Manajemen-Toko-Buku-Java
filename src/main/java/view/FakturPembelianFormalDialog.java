package view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
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
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import model.DetailPembelian;
import model.Pembelian;
import model.Supplier;
import net.sf.jasperreports.engine.JRException;
import org.kordamp.ikonli.materialdesign2.MaterialDesignF;
import org.kordamp.ikonli.materialdesign2.MaterialDesignP;
import org.kordamp.ikonli.swing.FontIcon;
import report.ReportHelper;
import util.AppConfig;
import util.LogoUtil;
import util.NeoBrutalTheme;
import util.NeoShadowBorder;

/**
 * Dialog faktur pembelian formal untuk arsip/riwayat.
 * Sengaja dipisahkan dari {@link FakturPembelianDialog} (bukti pasca-simpan)
 * dan dari struk penjualan agar gaya dokumennya berbeda.
 */
public class FakturPembelianFormalDialog extends JDialog {

    static final String TEMPLATE = "/reports/faktur_pembelian.jrxml";

    private static final NumberFormat FMT_RP = NumberFormat.getInstance(new Locale("id", "ID"));
    private static final DateTimeFormatter FMT_TGL =
            DateTimeFormatter.ofPattern("dd MMMM yyyy HH:mm", new Locale("id", "ID"));
    private static final DateTimeFormatter FMT_TGL_CETAK =
            DateTimeFormatter.ofPattern("dd MMMM yyyy", new Locale("id", "ID"));

    private final Pembelian pembelian;
    private final List<DetailPembelian> items;
    private final Map<String, Object> reportParams;
    private final String penerima;

    public FakturPembelianFormalDialog(Frame owner, Pembelian pembelian,
                                       List<DetailPembelian> items, Supplier supplier,
                                       String penerima) {
        super(owner, "Faktur Pembelian - "
                + (pembelian != null ? safe(pembelian.getNoFaktur()) : "-"), true);
        this.pembelian = pembelian;
        this.items = items != null ? new ArrayList<>(items) : new ArrayList<>();
        this.penerima = safe(penerima);
        this.reportParams = buildReportParams(pembelian, supplier, this.items, penerima);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(0, 10));
        getContentPane().setBackground(NeoBrutalTheme.BG);
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        add(buildTopBar(), BorderLayout.NORTH);
        add(buildPaperScroll(supplier), BorderLayout.CENTER);
        add(buildActions(), BorderLayout.SOUTH);

        setSize(960, 700);
        setMinimumSize(new Dimension(840, 600));
        setLocationRelativeTo(owner);
    }

    // =========================================================================
    // LAYOUT DOKUMEN
    // =========================================================================

    private JPanel buildTopBar() {
        JPanel pnl = new JPanel(new BorderLayout(0, 2));
        pnl.setOpaque(false);
        pnl.add(NeoFormStyle.section("Arsip Dokumen Pembelian"), BorderLayout.NORTH);
        JLabel judul = new JLabel("Faktur Pembelian");
        judul.setFont(NeoBrutalTheme.display(20));
        judul.setForeground(NeoBrutalTheme.INK);
        pnl.add(judul, BorderLayout.SOUTH);
        return pnl;
    }

    private JScrollPane buildPaperScroll(Supplier supplier) {
        JPanel paper = new JPanel(new BorderLayout(0, 14)) {
            @Override
            public Dimension getPreferredSize() {
                Dimension base = super.getPreferredSize();
                return new Dimension(880, base.height);
            }
        };
        paper.setBackground(NeoBrutalTheme.SURFACE);
        paper.setBorder(BorderFactory.createCompoundBorder(
                new NeoShadowBorder(),
                BorderFactory.createEmptyBorder(18, 28, 18, 28)));

        paper.add(buildDocumentHeader(), BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(0, 10));
        body.setOpaque(false);
        body.add(buildPartyBlocks(supplier), BorderLayout.NORTH);
        body.add(buildItemTable(), BorderLayout.CENTER);
        body.add(buildDocumentFooter(), BorderLayout.SOUTH);
        paper.add(body, BorderLayout.CENTER);

        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        wrapper.setOpaque(false);
        wrapper.add(paper);

        JScrollPane scroll = new JScrollPane(wrapper);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    private JPanel buildDocumentHeader() {
        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setOpaque(false);

        JLabel logo = new JLabel(LogoUtil.icon(64));
        header.add(logo, BorderLayout.WEST);

        JPanel store = new JPanel();
        store.setOpaque(false);
        store.setLayout(new BoxLayout(store, BoxLayout.Y_AXIS));
        JLabel nama = new JLabel(AppConfig.APP_NAME);
        nama.setFont(NeoBrutalTheme.display(19));
        nama.setForeground(NeoBrutalTheme.INK);
        nama.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel alamat = new JLabel("<html><body style='width: 390px'>"
                + AppConfig.ALAMAT_TOKO + "</body></html>");
        alamat.setFont(NeoBrutalTheme.body(Font.PLAIN, 9));
        alamat.setForeground(NeoBrutalTheme.TEXT_MUTED);
        alamat.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel telp = new JLabel("Telp: " + AppConfig.NO_TELP_TOKO);
        telp.setFont(NeoBrutalTheme.body(Font.PLAIN, 9));
        telp.setForeground(NeoBrutalTheme.TEXT_MUTED);
        telp.setAlignmentX(Component.LEFT_ALIGNMENT);
        store.add(nama);
        store.add(Box.createVerticalStrut(2));
        store.add(alamat);
        store.add(telp);
        header.add(store, BorderLayout.CENTER);

        JPanel doc = new JPanel();
        doc.setOpaque(false);
        doc.setLayout(new BoxLayout(doc, BoxLayout.Y_AXIS));
        JLabel judul = new JLabel("FAKTUR PEMBELIAN");
        judul.setFont(NeoBrutalTheme.display(17));
        judul.setForeground(NeoBrutalTheme.BRAND_MAROON);
        judul.setAlignmentX(Component.RIGHT_ALIGNMENT);
        JLabel no = new JLabel("No. Faktur: " + safe(pembelian != null
                ? pembelian.getNoFaktur() : null));
        no.setFont(NeoBrutalTheme.mono(10));
        no.setForeground(NeoBrutalTheme.INK);
        no.setAlignmentX(Component.RIGHT_ALIGNMENT);
        JLabel tgl = new JLabel("Tanggal: " + formatTanggal(pembelian != null
                ? pembelian.getTanggal() : null));
        tgl.setFont(NeoBrutalTheme.body(Font.PLAIN, 10));
        tgl.setForeground(NeoBrutalTheme.TEXT_MUTED);
        tgl.setAlignmentX(Component.RIGHT_ALIGNMENT);
        JLabel penerimaLbl = new JLabel("Penerima: " + penerima);
        penerimaLbl.setFont(NeoBrutalTheme.body(Font.PLAIN, 10));
        penerimaLbl.setForeground(NeoBrutalTheme.TEXT_MUTED);
        penerimaLbl.setAlignmentX(Component.RIGHT_ALIGNMENT);
        doc.add(judul);
        doc.add(Box.createVerticalStrut(4));
        doc.add(no);
        doc.add(tgl);
        doc.add(penerimaLbl);
        header.add(doc, BorderLayout.EAST);

        JPanel wrap = new JPanel(new BorderLayout(0, 10));
        wrap.setOpaque(false);
        wrap.add(header, BorderLayout.CENTER);
        JPanel lines = new JPanel(new BorderLayout(0, 2));
        lines.setOpaque(false);
        lines.add(rule(2), BorderLayout.NORTH);
        lines.add(rule(1), BorderLayout.SOUTH);
        wrap.add(lines, BorderLayout.SOUTH);
        return wrap;
    }

    private JPanel buildPartyBlocks(Supplier supplier) {
        JPanel pnl = new JPanel(new GridLayout(1, 2, 28, 0));
        pnl.setOpaque(false);

        String supplierName = supplier != null && supplier.getNamaSupplier() != null
                ? supplier.getNamaSupplier()
                : (pembelian != null ? safe(pembelian.getNamaSupplier()) : "-");
        String supplierAlamat = supplier != null ? safe(supplier.getAlamat()) : "-";
        String supplierTelp = supplier != null ? safe(supplier.getNoTelp()) : "-";

        pnl.add(partyBox("SUPPLIER", supplierName, supplierAlamat, supplierTelp));
        pnl.add(partyBox("KEPADA", AppConfig.APP_NAME,
                AppConfig.ALAMAT_TOKO, AppConfig.NO_TELP_TOKO));
        return pnl;
    }

    private JPanel partyBox(String judul, String nama, String alamat, String telp) {
        JPanel pnl = new JPanel();
        pnl.setOpaque(false);
        pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS));
        pnl.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0,
                        new java.awt.Color(0, 0, 0, 55)),
                BorderFactory.createEmptyBorder(8, 0, 0, 0)));

        JLabel lblJudul = new JLabel(judul);
        lblJudul.setFont(NeoBrutalTheme.mono(9));
        lblJudul.setForeground(NeoBrutalTheme.TEXT_MUTED);
        lblJudul.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblNama = new JLabel(safe(nama));
        lblNama.setFont(NeoBrutalTheme.body(Font.BOLD, 12));
        lblNama.setForeground(NeoBrutalTheme.INK);
        lblNama.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextArea areaAlamat = new JTextArea(safe(alamat));
        areaAlamat.setFont(NeoBrutalTheme.body(Font.PLAIN, 9));
        areaAlamat.setForeground(NeoBrutalTheme.TEXT_MUTED);
        areaAlamat.setOpaque(false);
        areaAlamat.setEditable(false);
        areaAlamat.setLineWrap(true);
        areaAlamat.setWrapStyleWord(true);
        areaAlamat.setFocusable(false);
        areaAlamat.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
        areaAlamat.setAlignmentX(Component.LEFT_ALIGNMENT);
        areaAlamat.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));

        JLabel lblTelp = new JLabel("Telp: " + safe(telp));
        lblTelp.setFont(NeoBrutalTheme.body(Font.PLAIN, 9));
        lblTelp.setForeground(NeoBrutalTheme.TEXT_MUTED);
        lblTelp.setAlignmentX(Component.LEFT_ALIGNMENT);

        pnl.add(lblJudul);
        pnl.add(Box.createVerticalStrut(2));
        pnl.add(lblNama);
        pnl.add(areaAlamat);
        pnl.add(lblTelp);
        return pnl;
    }

    private JScrollPane buildItemTable() {
        DefaultTableModel model = new DefaultTableModel(
                new String[]{"No", "Kode", "Judul", "Qty", "Harga Beli", "Subtotal"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        for (Object[] row : buildItemRows(items)) {
            model.addRow(row);
        }

        JTable table = new JTable(model);
        table.setName("tblItemFakturFormal");
        NeoTableStyle.apply(table);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.setRowHeight(28);
        int[] lebar = {36, 70, 420, 45, 95, 105};
        for (int i = 0; i < lebar.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(lebar[i]);
        }
        DefaultTableCellRenderer right = new DefaultTableCellRenderer();
        right.setHorizontalAlignment(SwingConstants.RIGHT);
        table.getColumnModel().getColumn(3).setCellRenderer(right);
        table.getColumnModel().getColumn(4).setCellRenderer(right);
        table.getColumnModel().getColumn(5).setCellRenderer(right);

        JScrollPane scroll = NeoTableStyle.wrap(table);
        int tinggiTabel = Math.max(120, Math.min(250, items.size() * 28 + 34));
        scroll.setPreferredSize(new Dimension(0, tinggiTabel));
        return scroll;
    }

    private JPanel buildDocumentFooter() {
        JPanel footer = new JPanel(new BorderLayout(0, 10));
        footer.setOpaque(false);

        JPanel summaryWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        summaryWrap.setOpaque(false);
        JPanel summary = new JPanel(new GridLayout(3, 2, 14, 3));
        summary.setOpaque(false);
        summary.setPreferredSize(new Dimension(330, 76));
        summary.add(rightLabel("Total Jenis Buku", Font.PLAIN, 9, NeoBrutalTheme.TEXT_MUTED));
        summary.add(rightValue(items.size() + " judul", Font.PLAIN, 9, NeoBrutalTheme.INK));
        summary.add(rightLabel("Total Qty Masuk", Font.PLAIN, 9, NeoBrutalTheme.TEXT_MUTED));
        summary.add(rightValue(totalQty() + " pcs", Font.PLAIN, 9, NeoBrutalTheme.INK));
        summary.add(rightLabel("TOTAL BIAYA", Font.BOLD, 11, NeoBrutalTheme.INK));
        summary.add(rightValue(formatRp(pembelian != null ? pembelian.getTotal() : totalBiaya()),
                Font.BOLD, 12, NeoBrutalTheme.BRAND_MAROON));
        summaryWrap.add(summary);
        footer.add(summaryWrap, BorderLayout.NORTH);

        JPanel signs = new JPanel(new GridLayout(1, 2, 48, 0));
        signs.setOpaque(false);
        signs.setBorder(BorderFactory.createEmptyBorder(4, 24, 0, 24));
        signs.add(signatureColumn("Penerima,", " ", penerima));
        signs.add(signatureColumn("Bekasi, " + LocalDate.now().format(FMT_TGL_CETAK),
                "Menyetujui,", "Kepala Toko Almira"));
        footer.add(signs, BorderLayout.CENTER);
        return footer;
    }

    private JLabel rightLabel(String text, int style, int size, java.awt.Color color) {
        JLabel label = new JLabel(text, SwingConstants.RIGHT);
        label.setFont(NeoBrutalTheme.body(style, size));
        label.setForeground(color);
        return label;
    }

    private JLabel rightValue(String text, int style, int size, java.awt.Color color) {
        JLabel label = new JLabel(text, SwingConstants.RIGHT);
        label.setFont(NeoBrutalTheme.body(style, size));
        label.setForeground(color);
        return label;
    }

    private JPanel signatureColumn(String line1, String line2, String name) {
        JPanel pnl = new JPanel();
        pnl.setOpaque(false);
        pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS));

        JLabel l1 = new JLabel(line1, SwingConstants.CENTER);
        l1.setFont(NeoBrutalTheme.body(Font.PLAIN, 10));
        l1.setForeground(NeoBrutalTheme.INK);
        l1.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel l2 = new JLabel(line2 == null ? " " : line2, SwingConstants.CENTER);
        l2.setFont(NeoBrutalTheme.body(Font.PLAIN, 10));
        l2.setForeground(NeoBrutalTheme.INK);
        l2.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel lblNama = new JLabel("( " + safe(name) + " )", SwingConstants.CENTER);
        lblNama.setFont(NeoBrutalTheme.body(Font.BOLD, 10));
        lblNama.setForeground(NeoBrutalTheme.INK);
        lblNama.setAlignmentX(Component.CENTER_ALIGNMENT);

        pnl.add(l1);
        pnl.add(l2);
        pnl.add(Box.createVerticalStrut(44));
        pnl.add(lblNama);
        return pnl;
    }

    private JPanel rule(int height) {
        JPanel line = new JPanel();
        line.setBackground(NeoBrutalTheme.INK);
        line.setPreferredSize(new Dimension(0, height));
        line.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
        return line;
    }

    private JPanel buildActions() {
        JPanel pnl = new JPanel(new BorderLayout(12, 0));
        pnl.setOpaque(false);

        JLabel hint = new JLabel("Dokumen dicetak dari data faktur tersimpan.");
        hint.setFont(NeoBrutalTheme.body(Font.PLAIN, 10));
        hint.setForeground(NeoBrutalTheme.TEXT_MUTED);
        pnl.add(hint, BorderLayout.WEST);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setOpaque(false);

        JButton btnCetak = NeoFormStyle.button("Cetak",
                FontIcon.of(MaterialDesignP.PRINTER, 16, NeoBrutalTheme.INK),
                NeoBrutalTheme.PRIMARY);
        btnCetak.setName("btnCetakFaktur");
        btnCetak.addActionListener(e -> doCetak());

        JButton btnPdf = NeoFormStyle.button("Export PDF",
                FontIcon.of(MaterialDesignF.FILE_DOWNLOAD_OUTLINE, 16, NeoBrutalTheme.INK),
                NeoBrutalTheme.SUCCESS);
        btnPdf.setName("btnExportPdfFaktur");
        btnPdf.addActionListener(e -> doExportPdf());

        JButton btnTutup = NeoFormStyle.button("Tutup", null, NeoBrutalTheme.SECONDARY);
        btnTutup.setName("btnTutupFaktur");
        btnTutup.addActionListener(e -> dispose());

        buttons.add(btnCetak);
        buttons.add(btnPdf);
        buttons.add(btnTutup);
        pnl.add(buttons, BorderLayout.EAST);
        return pnl;
    }

    // =========================================================================
    // AKSI CETAK / PDF
    // =========================================================================

    private void doCetak() {
        try {
            ReportHelper.print(TEMPLATE, items, reportParams);
        } catch (JRException ex) {
            JOptionPane.showMessageDialog(this,
                    "Gagal mencetak faktur: " + ex.getMessage(),
                    "Cetak Faktur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doExportPdf() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Export Faktur Pembelian ke PDF");
        fc.setFileFilter(new FileNameExtensionFilter("PDF Documents (*.pdf)", "pdf"));
        String no = safe(pembelian != null ? pembelian.getNoFaktur() : "faktur");
        fc.setSelectedFile(new File("Faktur_"
                + no.replaceAll("[^A-Za-z0-9_-]", "_") + ".pdf"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File out = fc.getSelectedFile();
        if (!out.getName().toLowerCase(Locale.ROOT).endsWith(".pdf")) {
            out = new File(out.getParentFile(), out.getName() + ".pdf");
        }
        try {
            ReportHelper.exportPdf(TEMPLATE, items, reportParams, out);
            JOptionPane.showMessageDialog(this,
                    "Faktur berhasil diekspor:\n" + out.getAbsolutePath(),
                    "Export PDF", JOptionPane.INFORMATION_MESSAGE);
        } catch (JRException ex) {
            JOptionPane.showMessageDialog(this,
                    "Gagal export PDF: " + ex.getMessage(),
                    "Export PDF", JOptionPane.ERROR_MESSAGE);
        }
    }

    // =========================================================================
    // HELPER STATIC (headless-safe, dipakai unit test)
    // =========================================================================

    static String safe(String value) {
        return (value == null || value.isBlank()) ? "-" : value.trim();
    }

    static String formatTanggal(LocalDateTime tgl) {
        return tgl == null ? "-" : tgl.format(FMT_TGL);
    }

    static String formatRp(double value) {
        return "Rp " + FMT_RP.format((long) Math.round(value));
    }

    static Object[][] buildItemRows(List<DetailPembelian> items) {
        if (items == null) {
            return new Object[0][0];
        }
        Object[][] rows = new Object[items.size()][6];
        for (int i = 0; i < items.size(); i++) {
            DetailPembelian d = items.get(i);
            rows[i] = new Object[]{
                i + 1,
                safe(d.getKodeBuku()),
                safe(d.getJudul()),
                d.getQty(),
                formatRp(d.getHargaBeli()),
                formatRp(d.getSubtotal())
            };
        }
        return rows;
    }

    static Map<String, Object> buildReportParams(Pembelian h, Supplier s,
                                                 List<DetailPembelian> items,
                                                 String penerima) {
        Map<String, Object> p = new HashMap<>();
        double total = (h != null) ? h.getTotal() : 0.0;
        int totalQty = 0;
        int totalJenis = (items != null) ? items.size() : 0;
        if (items != null) {
            double sum = 0.0;
            for (DetailPembelian d : items) {
                totalQty += d.getQty();
                sum += d.getSubtotal();
            }
            if (h == null || total <= 0.0) {
                total = sum;
            }
        }
        p.put("NO_FAKTUR", safe(h != null ? h.getNoFaktur() : null));
        p.put("TANGGAL", formatTanggal(h != null ? h.getTanggal() : null));
        p.put("SUPPLIER", safe(s != null ? s.getNamaSupplier()
                : (h != null ? h.getNamaSupplier() : null)));
        p.put("ALAMAT_SUPPLIER", safe(s != null ? s.getAlamat() : null));
        p.put("TELP_SUPPLIER", safe(s != null ? s.getNoTelp() : null));
        p.put("PENERIMA", safe(penerima));
        p.put("TOTAL", total);
        p.put("TOTAL_QTY", totalQty);
        p.put("TOTAL_JENIS", totalJenis);
        p.put("KOTA", "Bekasi");
        p.put("TGL_CETAK", LocalDate.now().format(FMT_TGL_CETAK));
        p.put("PETUGAS", safe(penerima));
        p.put("ALAMAT_TOKO", AppConfig.ALAMAT_TOKO);
        p.put("TELP_TOKO", AppConfig.NO_TELP_TOKO);
        return p;
    }

    private int totalQty() {
        int total = 0;
        for (DetailPembelian d : items) {
            total += d.getQty();
        }
        return total;
    }

    private double totalBiaya() {
        double total = 0.0;
        for (DetailPembelian d : items) {
            total += d.getSubtotal();
        }
        return total;
    }
}
