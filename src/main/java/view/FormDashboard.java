package view;

import dao.BukuDAO;
import dao.BukuDAOImpl;
import dao.PenjualanDAO;
import dao.PenjualanDAOImpl;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import model.LapGrafik;
import model.LapStok;
import model.LapTerlaris;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.kordamp.ikonli.materialdesign2.MaterialDesignC;
import org.kordamp.ikonli.swing.FontIcon;
import util.NeoBrutalTheme;
import util.NeoChartStyle;
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
        setLayout(new BorderLayout(0, 12));
        setBackground(NeoBrutalTheme.BG);
        setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        add(new NeoPageHeader("RINGKASAN HARI INI", "Dashboard",
                "Pantau omzet, transaksi, dan stok dalam satu layar.", null),
                BorderLayout.NORTH);

        LocalDate today = LocalDate.now();
        double omzetHariIni = 0;
        for (LapGrafik g : penjualanDAO.omzetPerHari(today, today)) {
            omzetHariIni += g.getNilai();
        }
        int transaksiHariIni = penjualanDAO.lapPenjualan(today, today).size();
        int itemHariIni = penjualanDAO.itemTerjual(today, today);
        List<LapStok> menipis = bukuDAO.getStokMenipis();

        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);

        GridBagConstraints row = new GridBagConstraints();
        row.gridx = 0;
        row.weightx = 1;
        row.fill = GridBagConstraints.BOTH;
        row.gridy = 0;
        row.weighty = 0;
        row.insets = new Insets(0, 0, 12, 0);
        JPanel top = buildTopRow(omzetHariIni, transaksiHariIni, itemHariIni,
                menipis.size());
        top.setPreferredSize(new Dimension(0, 150));
        top.setMinimumSize(new Dimension(0, 0));
        body.add(top, row);

        row.gridy = 1;
        row.weighty = 1;
        JPanel charts = buildCharts(today);
        charts.setPreferredSize(new Dimension(0, 175));
        charts.setMinimumSize(new Dimension(0, 0));
        body.add(charts, row);

        row.gridy = 2;
        row.weighty = 0;
        row.insets = new Insets(0, 0, 0, 0);
        JPanel stok = buildStokPanel(menipis);
        stok.setPreferredSize(new Dimension(0, 120));
        stok.setMinimumSize(new Dimension(0, 0));
        body.add(stok, row);

        add(body, BorderLayout.CENTER);
    }

    private JPanel buildTopRow(double omzet, int transaksi, int item, int stok) {
        JPanel top = new JPanel(new GridBagLayout());
        top.setOpaque(false);
        top.setMinimumSize(new Dimension(0, 0));

        GridBagConstraints hero = new GridBagConstraints();
        hero.gridx = 0;
        hero.gridy = 0;
        hero.gridheight = 3;
        hero.weightx = 0.62;
        hero.weighty = 1;
        hero.fill = GridBagConstraints.BOTH;
        hero.insets = new Insets(0, 0, 0, 12);
        JPanel heroPanel = buildHero(omzet);
        heroPanel.setMinimumSize(new Dimension(0, 0));
        top.add(heroPanel, hero);

        GridBagConstraints kpi = new GridBagConstraints();
        kpi.gridx = 1;
        kpi.weightx = 0.38;
        kpi.fill = GridBagConstraints.BOTH;
        kpi.insets = new Insets(0, 0, 4, 0);

        kpi.gridy = 0;
        kpi.weighty = 1.0 / 3.0;
        top.add(kartuKpi(String.valueOf(transaksi), "TRANSAKSI HARI INI",
                "dash_transaksi", NeoBrutalTheme.BRAND_TEAL), kpi);

        kpi.gridy = 1;
        top.add(kartuKpi(String.valueOf(item), "ITEM TERJUAL HARI INI",
                "dash_item", NeoBrutalTheme.WARNING), kpi);

        kpi.gridy = 2;
        kpi.insets = new Insets(0, 0, 0, 0);
        top.add(kartuKpi(String.valueOf(stok), "STOK MENIPIS",
                "dash_stok", NeoBrutalTheme.PRIMARY), kpi);

        return top;
    }

    private JPanel buildHero(double omzet) {
        JPanel hero = new JPanel(new BorderLayout(12, 0));
        hero.setName("dashHero");
        hero.setBackground(NeoBrutalTheme.BRAND_MAROON);
        hero.setBorder(BorderFactory.createCompoundBorder(
                new NeoShadowBorder(),
                BorderFactory.createEmptyBorder(14, 18, 14, 18)));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));

        JLabel kicker = new JLabel("OMZET HARI INI");
        kicker.setFont(NeoBrutalTheme.mono(10));
        kicker.setForeground(NeoBrutalTheme.WARNING);
        kicker.setAlignmentX(LEFT_ALIGNMENT);

        JLabel value = new JLabel("Rp " + FMT_RP.format(omzet));
        value.setFont(NeoBrutalTheme.display(30));
        value.setForeground(NeoBrutalTheme.BG);
        value.setAlignmentX(LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Akumulasi penjualan hari ini");
        sub.setFont(NeoBrutalTheme.body(java.awt.Font.PLAIN, 11));
        sub.setForeground(Color.decode("#F3DFCD"));
        sub.setAlignmentX(LEFT_ALIGNMENT);

        JPanel omzetBox = new JPanel();
        omzetBox.setName("dash_omzet");
        omzetBox.setOpaque(false);
        omzetBox.setLayout(new BoxLayout(omzetBox, BoxLayout.Y_AXIS));
        omzetBox.add(kicker);
        omzetBox.add(Box.createVerticalStrut(4));
        omzetBox.add(value);
        omzetBox.add(Box.createVerticalStrut(3));
        omzetBox.add(sub);

        JLabel icon = new JLabel(FontIcon.of(
                MaterialDesignC.CASH, 48, NeoBrutalTheme.WARNING));
        text.add(omzetBox);
        hero.add(text, BorderLayout.CENTER);
        hero.add(icon, BorderLayout.EAST);
        return hero;
    }

    private JPanel kartuKpi(String angka, String label, String name, Color bg) {
        JPanel card = new JPanel(new BorderLayout(0, 1));
        card.setName(name);
        card.setBackground(bg);
        card.setMinimumSize(new Dimension(0, 0));
        card.setBorder(BorderFactory.createCompoundBorder(
                new NeoShadowBorder(2, NeoBrutalTheme.INK, NeoBrutalTheme.INK),
                BorderFactory.createEmptyBorder(2, 10, 2, 10)));

        JLabel value = new JLabel(angka);
        value.setFont(NeoBrutalTheme.display(16));
        value.setForeground(NeoBrutalTheme.INK);

        JLabel caption = new JLabel(label);
        caption.setFont(NeoBrutalTheme.mono(9));
        caption.setForeground(NeoBrutalTheme.INK);

        card.add(value, BorderLayout.CENTER);
        card.add(caption, BorderLayout.SOUTH);
        return card;
    }

    private JPanel buildCharts(LocalDate today) {
        JPanel charts = new JPanel(new GridLayout(1, 2, 12, 0));
        charts.setOpaque(false);
        charts.add(buildOmzetChart(today));
        charts.add(buildTerlarisChart(today));
        return charts;
    }

    private JPanel buildOmzetChart(LocalDate today) {
        List<LapGrafik> data = penjualanDAO.omzetPerHari(today.minusDays(6), today);
        DefaultCategoryDataset ds = new DefaultCategoryDataset();
        for (LapGrafik g : data) {
            ds.addValue(g.getNilai(), "Omzet", g.getLabel());
        }
        JFreeChart chart = ChartFactory.createBarChart(
                "", "Tanggal", "Rp", ds,
                PlotOrientation.VERTICAL, false, true, false);
        return chartCard("Omzet 7 Hari", "Tren penjualan mingguan", chart);
    }

    private JPanel buildTerlarisChart(LocalDate today) {
        List<LapTerlaris> list = penjualanDAO.lapTerlaris(today.withDayOfMonth(1), today);
        DefaultCategoryDataset ds = new DefaultCategoryDataset();
        int n = Math.min(5, list.size());
        for (int i = 0; i < n; i++) {
            LapTerlaris t = list.get(i);
            ds.addValue(t.getTotalQty(), "Qty", potongJudul(t.getJudul()));
        }
        JFreeChart chart = ChartFactory.createBarChart(
                "", "Buku", "Qty", ds,
                PlotOrientation.VERTICAL, false, true, false);
        String sub = list.isEmpty()
                ? "Belum ada penjualan bulan ini" : "5 buku terlaris bulan ini";
        return chartCard("Top 5 Buku", sub, chart);
    }

    private JPanel chartCard(String title, String subtitle, JFreeChart chart) {
        JPanel wrap = new JPanel(new BorderLayout(0, 6));
        wrap.setOpaque(false);

        JPanel head = new JPanel();
        head.setOpaque(false);
        head.setLayout(new BoxLayout(head, BoxLayout.Y_AXIS));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(NeoBrutalTheme.display(13));
        lblTitle.setForeground(NeoBrutalTheme.INK);
        lblTitle.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lblSub = new JLabel(subtitle);
        lblSub.setFont(NeoBrutalTheme.mono(9));
        lblSub.setForeground(NeoBrutalTheme.TEXT_MUTED);
        lblSub.setAlignmentX(LEFT_ALIGNMENT);

        head.add(lblTitle);
        head.add(Box.createVerticalStrut(2));
        head.add(lblSub);

        NeoChartStyle.apply(chart);
        wrap.add(head, BorderLayout.NORTH);
        wrap.add(NeoChartStyle.card(chart), BorderLayout.CENTER);
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
        p.setBackground(NeoBrutalTheme.SURFACE);
        p.setBorder(BorderFactory.createCompoundBorder(
                new NeoShadowBorder(),
                BorderFactory.createEmptyBorder(9, 12, 9, 12)));

        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        JLabel lbl = new JLabel("Stok Menipis");
        lbl.setFont(NeoBrutalTheme.display(14));
        lbl.setForeground(NeoBrutalTheme.INK);
        JLabel badge = new JLabel(String.valueOf(menipis.size()));
        badge.setFont(NeoBrutalTheme.mono(9));
        badge.setOpaque(true);
        badge.setBackground(NeoBrutalTheme.PRIMARY);
        badge.setForeground(NeoBrutalTheme.INK);
        badge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(NeoBrutalTheme.INK, 1),
                BorderFactory.createEmptyBorder(2, 7, 2, 7)));
        head.add(lbl, BorderLayout.WEST);
        head.add(badge, BorderLayout.EAST);
        p.add(head, BorderLayout.NORTH);

        DefaultTableModel model = new DefaultTableModel(
                new String[]{"Kode", "Judul", "Stok"}, 0) {
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
        NeoTableStyle.apply(tbl);
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

        JScrollPane scroll = NeoTableStyle.wrap(tbl);
        scroll.setPreferredSize(new Dimension(0, 64));
        p.add(scroll, BorderLayout.CENTER);
        return p;
    }
}
