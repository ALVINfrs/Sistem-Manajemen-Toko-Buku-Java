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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
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
import model.Kategori;
import model.LapGrafik;
import org.kordamp.ikonli.materialdesign2.MaterialDesignC;
import org.kordamp.ikonli.materialdesign2.MaterialDesignD;
import org.kordamp.ikonli.materialdesign2.MaterialDesignM;
import org.kordamp.ikonli.materialdesign2.MaterialDesignP;
import org.kordamp.ikonli.swing.FontIcon;
import util.NeoBrutalTheme;
import util.Validasi;

public class FormKategori extends JPanel {

    private final KategoriDAO kategoriDAO = new KategoriDAOImpl();
    private final BukuDAO bukuDAO = new BukuDAOImpl();

    private int selectedId = -1;

    private JTable table;
    private DefaultTableModel model;
    private JTextField txtSearch;
    private JTextField txtNama;
    private JTextField txtDeskripsi;

    public FormKategori() {
        setLayout(new BorderLayout(12, 12));
        setBackground(NeoBrutalTheme.BG);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        add(buildNorth(), BorderLayout.NORTH);

        JPanel cardInput = buildInputCard();

        model = new DefaultTableModel(new String[]{"ID", "Nama Kategori", "Deskripsi", "Jml Buku"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        table.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        NeoTableStyle.apply(table);
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

        return new NeoPageHeader("MASTER DATA", "Data Kategori",
                "Kelompokkan buku dan ATK agar katalog rapi.", pnlSearch);
    }

    private JPanel buildInputCard() {
        JPanel card = NeoFormStyle.card(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 8, 4, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        txtNama = NeoFormStyle.field("nama_kategori");
        txtDeskripsi = NeoFormStyle.field("deskripsi");

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        card.add(NeoFormStyle.section("Nama Kategori"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        card.add(txtNama, gbc);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        card.add(NeoFormStyle.section("Deskripsi"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        card.add(txtDeskripsi, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        card.add(javax.swing.Box.createGlue(), gbc);
        return card;
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

    public void loadTable() {
        isiTabel(kategoriDAO.getAll());
    }

    public void cari(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            loadTable();
            return;
        }
        isiTabel(kategoriDAO.search(keyword.trim()));
    }

    private void isiTabel(List<Kategori> list) {
        Map<String, Integer> count = new HashMap<>();
        try {
            for (LapGrafik g : bukuDAO.countByKategori()) {
                count.put(g.getLabel(), (int) g.getNilai());
            }
        } catch (RuntimeException e) {
            count.clear();
        }
        model.setRowCount(0);
        for (Kategori k : list) {
            model.addRow(new Object[]{k.getIdKategori(), k.getNamaKategori(),
                    k.getDeskripsi() != null ? k.getDeskripsi() : "",
                    count.getOrDefault(k.getNamaKategori(), 0)});
        }
    }

    private void isiFieldDariBaris() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        int id = (int) model.getValueAt(row, 0);
        Kategori k = kategoriDAO.getById(id);
        if (k == null) return;
        selectedId = k.getIdKategori();
        txtNama.setText(k.getNamaKategori());
        txtDeskripsi.setText(k.getDeskripsi() != null ? k.getDeskripsi() : "");
    }

    private String bacaDeskripsi() {
        String d = txtDeskripsi.getText().trim();
        return d.isEmpty() ? null : d;
    }

    private void doSimpan() {
        if (!Validasi.wajib(txtNama)) return;
        Kategori k = new Kategori();
        k.setNamaKategori(txtNama.getText().trim());
        k.setDeskripsi(bacaDeskripsi());
        try {
            kategoriDAO.insert(k);
            reset();
            loadTable();
            JOptionPane.showMessageDialog(this, "Kategori tersimpan");
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "Gagal simpan kategori: " + ex.getMessage(),
                    "Gagal Simpan", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doEdit() {
        if (selectedId < 0) {
            JOptionPane.showMessageDialog(this, "Pilih data kategori dulu (klik baris tabel)");
            return;
        }
        if (!Validasi.wajib(txtNama)) return;
        Kategori k = new Kategori();
        k.setIdKategori(selectedId);
        k.setNamaKategori(txtNama.getText().trim());
        k.setDeskripsi(bacaDeskripsi());
        try {
            kategoriDAO.update(k);
            reset();
            loadTable();
            JOptionPane.showMessageDialog(this, "Kategori terupdate");
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "Gagal edit kategori: " + ex.getMessage(),
                    "Gagal Edit", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doHapus() {
        if (selectedId < 0) {
            JOptionPane.showMessageDialog(this, "Pilih data kategori dulu (klik baris tabel)");
            return;
        }
        int y = JOptionPane.showConfirmDialog(this, "Hapus kategori ini?",
                "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (y != JOptionPane.YES_OPTION) return;
        try {
            kategoriDAO.delete(selectedId);
            reset();
            loadTable();
            JOptionPane.showMessageDialog(this, "Kategori terhapus");
        } catch (RuntimeException ex) {
            if (isIntegrityViolation(ex)) {
                JOptionPane.showMessageDialog(this, "Data dipakai transaksi, tidak bisa dihapus",
                        "Gagal Hapus", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Gagal hapus kategori: " + ex.getMessage(),
                        "Gagal Hapus", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void reset() {
        selectedId = -1;
        txtNama.setText("");
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
