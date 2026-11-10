package view;

import dao.BukuDAO;
import dao.BukuDAOImpl;
import dao.KategoriDAO;
import dao.KategoriDAOImpl;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import model.Buku;
import model.Kategori;
import org.kordamp.ikonli.materialdesign2.MaterialDesignC;
import org.kordamp.ikonli.materialdesign2.MaterialDesignD;
import org.kordamp.ikonli.materialdesign2.MaterialDesignM;
import org.kordamp.ikonli.materialdesign2.MaterialDesignP;
import org.kordamp.ikonli.swing.FontIcon;
import util.ComboItem;
import util.NeoBrutalTheme;
import util.Validasi;

public class FormAtk extends JPanel {

    private static final String[] DAFTAR_SATUAN = {"pcs", "pak", "lusin", "box", "rim"};

    private final BukuDAO bukuDAO = new BukuDAOImpl();
    private final KategoriDAO kategoriDAO = new KategoriDAOImpl();

    private int selectedId = -1;

    private JTable table;
    private DefaultTableModel model;
    private JTextField txtSearch;
    private JTextField txtKode;
    private JTextField txtJudul;
    private JComboBox<ComboItem> cmbKategori;
    private JComboBox<String> cmbSatuan;
    private JTextField txtHargaBeli;
    private JTextField txtHargaJual;
    private JTextField txtStok;
    private javax.swing.JTextArea txtDeskripsi;

    public FormAtk() {
        setLayout(new BorderLayout(12, 12));
        setBackground(NeoBrutalTheme.BG);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        add(buildNorth(), BorderLayout.NORTH);

        JPanel cardInput = buildInputCard();

        model = new DefaultTableModel(
                new String[]{"ID", "Kode", "Nama", "Kategori", "Satuan",
                        "H. Beli", "H. Jual", "Stok"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        table.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        NeoTableStyle.apply(table);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        int[] lebar = {45, 75, 150, 95, 65, 85, 85, 55};
        for (int i = 0; i < lebar.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(lebar[i]);
        }
        JScrollPane scroll = NeoTableStyle.wrap(table);

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
        JPanel pnlSearch = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlSearch.setOpaque(false);

        JLabel lblCari = NeoFormStyle.label("Cari");
        lblCari.setIcon(FontIcon.of(MaterialDesignM.MAGNIFY, 18, NeoBrutalTheme.INK));
        lblCari.setIconTextGap(5);

        txtSearch = NeoFormStyle.field("search");
        txtSearch.setColumns(20);
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { cari(txtSearch.getText()); }
            @Override public void removeUpdate(DocumentEvent e) { cari(txtSearch.getText()); }
            @Override public void changedUpdate(DocumentEvent e) { cari(txtSearch.getText()); }
        });
        pnlSearch.add(lblCari);
        pnlSearch.add(txtSearch);

        return new NeoPageHeader("MASTER DATA", "Data ATK",
                "Kelola alat tulis kantor, harga, dan stok.", pnlSearch);
    }

    private JPanel buildInputCard() {
        JPanel card = NeoFormStyle.card(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 8, 3, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        txtKode = NeoFormStyle.field("kode");
        txtJudul = NeoFormStyle.field("judul");
        cmbKategori = NeoFormStyle.combo();
        cmbKategori.setName("kategori");
        cmbSatuan = new JComboBox<>(DAFTAR_SATUAN);
        cmbSatuan.setName("satuan");
        NeoFormStyle.styleCombo(cmbSatuan);
        txtHargaBeli = NeoFormStyle.field("harga_beli");
        txtHargaJual = NeoFormStyle.field("harga_jual");
        txtStok = NeoFormStyle.field("stok");
        txtDeskripsi = new javax.swing.JTextArea(3, 16);
        txtDeskripsi.setName("deskripsi");
        txtDeskripsi.setLineWrap(true);
        txtDeskripsi.setWrapStyleWord(true);
        NeoFormStyle.styleTextArea(txtDeskripsi);
        JScrollPane scrollDesc = new JScrollPane(txtDeskripsi);
        scrollDesc.setBorder(BorderFactory.createLineBorder(NeoBrutalTheme.INK, 2));

        int y = 0;
        y = addRow(card, gbc, y, "Kode", txtKode);
        y = addRow(card, gbc, y, "Nama Barang", txtJudul);
        y = addRow(card, gbc, y, "Kategori", cmbKategori);
        y = addRow(card, gbc, y, "Satuan", cmbSatuan);
        y = addRow(card, gbc, y, "Harga Beli", txtHargaBeli);
        y = addRow(card, gbc, y, "Harga Jual", txtHargaJual);
        y = addRow(card, gbc, y, "Stok", txtStok);
        y = addRow(card, gbc, y, "Deskripsi", scrollDesc);

        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        card.add(javax.swing.Box.createGlue(), gbc);

        reloadCombos();
        return card;
    }


    private int addRow(JPanel card, GridBagConstraints gbc, int y, String label,
            java.awt.Component comp) {
        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.weightx = 0;
        card.add(NeoFormStyle.section(label), gbc);
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

    private NeoButton styledButton(String teks, FontIcon ikon, Color bg) {
        return NeoFormStyle.button(teks, ikon, bg);
    }

    private void reloadCombos() {
        cmbKategori.removeAllItems();
        cmbKategori.addItem(ComboItem.EMPTY);
        for (Kategori k : kategoriDAO.getAll()) {
            cmbKategori.addItem(new ComboItem(k.getIdKategori(), k.getNamaKategori()));
        }
        cmbSatuan.setSelectedItem("pcs");
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
        isiTabel(bukuDAO.getAllByJenis("ATK"));
    }

    public void cari(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            loadTable();
            return;
        }
        isiTabel(bukuDAO.searchByJenis("ATK", keyword.trim()));
    }

    private void isiTabel(List<Buku> list) {
        model.setRowCount(0);
        for (Buku b : list) {
            model.addRow(new Object[]{
                    b.getIdBuku(),
                    b.getKodeBuku(),
                    b.getJudul(),
                    b.getNamaKategori() != null ? b.getNamaKategori() : "-",
                    b.getSatuan() != null && !b.getSatuan().isBlank() ? b.getSatuan() : "-",
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
        selectCombo(cmbKategori, b.getIdKategori());
        if (b.getSatuan() != null && !b.getSatuan().isBlank()) {
            cmbSatuan.setSelectedItem(b.getSatuan());
        } else {
            cmbSatuan.setSelectedItem("pcs");
        }
        txtHargaBeli.setText(String.valueOf(b.getHargaBeli()));
        txtHargaJual.setText(String.valueOf(b.getHargaJual()));
        txtStok.setText(String.valueOf(b.getStok()));
        txtDeskripsi.setText(b.getDeskripsi() != null ? b.getDeskripsi() : "");
    }

    private String satuanTerpilih() {
        Object s = cmbSatuan.getSelectedItem();
        return s == null ? "" : s.toString().trim();
    }

    private Buku bacaForm() {
        if (!Validasi.wajib(txtKode, txtJudul)) return null;
        ComboItem kat = (ComboItem) cmbKategori.getSelectedItem();
        if (kat == null || kat.id < 0) {
            JOptionPane.showMessageDialog(this, "Kategori wajib dipilih");
            cmbKategori.requestFocus();
            return null;
        }
        String satuan = satuanTerpilih();
        if (satuan.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Satuan wajib dipilih");
            cmbSatuan.requestFocus();
            return null;
        }
        if (!Validasi.angka(txtHargaBeli)) return null;
        if (!Validasi.angka(txtHargaJual)) return null;
        if (!Validasi.integer(txtStok)) return null;
        int stok = Integer.parseInt(txtStok.getText().trim());
        if (stok < 0) {
            JOptionPane.showMessageDialog(this, "Stok tidak boleh negatif");
            txtStok.requestFocus();
            return null;
        }
        Buku b = new Buku();
        b.setKodeBuku(txtKode.getText().trim());
        b.setJudul(txtJudul.getText().trim());
        b.setJenis("ATK");
        b.setSatuan(satuan);
        b.setIdKategori(kat.id);
        b.setIdPenerbit(null);
        b.setPenulis(null);
        b.setHargaBeli(Double.parseDouble(txtHargaBeli.getText().trim()));
        b.setHargaJual(Double.parseDouble(txtHargaJual.getText().trim()));
        b.setStok(stok);
        b.setDeskripsi(txtDeskripsi.getText().trim());
        return b;
    }

    private void doSimpan() {
        Buku b = bacaForm();
        if (b == null) return;
        if (bukuDAO.getByKode(b.getKodeBuku()) != null) {
            JOptionPane.showMessageDialog(this, "Kode sudah dipakai",
                    "Gagal Simpan", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            bukuDAO.insert(b);
            reset();
            loadTable();
            JOptionPane.showMessageDialog(this, "Barang ATK tersimpan");
        } catch (RuntimeException ex) {
            if (isIntegrityViolation(ex)) {
                JOptionPane.showMessageDialog(this, "Kode sudah dipakai",
                        "Gagal Simpan", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Gagal simpan ATK: " + ex.getMessage(),
                        "Gagal Simpan", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void doEdit() {
        if (selectedId < 0) {
            JOptionPane.showMessageDialog(this, "Pilih data ATK dulu (klik baris tabel)");
            return;
        }
        Buku b = bacaForm();
        if (b == null) return;
        Buku lain = bukuDAO.getByKode(b.getKodeBuku());
        if (lain != null && lain.getIdBuku() != selectedId) {
            JOptionPane.showMessageDialog(this, "Kode sudah dipakai",
                    "Gagal Edit", JOptionPane.ERROR_MESSAGE);
            return;
        }
        b.setIdBuku(selectedId);
        try {
            bukuDAO.update(b);
            reset();
            loadTable();
            JOptionPane.showMessageDialog(this, "Barang ATK terupdate");
        } catch (RuntimeException ex) {
            if (isIntegrityViolation(ex)) {
                JOptionPane.showMessageDialog(this, "Kode sudah dipakai",
                        "Gagal Edit", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Gagal edit ATK: " + ex.getMessage(),
                        "Gagal Edit", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void doHapus() {
        if (selectedId < 0) {
            JOptionPane.showMessageDialog(this, "Pilih data ATK dulu (klik baris tabel)");
            return;
        }
        int y = JOptionPane.showConfirmDialog(this, "Hapus barang ATK ini?",
                "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (y != JOptionPane.YES_OPTION) return;
        try {
            bukuDAO.delete(selectedId);
            reset();
            loadTable();
            JOptionPane.showMessageDialog(this, "Barang ATK terhapus");
        } catch (RuntimeException ex) {
            if (isIntegrityViolation(ex)) {
                JOptionPane.showMessageDialog(this, "Data dipakai transaksi, tidak bisa dihapus",
                        "Gagal Hapus", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Gagal hapus ATK: " + ex.getMessage(),
                        "Gagal Hapus", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void reset() {
        selectedId = -1;
        txtKode.setText("");
        txtJudul.setText("");
        cmbKategori.setSelectedItem(ComboItem.EMPTY);
        cmbSatuan.setSelectedItem("pcs");
        txtHargaBeli.setText("");
        txtHargaJual.setText("");
        txtStok.setText("");
        txtDeskripsi.setText("");
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
