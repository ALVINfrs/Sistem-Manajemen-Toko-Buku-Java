package view;

import dao.BukuDAO;
import dao.BukuDAOImpl;
import dao.KategoriDAO;
import dao.KategoriDAOImpl;
import dao.PenerbitDAO;
import dao.PenerbitDAOImpl;
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
import javax.swing.JComboBox;
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
import model.Buku;
import model.Kategori;
import model.Penerbit;
import org.kordamp.ikonli.materialdesign2.MaterialDesignC;
import org.kordamp.ikonli.materialdesign2.MaterialDesignD;
import org.kordamp.ikonli.materialdesign2.MaterialDesignM;
import org.kordamp.ikonli.materialdesign2.MaterialDesignP;
import org.kordamp.ikonli.swing.FontIcon;
import util.ComboItem;
import util.NeoBrutalTheme;
import util.NeoShadowBorder;
import util.Validasi;

public class FormBuku extends JPanel {

    private final BukuDAO bukuDAO = new BukuDAOImpl();
    private final PenerbitDAO penerbitDAO = new PenerbitDAOImpl();
    private final KategoriDAO kategoriDAO = new KategoriDAOImpl();

    private int selectedId = -1;

    private JTable table;
    private DefaultTableModel model;
    private JTextField txtSearch;
    private JTextField txtKode;
    private JTextField txtJudul;
    private JTextField txtPenulis;
    private JComboBox<ComboItem> cmbPenerbit;
    private JComboBox<ComboItem> cmbKategori;
    private JTextField txtHargaBeli;
    private JTextField txtHargaJual;
    private JTextField txtStok;

    public FormBuku() {
        setLayout(new BorderLayout(12, 12));
        setBackground(NeoBrutalTheme.BG);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        add(buildNorth(), BorderLayout.NORTH);

        JPanel cardInput = buildInputCard();

        model = new DefaultTableModel(
                new String[]{"ID", "Kode", "Judul", "Penulis", "Penerbit", "Kategori",
                        "Harga Beli", "Harga Jual", "Stok"}, 0) {
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
        JLabel lblJudul = new JLabel("Data Buku");
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

        txtKode = field("kode_buku");
        txtJudul = field("judul");
        txtPenulis = field("penulis");
        cmbPenerbit = new JComboBox<>();
        cmbPenerbit.setBackground(NeoBrutalTheme.SURFACE);
        cmbPenerbit.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        cmbKategori = new JComboBox<>();
        cmbKategori.setBackground(NeoBrutalTheme.SURFACE);
        cmbKategori.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        txtHargaBeli = field("harga_beli");
        txtHargaJual = field("harga_jual");
        txtStok = field("stok");

        int y = 0;
        y = addRow(card, gbc, y, "Kode Buku", txtKode);
        y = addRow(card, gbc, y, "Judul", txtJudul);
        y = addRow(card, gbc, y, "Penulis", txtPenulis);
        y = addRow(card, gbc, y, "Penerbit", cmbPenerbit);
        y = addRow(card, gbc, y, "Kategori", cmbKategori);
        y = addRow(card, gbc, y, "Harga Beli", txtHargaBeli);
        y = addRow(card, gbc, y, "Harga Jual", txtHargaJual);
        y = addRow(card, gbc, y, "Stok", txtStok);

        reloadCombos();
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

    private void reloadCombos() {
        cmbPenerbit.removeAllItems();
        cmbPenerbit.addItem(ComboItem.EMPTY);
        for (Penerbit p : penerbitDAO.getAll()) {
            cmbPenerbit.addItem(new ComboItem(p.getIdPenerbit(), p.getNamaPenerbit()));
        }
        cmbKategori.removeAllItems();
        cmbKategori.addItem(ComboItem.EMPTY);
        for (Kategori k : kategoriDAO.getAll()) {
            cmbKategori.addItem(new ComboItem(k.getIdKategori(), k.getNamaKategori()));
        }
    }

    private void selectCombo(JComboBox<ComboItem> cmb, Integer id) {
        if (id == null) {
            cmb.setSelectedItem(ComboItem.EMPTY);
            return;
        }
        for (int i = 0; i < cmb.getItemCount(); i++) {
            if (cmb.getItemAt(i).id == id) {
                cmb.setSelectedIndex(i);
                return;
            }
        }
        cmb.setSelectedItem(ComboItem.EMPTY);
    }

    public void loadTable() {
        isiTabel(bukuDAO.getAll());
    }

    public void cari(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            loadTable();
            return;
        }
        isiTabel(bukuDAO.search(keyword.trim()));
    }

    private void isiTabel(List<Buku> list) {
        model.setRowCount(0);
        for (Buku b : list) {
            model.addRow(new Object[]{
                    b.getIdBuku(),
                    b.getKodeBuku(),
                    b.getJudul(),
                    b.getPenulis(),
                    b.getNamaPenerbit() != null ? b.getNamaPenerbit() : "-",
                    b.getNamaKategori() != null ? b.getNamaKategori() : "-",
                    b.getHargaBeli(),
                    b.getHargaJual(),
                    b.getStok()
            });
        }
    }

    private void isiFieldDariBaris() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        int id = (int) model.getValueAt(row, 0);
        Buku b = bukuDAO.getById(id);
        if (b == null) return;
        selectedId = b.getIdBuku();
        txtKode.setText(b.getKodeBuku());
        txtJudul.setText(b.getJudul());
        txtPenulis.setText(b.getPenulis() != null ? b.getPenulis() : "");
        selectCombo(cmbPenerbit, b.getIdPenerbit());
        selectCombo(cmbKategori, b.getIdKategori());
        txtHargaBeli.setText(String.valueOf(b.getHargaBeli()));
        txtHargaJual.setText(String.valueOf(b.getHargaJual()));
        txtStok.setText(String.valueOf(b.getStok()));
    }

    private Buku bacaForm() {
        if (!Validasi.wajib(txtKode, txtJudul)) return null;
        if (!Validasi.angka(txtHargaBeli)) return null;
        if (!Validasi.angka(txtHargaJual)) return null;
        if (!Validasi.integer(txtStok)) return null;
        ComboItem pen = (ComboItem) cmbPenerbit.getSelectedItem();
        ComboItem kat = (ComboItem) cmbKategori.getSelectedItem();
        Buku b = new Buku();
        b.setKodeBuku(txtKode.getText().trim());
        b.setJudul(txtJudul.getText().trim());
        b.setPenulis(txtPenulis.getText().trim());
        b.setIdPenerbit(pen == null || pen.id < 0 ? null : pen.id);
        b.setIdKategori(kat == null || kat.id < 0 ? null : kat.id);
        b.setHargaBeli(Double.parseDouble(txtHargaBeli.getText().trim()));
        b.setHargaJual(Double.parseDouble(txtHargaJual.getText().trim()));
        b.setStok(Integer.parseInt(txtStok.getText().trim()));
        return b;
    }

    private void doSimpan() {
        Buku b = bacaForm();
        if (b == null) return;
        try {
            bukuDAO.insert(b);
            reset();
            loadTable();
            JOptionPane.showMessageDialog(this, "Buku tersimpan");
        } catch (RuntimeException ex) {
            if (isIntegrityViolation(ex)) {
                JOptionPane.showMessageDialog(this, "Kode buku sudah dipakai",
                        "Gagal Simpan", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Gagal simpan buku: " + ex.getMessage(),
                        "Gagal Simpan", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void doEdit() {
        if (selectedId < 0) {
            JOptionPane.showMessageDialog(this, "Pilih data buku dulu (klik baris tabel)");
            return;
        }
        Buku b = bacaForm();
        if (b == null) return;
        b.setIdBuku(selectedId);
        try {
            bukuDAO.update(b);
            reset();
            loadTable();
            JOptionPane.showMessageDialog(this, "Buku terupdate");
        } catch (RuntimeException ex) {
            if (isIntegrityViolation(ex)) {
                JOptionPane.showMessageDialog(this, "Kode buku sudah dipakai",
                        "Gagal Edit", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Gagal edit buku: " + ex.getMessage(),
                        "Gagal Edit", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void doHapus() {
        if (selectedId < 0) {
            JOptionPane.showMessageDialog(this, "Pilih data buku dulu (klik baris tabel)");
            return;
        }
        int y = JOptionPane.showConfirmDialog(this, "Hapus buku ini?",
                "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (y != JOptionPane.YES_OPTION) return;
        try {
            bukuDAO.delete(selectedId);
            reset();
            loadTable();
            JOptionPane.showMessageDialog(this, "Buku terhapus");
        } catch (RuntimeException ex) {
            if (isIntegrityViolation(ex)) {
                JOptionPane.showMessageDialog(this, "Data dipakai transaksi, tidak bisa dihapus",
                        "Gagal Hapus", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Gagal hapus buku: " + ex.getMessage(),
                        "Gagal Hapus", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void reset() {
        selectedId = -1;
        txtKode.setText("");
        txtJudul.setText("");
        txtPenulis.setText("");
        cmbPenerbit.setSelectedItem(ComboItem.EMPTY);
        cmbKategori.setSelectedItem(ComboItem.EMPTY);
        txtHargaBeli.setText("");
        txtHargaJual.setText("");
        txtStok.setText("");
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
