package view;

import dao.BukuDAO;
import dao.BukuDAOImpl;
import dao.PenjualanDAO;
import dao.PenjualanDAOImpl;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import model.LapGrafik;
import model.LapStok;
import model.LapTerlaris;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import util.NeoBrutalTheme;
import util.NeoShadowBorder;

public class FormDashboard extends JPanel {

    private final PenjualanDAO penjualanDAO = new PenjualanDAOImpl();
    private final BukuDAO bukuDAO = new BukuDAOImpl();

    private static final NumberFormat FMT_RP;

    static {
        FMT_RP = NumberFormat.getInstance(new Locale("id", "ID"));
        FMT_RP.setMaximumFractionDigits(0);
    }

    public FormDashboard() {
        setLayout(new BorderLayout(12, 12));
        setBackground(NeoBrutalTheme.BG);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JLabel lblJudul = new JLabel("Dashboard");
        lblJudul.setFont(new Font("Segoe UI Black", Font.BOLD, 22));
        add(lblJudul, BorderLayout.NORTH);

        LocalDate today = LocalDate.now();
        double omzetHariIni = 0;
        for (LapGrafik g : penjualanDAO.omzetPerHari(today, today)) {
            omzetHariIni += g.getNilai();
        }
        int transaksiHariIni = penjualanDAO.lapPenjualan(today, today).size();
        int itemHariIni = penjualanDAO.itemTerjual(today, today);
        List<LapStok> menipis = bukuDAO.getStokMenipis();

        JPanel kartu = new JPanel(new GridLayout(1, 4, 12, 0));
        kartu.setBackground(NeoBrutalTheme.BG);
        kartu.add(kartuAngka("Rp " + FMT_RP.format(omzetHariIni), "Omzet Hari Ini", "dash_omzet"));
        kartu.add(kartuAngka(String.valueOf(transaksiHariIni), "Transaksi Hari Ini", "dash_transaksi"));
        kartu.add(kartuAngka(String.valueOf(itemHariIni), "Item Terjual Hari Ini", "dash_item"));
        kartu.add(kartuAngka(String.valueOf(menipis.size()), "Stok Menipis", "dash_stok"));
        add(kartu, BorderLayout.CENTER);

        JPanel bawah = new JPanel(new BorderLayout(12, 12));
        bawah.setBackground(NeoBrutalTheme.BG);

        JPanel charts = new JPanel(new GridLayout(1, 2, 12, 0));
        charts.setBackground(NeoBrutalTheme.BG);
        charts.add(buildOmzetChart(today));
        charts.add(buildTerlarisChart(today));
        bawah.add(charts, BorderLayout.CENTER);
        bawah.add(buildStokPanel(menipis), BorderLayout.SOUTH);

        add(bawah, BorderLayout.SOUTH);
    }

    private JPanel kartuAngka(String angka, String label, String name) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setName(name);
        p.setBackground(NeoBrutalTheme.SURFACE);
        p.setBorder(new NeoShadowBorder());
        JLabel lblAngka = new JLabel(angka, SwingConstants.CENTER);
        lblAngka.setFont(new Font("Segoe UI Black", Font.BOLD, 24));
        JLabel lblLabel = new JLabel(label, SwingConstants.CENTER);
        lblLabel.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 12));
        p.add(lblAngka, BorderLayout.CENTER);
        p.add(lblLabel, BorderLayout.SOUTH);
        return p;
    }

    private JPanel buildOmzetChart(LocalDate today) {
        List<LapGrafik> data = penjualanDAO.omzetPerHari(today.minusDays(6), today);
        DefaultCategoryDataset ds = new DefaultCategoryDataset();
        for (LapGrafik g : data) {
            ds.addValue(g.getNilai(), "Omzet", g.getLabel());
        }
        JFreeChart chart = ChartFactory.createBarChart(
                "Omzet 7 Hari Terakhir", "Tanggal", "Rp", ds,
                PlotOrientation.VERTICAL, false, true, false);
        ChartPanel cp = new ChartPanel(chart);
        cp.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(NeoBrutalTheme.SURFACE);
        wrap.setBorder(new NeoShadowBorder());
        wrap.add(cp, BorderLayout.CENTER);
        return wrap;
    }

    private JPanel buildTerlarisChart(LocalDate today) {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(NeoBrutalTheme.SURFACE);
        wrap.setBorder(new NeoShadowBorder());
        List<LapTerlaris> list = penjualanDAO.lapTerlaris(today.withDayOfMonth(1), today);
        if (list.isEmpty()) {
            JLabel info = new JLabel("Belum ada penjualan bulan ini", SwingConstants.CENTER);
            info.setFont(new Font("Segoe UI Semibold", Font.BOLD, 14));
            wrap.add(info, BorderLayout.CENTER);
            return wrap;
        }
        DefaultCategoryDataset ds = new DefaultCategoryDataset();
        int n = Math.min(5, list.size());
        for (int i = 0; i < n; i++) {
            LapTerlaris t = list.get(i);
            ds.addValue(t.getTotalQty(), "Qty", potongJudul(t.getJudul()));
        }
        JFreeChart chart = ChartFactory.createBarChart(
                "Top 5 Buku Bulan Ini", "Buku", "Qty", ds,
                PlotOrientation.VERTICAL, false, true, false);
        ChartPanel cp = new ChartPanel(chart);
        cp.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        wrap.add(cp, BorderLayout.CENTER);
        return wrap;
    }

    private String potongJudul(String judul) {
        if (judul == null) {
            return "-";
        }
        if (judul.length() <= 18) {
            return judul;
        }
        return judul.substring(0, 17) + "\u2026";
    }

    private JPanel buildStokPanel(List<LapStok> menipis) {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setBackground(NeoBrutalTheme.BG);
        JLabel lbl = new JLabel("Stok Menipis");
        lbl.setFont(new Font("Segoe UI Black", Font.BOLD, 16));
        p.add(lbl, BorderLayout.NORTH);

        DefaultTableModel model = new DefaultTableModel(new String[]{"Kode", "Judul", "Stok"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        for (LapStok s : menipis) {
            model.addRow(new Object[]{s.getKodeBuku(), s.getJudul(), s.getStok()});
        }
        JTable tbl = new JTable(model);
        tbl.setName("dash_stok_table");
        tbl.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    JOptionPane.showMessageDialog(FormDashboard.this,
                            "Buka Form Buku untuk restock",
                            "Stok Menipis", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });
        JScrollPane scroll = new JScrollPane(tbl);
        scroll.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        scroll.setPreferredSize(new java.awt.Dimension(0, 160));
        p.add(scroll, BorderLayout.CENTER);
        return p;
    }
}
