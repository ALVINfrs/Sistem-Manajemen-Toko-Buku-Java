package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import model.DetailPenjualan;
import model.Penjualan;
import util.AppConfig;
import util.NeoBrutalTheme;
import util.NeoShadowBorder;

public class StrukDialog extends JDialog {

    public StrukDialog(Frame owner, Penjualan h, List<DetailPenjualan> items, String kasir, String member) {
        super(owner, AppConfig.APP_NAME, true);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(NeoBrutalTheme.BG);
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        String tanggal = h.getTanggal() != null ? h.getTanggal().format(fmt) : "-";

        JPanel pnlInfo = new JPanel(new GridLayout(0, 1, 2, 2));
        pnlInfo.setBackground(NeoBrutalTheme.SURFACE);
        pnlInfo.setBorder(new NeoShadowBorder());
        pnlInfo.add(infoLabel("No Nota : " + h.getNoNota()));
        pnlInfo.add(infoLabel("Tanggal : " + tanggal));
        pnlInfo.add(infoLabel("Kasir   : " + (kasir != null ? kasir : "-")));
        pnlInfo.add(infoLabel("Member  : " + (member != null ? member : "-")));
        add(pnlInfo, BorderLayout.NORTH);

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
        add(scroll, BorderLayout.CENTER);

        JPanel pnlTotal = new JPanel(new GridLayout(3, 1, 4, 4));
        pnlTotal.setBackground(NeoBrutalTheme.BG);
        pnlTotal.add(besarLabel("Total     : " + h.getTotal()));
        pnlTotal.add(besarLabel("Bayar     : " + h.getBayar()));
        pnlTotal.add(besarLabel("Kembalian : " + h.getKembalian()));

        JButton btnTutup = new JButton("Tutup");
        btnTutup.setFont(new Font("Segoe UI Semibold", Font.BOLD, 13));
        btnTutup.setBackground(NeoBrutalTheme.SECONDARY);
        btnTutup.setForeground(Color.BLACK);
        btnTutup.setBorder(new NeoShadowBorder());
        btnTutup.setFocusPainted(false);
        btnTutup.addActionListener(e -> dispose());
        JPanel pnlSouth = new JPanel(new BorderLayout(8, 8));
        pnlSouth.setBackground(NeoBrutalTheme.BG);
        pnlSouth.add(pnlTotal, BorderLayout.CENTER);
        JPanel pnlBtn = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlBtn.setBackground(NeoBrutalTheme.BG);
        pnlBtn.add(btnTutup);
        pnlSouth.add(pnlBtn, BorderLayout.SOUTH);
        add(pnlSouth, BorderLayout.SOUTH);

        setSize(480, 520);
        setLocationRelativeTo(owner);
    }

    private JLabel infoLabel(String teks) {
        JLabel l = new JLabel(teks);
        l.setFont(new Font("Segoe UI Semibold", Font.BOLD, 13));
        return l;
    }

    private JLabel besarLabel(String teks) {
        JLabel l = new JLabel(teks);
        l.setFont(new Font("Segoe UI Black", Font.BOLD, 18));
        l.setForeground(NeoBrutalTheme.PRIMARY);
        return l;
    }
}
