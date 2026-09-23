package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileNameExtensionFilter;
import model.Retur;
import org.kordamp.ikonli.materialdesign2.MaterialDesignC;
import org.kordamp.ikonli.materialdesign2.MaterialDesignF;
import org.kordamp.ikonli.materialdesign2.MaterialDesignP;
import org.kordamp.ikonli.swing.FontIcon;
import util.AppConfig;
import util.NeoBrutalTheme;
import util.NeoShadowBorder;

public class StrukReturDialog extends JDialog {

    private static final int LINE_WIDTH = 42;
    private static final NumberFormat FMT_RP = NumberFormat.getInstance(new Locale("id", "ID"));
    private static final DateTimeFormatter FMT_TGL = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    private final JTextArea txtStruk;
    private final String receiptContent;

    public StrukReturDialog(Frame owner, Retur r, double hargaSatuan, String petugas, String pelanggan) {
        super(owner, "Bukti Retur - " + (r != null ? r.getNoRetur() : AppConfig.APP_NAME), true);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(NeoBrutalTheme.BG);
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        double harga = hargaSatuan > 0 ? hargaSatuan : (r != null ? r.getHargaJual() : 0.0);
        receiptContent = generateReceiptText(r, harga, petugas, pelanggan);

        txtStruk = new JTextArea(receiptContent);
        txtStruk.setFont(getReceiptFont());
        txtStruk.setEditable(false);
        txtStruk.setBackground(Color.WHITE);
        txtStruk.setForeground(Color.BLACK);
        txtStruk.setCaretPosition(0);
        txtStruk.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel pnlKertasStruk = new JPanel(new BorderLayout());
        pnlKertasStruk.setBackground(Color.WHITE);
        pnlKertasStruk.setBorder(new NeoShadowBorder());
        pnlKertasStruk.add(txtStruk, BorderLayout.CENTER);

        JPanel pnlWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        pnlWrapper.setOpaque(false);
        pnlWrapper.add(pnlKertasStruk);

        JScrollPane scroll = new JScrollPane(pnlWrapper);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        JButton btnCetak = new JButton("Cetak Struk",
                FontIcon.of(MaterialDesignP.PRINTER, 18, Color.BLACK));
        btnCetak.setFont(new Font("Segoe UI Semibold", Font.BOLD, 13));
        btnCetak.setBackground(NeoBrutalTheme.PRIMARY);
        btnCetak.setForeground(Color.BLACK);
        btnCetak.setBorder(new NeoShadowBorder());
        btnCetak.setFocusPainted(false);
        btnCetak.addActionListener(e -> doCetak());
        btnCetak.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                btnCetak.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                btnCetak.setBorder(new NeoShadowBorder());
            }
        });

        JButton btnExport = new JButton("Simpan File",
                FontIcon.of(MaterialDesignF.FILE_DOWNLOAD_OUTLINE, 18, Color.BLACK));
        btnExport.setFont(new Font("Segoe UI Semibold", Font.BOLD, 13));
        btnExport.setBackground(new Color(254, 240, 138)); // Light yellow accent
        btnExport.setForeground(Color.BLACK);
        btnExport.setBorder(new NeoShadowBorder());
        btnExport.setFocusPainted(false);
        btnExport.addActionListener(e -> doExportFile(r));
        btnExport.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                btnExport.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                btnExport.setBorder(new NeoShadowBorder());
            }
        });

        JButton btnTutup = new JButton("Tutup");
        btnTutup.setFont(new Font("Segoe UI Semibold", Font.BOLD, 13));
        btnTutup.setBackground(NeoBrutalTheme.SECONDARY);
        btnTutup.setForeground(Color.BLACK);
        btnTutup.setBorder(new NeoShadowBorder());
        btnTutup.setFocusPainted(false);
        btnTutup.addActionListener(e -> dispose());
        btnTutup.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                btnTutup.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                btnTutup.setBorder(new NeoShadowBorder());
            }
        });

        JPanel pnlBtn = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlBtn.setBackground(NeoBrutalTheme.BG);
        pnlBtn.add(btnCetak);
        pnlBtn.add(btnExport);
        pnlBtn.add(btnTutup);
        add(pnlBtn, BorderLayout.SOUTH);

        setSize(460, 620);
        setLocationRelativeTo(owner);
    }

    public StrukReturDialog(Frame owner, Retur r, double hargaSatuan, String petugas) {
        this(owner, r, hargaSatuan, petugas, "Pelanggan Umum");
    }

    private Font getReceiptFont() {
        Font f = new Font("Consolas", Font.PLAIN, 12);
        if (!"Consolas".equalsIgnoreCase(f.getFamily())) {
            f = new Font(Font.MONOSPACED, Font.PLAIN, 12);
        }
        return f;
    }

    private String generateReceiptText(Retur r, double hargaSatuan, String petugas, String pelanggan) {
        StringBuilder sb = new StringBuilder();
        String eqLine = repeat('=', LINE_WIDTH);
        String dashLine = repeat('-', LINE_WIDTH);

        // Header Toko
        sb.append(eqLine).append("\n");
        sb.append(center(AppConfig.APP_NAME.toUpperCase())).append("\n");
        sb.append(center("BUKTI TANDA TERIMA RETUR")).append("\n");
        sb.append(center("Jl. Merdeka No. 45 - Telp: 0812-3456-7890")).append("\n");
        sb.append(eqLine).append("\n");

        // Info Transaksi Retur
        LocalDateTime tgl = (r != null && r.getTanggal() != null) ? r.getTanggal() : LocalDateTime.now();
        String strTanggal = tgl.format(FMT_TGL);
        String strRetur = (r != null && r.getNoRetur() != null) ? r.getNoRetur() : "-";
        String strNota = (r != null && r.getNoNota() != null) ? r.getNoNota() : "-";
        String strPetugas = (petugas != null && !petugas.isBlank()) ? petugas : "Petugas";
        String strPelanggan = (pelanggan != null && !pelanggan.isBlank()) ? pelanggan : "Umum";

        sb.append(row("No. Retur   : " + strRetur, "")).append("\n");
        sb.append(row("Tanggal     : " + strTanggal, "")).append("\n");
        sb.append(row("Petugas     : " + strPetugas, "")).append("\n");
        sb.append(dashLine).append("\n");
        sb.append(row("No. Nota Asli : " + strNota, "")).append("\n");
        sb.append(row("Pelanggan     : " + strPelanggan, "")).append("\n");
        sb.append(dashLine).append("\n");

        // Info Item Retur
        sb.append("Buku yang Diretur:\n");
        String kode = (r != null && r.getKodeBuku() != null) ? r.getKodeBuku() : "-";
        String judul = (r != null && r.getJudul() != null) ? r.getJudul() : "Buku";
        appendWrappedText(sb, "[" + kode + "] " + judul, LINE_WIDTH);

        int qty = (r != null) ? r.getQty() : 0;
        double totalRefund = qty * hargaSatuan;
        String alasan = (r != null && r.getAlasan() != null && !r.getAlasan().isBlank())
                ? r.getAlasan() : "Tidak disebutkan";

        sb.append(row("  Qty Retur   :", qty + " pcs")).append("\n");
        sb.append(row("  Harga Satuan:", formatRp(hargaSatuan))).append("\n");
        sb.append(dashLine).append("\n");
        sb.append("Alasan Retur:\n");
        appendWrappedText(sb, "  " + alasan, LINE_WIDTH);
        sb.append(dashLine).append("\n");

        // Total Refund
        sb.append(center("TOTAL REFUND (UANG KEMBALI)")).append("\n");
        sb.append(center(formatRp(totalRefund))).append("\n");
        sb.append(eqLine).append("\n");

        // Keterangan & Konfirmasi
        sb.append(center("Barang telah diterima kembali &")).append("\n");
        sb.append(center("stok otomatis diperbarui di gudang.")).append("\n");
        sb.append(center("Dana telah dikembalikan kepada pelanggan.")).append("\n");
        sb.append(dashLine).append("\n");
        sb.append(center("Simpan struk ini sebagai bukti sah.")).append("\n");
        sb.append(eqLine).append("\n");

        return sb.toString();
    }

    private void appendWrappedText(StringBuilder sb, String text, int maxWidth) {
        if (text == null) return;
        while (text.length() > maxWidth) {
            int spaceIdx = text.lastIndexOf(' ', maxWidth);
            if (spaceIdx <= 0) spaceIdx = maxWidth;
            sb.append(text, 0, spaceIdx).append("\n");
            text = text.substring(spaceIdx).trim();
        }
        if (!text.isEmpty()) {
            sb.append(text).append("\n");
        }
    }

    private String formatRp(double val) {
        return "Rp " + FMT_RP.format((long) Math.round(val));
    }

    private String center(String text) {
        if (text == null) text = "";
        if (text.length() >= LINE_WIDTH) return text;
        int pad = (LINE_WIDTH - text.length()) / 2;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < pad; i++) sb.append(' ');
        sb.append(text);
        return sb.toString();
    }

    private String repeat(char c, int n) {
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) sb.append(c);
        return sb.toString();
    }

    private String row(String left, String right) {
        int space = LINE_WIDTH - left.length() - right.length();
        if (space < 1) space = 1;
        StringBuilder sb = new StringBuilder();
        sb.append(left);
        for (int i = 0; i < space; i++) sb.append(' ');
        sb.append(right);
        return sb.toString();
    }

    private void doCetak() {
        try {
            boolean complete = txtStruk.print(null, null, true, null, null, true);
            if (complete) {
                JOptionPane.showMessageDialog(this, "Bukti retur berhasil dikirim ke antrean cetak!",
                        "Cetak Struk", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (PrinterException ex) {
            JOptionPane.showMessageDialog(this, "Gagal mencetak struk: " + ex.getMessage(),
                    "Cetak Struk", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doExportFile(Retur r) {
        String defaultName = (r != null && r.getNoRetur() != null)
                ? "Bukti_Retur_" + r.getNoRetur() + ".txt"
                : "Bukti_Retur.txt";

        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Simpan Bukti Retur");
        fc.setSelectedFile(new File(defaultName));
        fc.setFileFilter(new FileNameExtensionFilter("Text File (*.txt)", "txt"));

        int res = fc.showSaveDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".txt")) {
                file = new File(file.getParentFile(), file.getName() + ".txt");
            }
            try (FileWriter fw = new FileWriter(file, StandardCharsets.UTF_8)) {
                fw.write(receiptContent);
                JOptionPane.showMessageDialog(this,
                        "Bukti retur berhasil disimpan:\n" + file.getAbsolutePath(),
                        "Simpan Berhasil", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                        "Gagal menyimpan file: " + ex.getMessage(),
                        "Error Simpan", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
