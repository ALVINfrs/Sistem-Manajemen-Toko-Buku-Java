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
import model.DetailPenjualan;
import model.Penjualan;
import org.kordamp.ikonli.materialdesign2.MaterialDesignP;
import org.kordamp.ikonli.swing.FontIcon;
import util.AppConfig;
import util.NeoBrutalTheme;
import util.NeoShadowBorder;

public class StrukDialog extends JDialog {

    private static final int LINE_WIDTH = 42;
    private static final NumberFormat FMT_RP = NumberFormat.getInstance(new Locale("id", "ID"));
    private static final DateTimeFormatter FMT_TGL = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    private final JTextArea txtStruk;

    public StrukDialog(Frame owner, Penjualan h, List<DetailPenjualan> items,
                       String kasir, String member, double diskonMember, double potongan) {
        super(owner, "Struk Penjualan - " + (h != null ? h.getNoNota() : AppConfig.APP_NAME), true);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(NeoBrutalTheme.BG);
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        String receiptContent = generateReceiptText(h, items, kasir, member, diskonMember, potongan);

        txtStruk = new JTextArea(receiptContent);
        txtStruk.setFont(getReceiptFont());
        txtStruk.setEditable(false);
        txtStruk.setBackground(Color.WHITE);
        txtStruk.setForeground(Color.BLACK);
        txtStruk.setCaretPosition(0);
        txtStruk.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        // Panel kertas struk dengan shadow khas neobrutalism
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

        setSize(460, 620);
        setLocationRelativeTo(owner);
    }

    private Font getReceiptFont() {
        Font f = new Font("Consolas", Font.PLAIN, 12);
        if (!"Consolas".equalsIgnoreCase(f.getFamily())) {
            f = new Font(Font.MONOSPACED, Font.PLAIN, 12);
        }
        return f;
    }

    private String generateReceiptText(Penjualan h, List<DetailPenjualan> items,
                                      String kasir, String member,
                                      double diskonMember, double potongan) {
        StringBuilder sb = new StringBuilder();
        String eqLine = repeat('=', LINE_WIDTH);
        String dashLine = repeat('-', LINE_WIDTH);

        // Header Toko
        sb.append(eqLine).append("\n");
        sb.append(center(AppConfig.APP_NAME.toUpperCase())).append("\n");
        sb.append(center("TOKO BUKU & ALAT TULIS")).append("\n");
        sb.append(center("Jl. Merdeka No. 45 - Telp: 0812-3456-7890")).append("\n");
        sb.append(eqLine).append("\n");

        // Info Transaksi
        LocalDateTime tgl = (h != null && h.getTanggal() != null) ? h.getTanggal() : LocalDateTime.now();
        String strTanggal = tgl.format(FMT_TGL);
        String strNota = (h != null && h.getNoNota() != null) ? h.getNoNota() : "-";
        String strKasir = (kasir != null && !kasir.isBlank()) ? kasir : "-";
        String strMember = (member != null && !member.isBlank() && !"-".equals(member)) ? member : "- (Non-Member)";

        sb.append(row("No. Nota  : " + strNota, "")).append("\n");
        sb.append(row("Tanggal   : " + strTanggal, "")).append("\n");
        sb.append(row("Kasir     : " + strKasir, "")).append("\n");
        sb.append(row("Pelanggan : " + strMember, "")).append("\n");
        sb.append(dashLine).append("\n");

        // Daftar Item
        int totalQty = 0;
        int totalItem = 0;
        if (items != null) {
            totalItem = items.size();
            for (DetailPenjualan d : items) {
                String judul = (d.getJudul() != null && !d.getJudul().isBlank())
                        ? d.getJudul() : d.getKodeBuku();
                if (judul == null || judul.isBlank()) {
                    judul = "Item Buku";
                }
                appendWrappedText(sb, judul, LINE_WIDTH);

                String leftCol = "  " + d.getQty() + " x " + formatAngka(d.getHargaJual());
                String rightCol = formatAngka(d.getSubtotal());
                sb.append(row(leftCol, rightCol)).append("\n");

                totalQty += d.getQty();
            }
        }
        sb.append(dashLine).append("\n");

        // Ringkasan Pembayaran
        double total = (h != null) ? h.getTotal() : 0.0;
        double diskon = (h != null) ? h.getDiskon() : 0.0;
        double subtotal = total + diskon;
        double bayar = (h != null) ? h.getBayar() : 0.0;
        double kembalian = (h != null) ? h.getKembalian() : 0.0;
        String metode = (h != null && h.getMetodeBayar() != null && !h.getMetodeBayar().isBlank())
                ? h.getMetodeBayar().toUpperCase() : "TUNAI";

        sb.append(row("Total Item : " + totalItem + " (" + totalQty + " pcs)", "")).append("\n");
        sb.append(row("Subtotal Belanja :", formatRp(subtotal))).append("\n");

        if (diskonMember > 0) {
            sb.append(row("Diskon Member (" + AppConfig.DISKON_MEMBER_PCT + "%) :", "-" + formatRp(diskonMember))).append("\n");
        }
        if (potongan > 0) {
            sb.append(row("Potongan Promo :", "-" + formatRp(potongan))).append("\n");
        }

        sb.append(dashLine).append("\n");
        sb.append(row("TOTAL AKHIR :", formatRp(total))).append("\n");
        sb.append(row("METODE BAYAR :", metode)).append("\n");
        sb.append(row("BAYAR :", formatRp(bayar))).append("\n");
        sb.append(row("KEMBALIAN :", formatRp(kembalian))).append("\n");
        sb.append(eqLine).append("\n");

        // Footer Struk
        sb.append(center("PPN 11% SUDAH TERMASUK DALAM HARGA")).append("\n");
        sb.append(center("TERIMA KASIH TELAH BERBELANJA")).append("\n");
        sb.append(center("Barang yang sudah dibeli tidak dapat")).append("\n");
        sb.append(center("ditukar/dikembalikan tanpa struk")).append("\n");
        sb.append(center("Layanan Konsumen SMS/WA: 0812-3456-7890")).append("\n");
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
                JOptionPane.showMessageDialog(this, "Struk berhasil dikirim ke antrean cetak!",
                        "Cetak Struk", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (PrinterException ex) {
            JOptionPane.showMessageDialog(this, "Gagal mencetak struk: " + ex.getMessage(),
                    "Cetak Struk", JOptionPane.ERROR_MESSAGE);
        }
    }
}
