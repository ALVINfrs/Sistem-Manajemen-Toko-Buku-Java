package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.print.PrinterException;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import model.DetailPembelian;
import model.Pembelian;
import org.kordamp.ikonli.materialdesign2.MaterialDesignP;
import org.kordamp.ikonli.swing.FontIcon;
import util.AppConfig;
import util.NeoBrutalTheme;
import util.NeoShadowBorder;

public class FakturPembelianDialog extends JDialog {

    private static final int LINE_WIDTH = 44;
    private static final NumberFormat FMT_RP = NumberFormat.getInstance(new Locale("id", "ID"));
    private static final DateTimeFormatter FMT_TGL = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    private final JTextArea txtStruk;

    public FakturPembelianDialog(Frame owner, Pembelian h, List<DetailPembelian> items,
                                 String supplierName, String supplierAlamat, String supplierTelp,
                                 String adminUser) {
        super(owner, "Faktur Masuk - " + (h != null ? h.getNoFaktur() : AppConfig.APP_NAME), true);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(NeoBrutalTheme.BG);
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        String receiptContent = generateReceiptText(h, items, supplierName, supplierAlamat, supplierTelp, adminUser);

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

        JButton btnCetak = new JButton("Cetak Bukti",
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
        pnlBtn.add(btnTutup);
        add(pnlBtn, BorderLayout.SOUTH);

        setSize(480, 640);
        setLocationRelativeTo(owner);
    }

    private Font getReceiptFont() {
        Font f = new Font("Consolas", Font.PLAIN, 12);
        if (!"Consolas".equalsIgnoreCase(f.getFamily())) {
            f = new Font(Font.MONOSPACED, Font.PLAIN, 12);
        }
        return f;
    }

    private String generateReceiptText(Pembelian h, List<DetailPembelian> items,
                                       String supplierName, String supplierAlamat, String supplierTelp,
                                       String adminUser) {
        StringBuilder sb = new StringBuilder();
        String eqLine = repeat('=', LINE_WIDTH);
        String dashLine = repeat('-', LINE_WIDTH);

        // Header Toko
        sb.append(eqLine).append("\n");
        sb.append(center(AppConfig.APP_NAME.toUpperCase())).append("\n");
        sb.append(center("BUKTI PENERIMAAN BARANG (FAKTUR BELI)")).append("\n");
        sb.append(eqLine).append("\n");

        // Info Transaksi
        LocalDateTime tgl = (h != null && h.getTanggal() != null) ? h.getTanggal() : LocalDateTime.now();
        String strTanggal = tgl.format(FMT_TGL);
        String strFaktur = (h != null && h.getNoFaktur() != null) ? h.getNoFaktur() : "-";
        String strPenerima = (adminUser != null && !adminUser.isBlank()) ? adminUser : "-";
        String strSupplier = (supplierName != null && !supplierName.isBlank()) ? supplierName : "-";
        String strAlamat = (supplierAlamat != null && !supplierAlamat.isBlank()) ? supplierAlamat : "-";
        String strTelp = (supplierTelp != null && !supplierTelp.isBlank()) ? supplierTelp : "-";

        sb.append(row("No. Faktur : " + strFaktur, "")).append("\n");
        sb.append(row("Tanggal    : " + strTanggal, "")).append("\n");
        sb.append(row("Penerima   : " + strPenerima, "")).append("\n");
        sb.append(dashLine).append("\n");
        sb.append("Supplier   : ").append(strSupplier).append("\n");
        sb.append("Alamat     : ").append(strAlamat).append("\n");
        sb.append("No. Telp   : ").append(strTelp).append("\n");
        sb.append(dashLine).append("\n");

        // Daftar Item
        int totalQty = 0;
        int totalItem = 0;
        if (items != null) {
            totalItem = items.size();
            for (DetailPembelian d : items) {
                String judul = (d.getJudul() != null && !d.getJudul().isBlank())
                        ? d.getJudul() : d.getKodeBuku();
                if (judul == null || judul.isBlank()) {
                    judul = "Item Buku";
                }
                appendWrappedText(sb, "[" + d.getKodeBuku() + "] " + judul, LINE_WIDTH);

                String leftCol = "  " + d.getQty() + " pcs x " + formatAngka(d.getHargaBeli());
                String rightCol = formatAngka(d.getSubtotal());
                sb.append(row(leftCol, rightCol)).append("\n");

                totalQty += d.getQty();
            }
        }
        sb.append(dashLine).append("\n");

        // Ringkasan Pembelian
        double total = (h != null) ? h.getTotal() : 0.0;
        sb.append(row("Total Jenis Buku :", totalItem + " Judul")).append("\n");
        sb.append(row("Total Qty Masuk  :", totalQty + " pcs")).append("\n");
        sb.append(dashLine).append("\n");
        sb.append(row("TOTAL BIAYA BELI :", formatRp(total))).append("\n");
        sb.append(eqLine).append("\n");

        // Tanda Tangan
        sb.append("\n");
        sb.append(center("Petugas Gudang / Penerima")).append("\n");
        sb.append("\n\n");
        sb.append(center("( " + strPenerima + " )")).append("\n");
        sb.append(dashLine).append("\n");
        sb.append(center("Barang telah diterima & stok bertambah")).append("\n");
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

    private String formatAngka(double val) {
        return FMT_RP.format((long) Math.round(val));
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
                JOptionPane.showMessageDialog(this, "Faktur masuk berhasil dikirim ke printer!",
                        "Cetak Faktur", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (PrinterException ex) {
            JOptionPane.showMessageDialog(this, "Gagal mencetak faktur: " + ex.getMessage(),
                    "Cetak Faktur", JOptionPane.ERROR_MESSAGE);
        }
    }
}
