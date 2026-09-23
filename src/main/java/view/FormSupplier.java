package view;

import dao.SupplierDAO;
import dao.SupplierDAOImpl;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import model.Supplier;
import org.kordamp.ikonli.materialdesign2.MaterialDesignC;
import org.kordamp.ikonli.materialdesign2.MaterialDesignD;
import org.kordamp.ikonli.materialdesign2.MaterialDesignM;
import org.kordamp.ikonli.materialdesign2.MaterialDesignP;
import org.kordamp.ikonli.swing.FontIcon;
import util.NeoBrutalTheme;
import util.NeoShadowBorder;
import util.Validasi;

public class FormSupplier extends JPanel {

    private final SupplierDAO supplierDAO = new SupplierDAOImpl();

    private int selectedId = -1;

    private JTable table;
    private DefaultTableModel model;
    private JTextField txtSearch;
    private JTextField txtNama;
    private JTextField txtAlamat;
    private JTextField txtNoTelp;

    public FormSupplier() {
        setLayout(new BorderLayout(12, 12));
        setBackground(NeoBrutalTheme.BG);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        add(buildNorth(), BorderLayout.NORTH);

        JPanel cardInput = buildInputCard();

        model = new DefaultTableModel(new String[]{"ID", "Nama", "Alamat", "No. Telp"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        table.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) isiFieldDariBaris();
        });

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, cardInput, scroll);
        split.setResizeWeight(0.35);
        split.setDividerLocation(340);
        split.setBackground(NeoBrutalTheme.BG);
        add(split, BorderLayout.CENTER);

        add(buildSouth(), BorderLayout.SOUTH);

        loadTable();
    }

    private JPanel buildNorth() {
        JPanel pnl = new JPanel(new BorderLayout(8, 8));
        pnl.setBackground(NeoBrutalTheme.BG);
        JLabel lblJudul = new JLabel("Data Supplier");
        lblJudul.setFont(new Font("Segoe UI Black", Font.BOLD, 22));
        lblJudul.setForeground(Color.BLACK);
        pnl.add(lblJudul, BorderLayout.WEST);

        JPanel pnlSearch = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlSearch.setBackground(NeoBrutalTheme.BG);
        JLabel lblCari = new JLabel("Cari", FontIcon.of(MaterialDesignM.MAGNIFY, 20, Color.BLACK),
                SwingConstants.LEFT);
        lblCari.setFont(new Font("Segoe UI Semibold", Font.BOLD, 13));
        txtSearch = new JTextField(20);
        txtSearch.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { cari(txtSearch.getText()); }
            @Override public void removeUpdate(DocumentEvent e) { cari(txtSearch.getText()); }
            @Override public void changedUpdate(DocumentEvent e) { cari(txtSearch.getText()); }
        });
        pnlSearch.add(lblCari);
        pnlSearch.add(txtSearch);
        pnl.add(pnlSearch, BorderLayout.EAST);
        return pnl;
    }

    private JPanel buildInputCard() {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(NeoBrutalTheme.SURFACE);
        card.setBorder(new NeoShadowBorder());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 8, 5, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        txtNama = field("nama_supplier");
        txtAlamat = field("alamat");
        txtNoTelp = field("no_telp");

        int y = 0;
        y = addRow(card, gbc, y, "Nama Supplier", txtNama);
        y = addRow(card, gbc, y, "Alamat", txtAlamat);
        y = addRow(card, gbc, y, "No. Telp", txtNoTelp);
        return card;
    }

    private JTextField field(String name) {
        JTextField f = new JTextField(16);
        f.setName(name);
        f.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        return f;
    }

    private int addRow(JPanel card, GridBagConstraints gbc, int y, String label,
            java.awt.Component comp) {
        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.weightx = 0;
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI Semibold", Font.BOLD, 13));
        card.add(lbl, gbc);
        gbc.gridx = 1;
        gbc.gridy = y;
        gbc.weightx = 1.0;
        card.add(comp, gbc);
        return y + 1;
    }

    private JPanel buildSouth() {
        JPanel pnl = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnl.setBackground(NeoBrutalTheme.BG);
        JButton btnSimpan = styledButton("Simpan",
                FontIcon.of(MaterialDesignC.CONTENT_SAVE, 18, Color.BLACK),
                NeoBrutalTheme.PRIMARY);
        JButton btnEdit = styledButton("Edit",
                FontIcon.of(MaterialDesignP.PENCIL, 18, Color.BLACK),
                NeoBrutalTheme.SECONDARY);
        JButton btnHapus = styledButton("Hapus",
                FontIcon.of(MaterialDesignD.DELETE, 18, Color.BLACK),
                NeoBrutalTheme.DANGER);
        btnSimpan.addActionListener(e -> doSimpan());
        btnEdit.addActionListener(e -> doEdit());
        btnHapus.addActionListener(e -> doHapus());
        pnl.add(btnSimpan);
        pnl.add(btnEdit);
        pnl.add(btnHapus);
        return pnl;
    }

    private JButton styledButton(String teks, FontIcon ikon, Color bg) {
        JButton b = new JButton(teks, ikon);
        b.setIconTextGap(8);
        b.setFont(new Font("Segoe UI Semibold", Font.BOLD, 13));
        b.setBackground(bg);
        b.setForeground(Color.BLACK);
        b.setBorder(new NeoShadowBorder());
        b.setFocusPainted(false);
        addPressEffect(b);
        return b;
    }

    private void addPressEffect(JButton btn) {
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                btn.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                btn.setBorder(new NeoShadowBorder());
            }
        });
    }

    public void loadTable() {
        isiTabel(supplierDAO.getAll());
    }

    public void cari(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            loadTable();
            return;
        }
        isiTabel(supplierDAO.search(keyword.trim()));
    }

    private void isiTabel(List<Supplier> list) {
        model.setRowCount(0);
        for (Supplier s : list) {
            model.addRow(new Object[]{s.getIdSupplier(), s.getNamaSupplier(),
                    s.getAlamat(), s.getNoTelp()});
        }
    }

    private void isiFieldDariBaris() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        int id = (int) model.getValueAt(row, 0);
        Supplier s = supplierDAO.getById(id);
        if (s == null) return;
        selectedId = s.getIdSupplier();
        txtNama.setText(s.getNamaSupplier());
        txtAlamat.setText(s.getAlamat() != null ? s.getAlamat() : "");
        txtNoTelp.setText(s.getNoTelp() != null ? s.getNoTelp() : "");
    }

    private Supplier bacaForm() {
        if (!Validasi.wajib(txtNama)) return null;
        Supplier s = new Supplier();
        s.setNamaSupplier(txtNama.getText().trim());
        s.setAlamat(txtAlamat.getText().trim());
        s.setNoTelp(txtNoTelp.getText().trim());
        return s;
    }

    private void doSimpan() {
        Supplier s = bacaForm();
        if (s == null) return;
        try {
            supplierDAO.insert(s);
            reset();
            loadTable();
            JOptionPane.showMessageDialog(this, "Supplier tersimpan");
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "Gagal simpan supplier: " + ex.getMessage(),
                    "Gagal Simpan", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doEdit() {
        if (selectedId < 0) {
            JOptionPane.showMessageDialog(this, "Pilih data supplier dulu (klik baris tabel)");
            return;
        }
        Supplier s = bacaForm();
        if (s == null) return;
        s.setIdSupplier(selectedId);
        try {
            supplierDAO.update(s);
            reset();
            loadTable();
            JOptionPane.showMessageDialog(this, "Supplier terupdate");
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "Gagal edit supplier: " + ex.getMessage(),
                    "Gagal Edit", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doHapus() {
        if (selectedId < 0) {
            JOptionPane.showMessageDialog(this, "Pilih data supplier dulu (klik baris tabel)");
            return;
        }
        int y = JOptionPane.showConfirmDialog(this, "Hapus supplier ini?",
                "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (y != JOptionPane.YES_OPTION) return;
        try {
            supplierDAO.delete(selectedId);
            reset();
            loadTable();
            JOptionPane.showMessageDialog(this, "Supplier terhapus");
        } catch (RuntimeException ex) {
            if (isIntegrityViolation(ex)) {
                JOptionPane.showMessageDialog(this, "Data dipakai transaksi, tidak bisa dihapus",
                        "Gagal Hapus", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Gagal hapus supplier: " + ex.getMessage(),
                        "Gagal Hapus", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void reset() {
        selectedId = -1;
        txtNama.setText("");
        txtAlamat.setText("");
        txtNoTelp.setText("");
        table.clearSelection();
    }

    private boolean isIntegrityViolation(Throwable t) {
        while (t != null) {
            if (t instanceof SQLIntegrityConstraintViolationException) return true;
            t = t.getCause();
        }
        return false;
    }
}
