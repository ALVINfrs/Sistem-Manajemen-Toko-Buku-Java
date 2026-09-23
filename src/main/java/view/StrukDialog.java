package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import model.DetailPenjualan;
import model.Penjualan;
import org.kordamp.ikonli.materialdesign2.MaterialDesignP;
import org.kordamp.ikonli.swing.FontIcon;
import util.AppConfig;
import util.NeoBrutalTheme;
import util.NeoShadowBorder;

public class StrukDialog extends JDialog {

    private final JPanel pnlStruk;

    public StrukDialog(Frame owner, Penjualan h, List<DetailPenjualan> items,
                       String kasir, String member, double diskonMember, double potongan) {
        super(owner, AppConfig.APP_NAME, true);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(NeoBrutalTheme.BG);
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        String tanggal = h.getTanggal() != null ? h.getTanggal().format(fmt) : "-";
        double subtotal = h.getTotal() + h.getDiskon();

        pnlStruk = new JPanel(new BorderLayout(8, 8));
        pnlStruk.setBackground(NeoBrutalTheme.SURFACE);
        pnlStruk.setBorder(new NeoShadowBorder());

        JPanel pnlInfo = new JPanel(new GridLayout(0, 1, 2, 2));
        pnlInfo.setBackground(NeoBrutalTheme.SURFACE);
        pnlInfo.add(infoLabel(AppConfig.APP_NAME, true));
        pnlInfo.add(infoLabel("No Nota : " + h.getNoNota()));
        pnlInfo.add(infoLabel("Tanggal : " + tanggal));
        pnlInfo.add(infoLabel("Kasir   : " + (kasir != null ? kasir : "-")));
        pnlInfo.add(infoLabel("Member  : " + (member != null ? member : "-")));
        pnlStruk.add(pnlInfo, BorderLayout.NORTH);

        DefaultTableModel model = new DefaultTableModel(
                new String[]{"Judul", "Qty", "Harga", "Subtotal"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        if (items != null) {
            for (DetailPenjualan d : items) {
                model.addRow(new Object[]{
                        d.getJudul() != null ? d.getJudul() : d.getKodeBuku(),
                        d.getQty(),
                        d.getHargaJual(),
                        d.getSubtotal()
                });
            }
        }
        JTable table = new JTable(model);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        pnlStruk.add(scroll, BorderLayout.CENTER);

        JPanel pnlTotal = new JPanel(new GridLayout(0, 1, 4, 4));
        pnlTotal.setBackground(NeoBrutalTheme.SURFACE);
        pnlTotal.add(besarLabel("Subtotal    : " + subtotal));
        pnlTotal.add(besarLabel("Diskon member : " + diskonMember));
        pnlTotal.add(besarLabel("Potongan    : " + potongan));
        pnlTotal.add(besarLabel("Total     : " + h.getTotal()));
        pnlTotal.add(besarLabel("Bayar     : " + h.getBayar()));
        pnlTotal.add(besarLabel("Kembalian : " + h.getKembalian()));
        pnlTotal.add(besarLabel("Metode    : " + (h.getMetodeBayar() != null ? h.getMetodeBayar() : "-")));
        pnlTotal.add(infoLabel("Terima kasih sudah berbelanja"));
        pnlStruk.add(pnlTotal, BorderLayout.SOUTH);
        add(pnlStruk, BorderLayout.CENTER);

        JButton btnCetak = new JButton("Cetak",
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
        JPanel pnlBtn = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlBtn.setBackground(NeoBrutalTheme.BG);
        pnlBtn.add(btnCetak);
        pnlBtn.add(btnTutup);
        add(pnlBtn, BorderLayout.SOUTH);

        setSize(480, 560);
        setLocationRelativeTo(owner);
    }

    private void doCetak() {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setJobName("Struk " + AppConfig.APP_NAME);
        job.setPrintable((g, pf, idx) -> {
            if (idx > 0) return java.awt.print.Printable.NO_SUCH_PAGE;
            pnlStruk.print(g);
            return java.awt.print.Printable.PAGE_EXISTS;
        });
        if (!job.printDialog()) return;
        try {
            job.print();
        } catch (PrinterException ex) {
            JOptionPane.showMessageDialog(this, "Gagal mencetak: " + ex.getMessage(),
                    "Cetak", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JLabel infoLabel(String teks) {
        return infoLabel(teks, false);
    }

    private JLabel infoLabel(String teks, boolean judul) {
        JLabel l = new JLabel(teks);
        l.setFont(new Font(judul ? "Segoe UI Black" : "Segoe UI Semibold", Font.BOLD, judul ? 16 : 13));
        return l;
    }

    private JLabel besarLabel(String teks) {
        JLabel l = new JLabel(teks);
        l.setFont(new Font("Segoe UI Black", Font.BOLD, 18));
        l.setForeground(NeoBrutalTheme.PRIMARY);
        return l;
    }
}
