package view;

import com.github.lgooddatepicker.components.DatePicker;
import dao.PenjualanDAO;
import dao.PenjualanDAOImpl;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import model.DetailPenjualan;
import model.LapPenjualan;
import model.Penjualan;
import org.kordamp.ikonli.materialdesign2.MaterialDesignM;
import org.kordamp.ikonli.swing.FontIcon;
import util.NeoBrutalTheme;
import util.NeoShadowBorder;
import util.Sesi;

public class FormRiwayat extends JPanel {

    private final PenjualanDAO penjualanDAO = new PenjualanDAOImpl();

    private DatePicker dpDari;
    private DatePicker dpSampai;
    private JTextField txtCari;
    private JTable tblRiwayat;
    private DefaultTableModel modelRiwayat;
    private List<LapPenjualan> cache = new ArrayList<>();

    private static final DateTimeFormatter FMT_TGL = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    public FormRiwayat() {
        setLayout(new BorderLayout(12, 12));
        setBackground(NeoBrutalTheme.BG);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JLabel lblJudul = new JLabel("Riwayat Nota");
        lblJudul.setFont(new Font("Segoe UI Black", Font.BOLD, 22));
        add(lblJudul, BorderLayout.NORTH);

        add(buildFilter(), BorderLayout.CENTER);
    }

    private JPanel buildFilter() {
        JPanel wrap = new JPanel(new BorderLayout(8, 8));
        wrap.setBackground(NeoBrutalTheme.BG);

        JPanel card = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        card.setBackground(NeoBrutalTheme.SURFACE);
        card.setBorder(new NeoShadowBorder());

        card.add(new JLabel("Dari"));
        dpDari = new DatePicker();
        dpDari.setName("tglDari");
        dpDari.setDate(LocalDate.now().minusDays(30));
        dpDari.addDateChangeListener(e -> reloadData());
        card.add(dpDari);

        card.add(new JLabel("Sampai"));
        dpSampai = new DatePicker();
        dpSampai.setName("tglSampai");
        dpSampai.setDateToToday();
        dpSampai.addDateChangeListener(e -> reloadData());
        card.add(dpSampai);

        txtCari = new JTextField(16);
        txtCari.setName("cari_nota");
        txtCari.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        txtCari.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { applyFilter(); }
            @Override public void removeUpdate(DocumentEvent e) { applyFilter(); }
            @Override public void changedUpdate(DocumentEvent e) { applyFilter(); }
        });
        card.add(new JLabel(FontIcon.of(MaterialDesignM.MAGNIFY, 18, Color.BLACK)));
        card.add(txtCari);
        wrap.add(card, BorderLayout.NORTH);

        modelRiwayat = new DefaultTableModel(
                new String[]{"Nota", "Tanggal", "Kasir", "Member", "Metode", "Total"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblRiwayat = new JTable(modelRiwayat);
        tblRiwayat.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) bukaStruk();
            }
        });
        tblRiwayat.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "bukaStruk");
        tblRiwayat.getActionMap().put("bukaStruk", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                bukaStruk();
            }
        });
        JScrollPane scroll = new JScrollPane(tblRiwayat);
        scroll.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        wrap.add(scroll, BorderLayout.CENTER);

        JLabel lblHint = new JLabel("Klik 2x / Enter pada baris untuk buka struk (cetak ulang)");
        lblHint.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 12));
        wrap.add(lblHint, BorderLayout.SOUTH);

        reloadData();
        return wrap;
    }

    public void setPeriode(LocalDate dari, LocalDate sampai) {
        dpDari.setDate(dari);
        dpSampai.setDate(sampai);
        reloadData();
    }

    public int getRowCount() {
        return modelRiwayat.getRowCount();
    }

    private void reloadData() {
        if (dpDari == null || dpSampai == null) return;
        LocalDate a = dpDari.getDate();
        LocalDate b = dpSampai.getDate();
        if (a == null || b == null) return;
        List<LapPenjualan> list;
        if (Sesi.userLogin != null && !Sesi.isAdmin()) {
            list = penjualanDAO.lapPenjualanByUser(a, b, Sesi.userLogin.getIdUser());
        } else if (Sesi.userLogin != null) {
            list = penjualanDAO.lapPenjualan(a, b);
        } else {
            list = new ArrayList<>();
        }
        list.sort(Comparator.comparing(LapPenjualan::getTanggal,
                Comparator.nullsLast(Comparator.naturalOrder())).reversed());
        cache = list;
        applyFilter();
    }

    private void applyFilter() {
        String key = txtCari == null ? "" : txtCari.getText().trim().toLowerCase();
        modelRiwayat.setRowCount(0);
        for (LapPenjualan l : cache) {
            if (!key.isEmpty() && (l.getNoNota() == null
                    || !l.getNoNota().toLowerCase().contains(key))) {
                continue;
            }
            modelRiwayat.addRow(new Object[]{
                    l.getNoNota(),
                    l.getTanggal() != null ? l.getTanggal().format(FMT_TGL) : "-",
                    l.getKasir(),
                    l.getMember(),
                    l.getMetodeBayar(),
                    l.getTotal()
            });
        }
    }

    private void bukaStruk() {
        int row = tblRiwayat.getSelectedRow();
        if (row < 0 || row >= modelRiwayat.getRowCount()) {
            JOptionPane.showMessageDialog(this, "Pilih baris nota dulu",
                    "Validasi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String noNota = String.valueOf(modelRiwayat.getValueAt(row, 0));
        String kasir = String.valueOf(modelRiwayat.getValueAt(row, 2));
        String member = String.valueOf(modelRiwayat.getValueAt(row, 3));
        try {
            Penjualan h = penjualanDAO.getByNoNota(noNota);
            if (h == null) {
                JOptionPane.showMessageDialog(this, "Nota tidak ditemukan",
                        "Validasi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            List<DetailPenjualan> items = penjualanDAO.getDetailByPenjualan(h.getIdPenjualan());
            Frame owner = (Frame) SwingUtilities.getWindowAncestor(this);
            new StrukDialog(owner, h, items, kasir, member, 0, h.getDiskon()).setVisible(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal buka struk: " + ex.getMessage(),
                    "Gagal", JOptionPane.ERROR_MESSAGE);
        }
    }
}
