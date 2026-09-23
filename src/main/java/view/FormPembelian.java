package view;

import dao.BukuDAO;
import dao.BukuDAOImpl;
import dao.PembelianDAO;
import dao.PembelianDAOImpl;
import dao.SupplierDAO;
import dao.SupplierDAOImpl;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import model.Buku;
import model.DetailPembelian;
import model.Pembelian;
import model.Supplier;
import org.kordamp.ikonli.materialdesign2.MaterialDesignC;
import org.kordamp.ikonli.materialdesign2.MaterialDesignD;
import org.kordamp.ikonli.materialdesign2.MaterialDesignM;
import org.kordamp.ikonli.swing.FontIcon;
import util.ComboItem;
import util.NeoBrutalTheme;
import util.NeoShadowBorder;
import util.NotaGenerator;
import util.Sesi;
import util.Validasi;

public class FormPembelian extends JPanel {

    private final BukuDAO bukuDAO = new BukuDAOImpl();
    private final SupplierDAO supplierDAO = new SupplierDAOImpl();
    private final PembelianDAO pembelianDAO = new PembelianDAOImpl();

    private static class BeliRow {
        Buku buku;
        int qty;
        double hargaBeli;
    }
    private final List<BeliRow> items = new ArrayList<>();
    private List<Buku> hasilCari = new ArrayList<>();

    private JComboBox<ComboItem> cmbSupplier;
    private JTextField txtCari;
    private JTable tblHasil;
    private DefaultTableModel modelHasil;
    private JTextField txtQty;
    private JTextField txtHargaBeli;
    private JTable tblItem;
    private DefaultTableModel modelItem;
    private JLabel lblTotal;

    public FormPembelian() {
        setLayout(new BorderLayout(12, 12));
        setBackground(NeoBrutalTheme.BG);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JLabel lblJudul = new JLabel("Pembelian");
        lblJudul.setFont(new Font("Segoe UI Black", Font.BOLD, 22));
        add(lblJudul, BorderLayout.NORTH);

        JPanel cols = new JPanel(new GridLayout(1, 2, 12, 0));
        cols.setBackground(NeoBrutalTheme.BG);
        cols.add(buildKiri());
        cols.add(buildKanan());
        add(cols, BorderLayout.CENTER);

        reloadSupplier();
    }

    private JPanel buildKiri() {
        JPanel card = new JPanel(new BorderLayout(8, 8));
        card.setBackground(NeoBrutalTheme.SURFACE);
        card.setBorder(new NeoShadowBorder());

        JPanel pnlTop = new JPanel(new GridLayout(0, 1, 4, 4));
        pnlTop.setBackground(NeoBrutalTheme.SURFACE);
        pnlTop.add(new JLabel("Supplier"));
        cmbSupplier = new JComboBox<>();
        cmbSupplier.setBackground(NeoBrutalTheme.SURFACE);
        cmbSupplier.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        pnlTop.add(cmbSupplier);
        pnlTop.add(new JLabel("Cari Buku"));
        JPanel pnlCari = new JPanel(new BorderLayout(6, 0));
        pnlCari.setBackground(NeoBrutalTheme.SURFACE);
        txtCari = new JTextField();
        txtCari.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        txtCari.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { cariBuku(txtCari.getText()); }
            @Override public void removeUpdate(DocumentEvent e) { cariBuku(txtCari.getText()); }
            @Override public void changedUpdate(DocumentEvent e) { cariBuku(txtCari.getText()); }
        });
        JButton btnCari = styledButton("Cari",
                FontIcon.of(MaterialDesignM.MAGNIFY, 18, Color.BLACK),
                NeoBrutalTheme.SECONDARY);
        btnCari.addActionListener(e -> cariBuku(txtCari.getText()));
        pnlCari.add(txtCari, BorderLayout.CENTER);
        pnlCari.add(btnCari, BorderLayout.EAST);
        pnlTop.add(pnlCari);
        card.add(pnlTop, BorderLayout.NORTH);

        modelHasil = new DefaultTableModel(new String[]{"Kode", "Judul", "Harga Beli", "Stok"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblHasil = new JTable(modelHasil);
        JScrollPane scroll = new JScrollPane(tblHasil);
        scroll.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        card.add(scroll, BorderLayout.CENTER);

        JPanel pnlTambah = new JPanel(new GridLayout(0, 2, 6, 6));
        pnlTambah.setBackground(NeoBrutalTheme.SURFACE);
        txtQty = new JTextField();
        txtQty.setName("qty");
        txtQty.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        txtHargaBeli = new JTextField();
        txtHargaBeli.setName("harga_beli");
        txtHargaBeli.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        pnlTambah.add(new JLabel("Qty"));
        pnlTambah.add(new JLabel("Harga Beli"));
        pnlTambah.add(txtQty);
        pnlTambah.add(txtHargaBeli);
        JButton btnTambah = styledButton("Tambah", null, NeoBrutalTheme.SUCCESS);
        btnTambah.addActionListener(e -> tambahItem());

        JPanel pnlBawah = new JPanel(new BorderLayout(6, 6));
        pnlBawah.setBackground(NeoBrutalTheme.SURFACE);
        pnlBawah.add(pnlTambah, BorderLayout.CENTER);
        pnlBawah.add(btnTambah, BorderLayout.SOUTH);
        card.add(pnlBawah, BorderLayout.SOUTH);
        return card;
    }

    private JPanel buildKanan() {
        JPanel card = new JPanel(new BorderLayout(8, 8));
        card.setBackground(NeoBrutalTheme.SURFACE);
        card.setBorder(new NeoShadowBorder());

        JLabel lbl = new JLabel("Item Pembelian");
        lbl.setFont(new Font("Segoe UI Semibold", Font.BOLD, 14));
        card.add(lbl, BorderLayout.NORTH);

        modelItem = new DefaultTableModel(new String[]{"Kode", "Judul", "Qty", "Harga Beli", "Subtotal"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblItem = new JTable(modelItem);
        JScrollPane scroll = new JScrollPane(tblItem);
        scroll.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        card.add(scroll, BorderLayout.CENTER);

        JPanel pnlBawah = new JPanel(new BorderLayout(6, 6));
        pnlBawah.setBackground(NeoBrutalTheme.SURFACE);
        lblTotal = new JLabel("Total: 0");
        lblTotal.setFont(new Font("Segoe UI Black", Font.BOLD, 20));
        lblTotal.setForeground(NeoBrutalTheme.PRIMARY);
        pnlBawah.add(lblTotal, BorderLayout.NORTH);

        JPanel pnlBtn = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pnlBtn.setBackground(NeoBrutalTheme.SURFACE);
        JButton btnHapus = styledButton("Hapus",
                FontIcon.of(MaterialDesignD.DELETE, 18, Color.BLACK),
                NeoBrutalTheme.DANGER);
        btnHapus.addActionListener(e -> hapusItem());
        JButton btnSimpan = styledButton("Simpan",
                FontIcon.of(MaterialDesignC.CONTENT_SAVE, 18, Color.BLACK),
                NeoBrutalTheme.PRIMARY);
        btnSimpan.addActionListener(e -> doSimpan());
        pnlBtn.add(btnHapus);
        pnlBtn.add(btnSimpan);
        pnlBawah.add(pnlBtn, BorderLayout.SOUTH);
        card.add(pnlBawah, BorderLayout.SOUTH);
        return card;
    }

    private JButton styledButton(String teks, FontIcon ikon, Color bg) {
        JButton b = ikon == null ? new JButton(teks) : new JButton(teks, ikon);
        b.setIconTextGap(8);
        b.setFont(new Font("Segoe UI Semibold", Font.BOLD, 13));
        b.setBackground(bg);
        b.setForeground(Color.BLACK);
        b.setBorder(new NeoShadowBorder());
        b.setFocusPainted(false);
        b.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                b.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                b.setBorder(new NeoShadowBorder());
            }
        });
        return b;
    }

    private void reloadSupplier() {
        cmbSupplier.removeAllItems();
        cmbSupplier.addItem(ComboItem.EMPTY);
        for (Supplier s : supplierDAO.getAll()) {
            cmbSupplier.addItem(new ComboItem(s.getIdSupplier(), s.getNamaSupplier()));
        }
    }

    private void cariBuku(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            hasilCari = bukuDAO.getAll();
        } else {
            hasilCari = bukuDAO.search(keyword.trim());
        }
        modelHasil.setRowCount(0);
        for (Buku b : hasilCari) {
            modelHasil.addRow(new Object[]{b.getKodeBuku(), b.getJudul(), b.getHargaBeli(), b.getStok()});
        }
    }

    private void tambahItem() {
        int row = tblHasil.getSelectedRow();
        if (row < 0 || row >= hasilCari.size()) {
            JOptionPane.showMessageDialog(this, "Pilih buku dulu dari hasil pencarian");
            return;
        }
        if (!Validasi.integer(txtQty)) return;
        if (!Validasi.angka(txtHargaBeli)) return;
        int qty = Integer.parseInt(txtQty.getText().trim());
        double harga = Double.parseDouble(txtHargaBeli.getText().trim());
        if (qty <= 0) {
            JOptionPane.showMessageDialog(this, "Qty harus lebih dari 0",
                    "Validasi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        Buku b = hasilCari.get(row);
        for (BeliRow r : items) {
            if (r.buku.getIdBuku() == b.getIdBuku()) {
                r.qty += qty;
                r.hargaBeli = harga;
                refreshItem();
                return;
            }
        }
        BeliRow r = new BeliRow();
        r.buku = b;
        r.qty = qty;
        r.hargaBeli = harga;
        items.add(r);
        refreshItem();
    }

    private void hapusItem() {
        int row = tblItem.getSelectedRow();
        if (row < 0 || row >= items.size()) {
            JOptionPane.showMessageDialog(this, "Pilih baris item dulu");
            return;
        }
        items.remove(row);
        refreshItem();
    }

    private double hitungTotal() {
        double total = 0;
        for (BeliRow r : items) {
            total += r.qty * r.hargaBeli;
        }
        return total;
    }

    private void refreshItem() {
        modelItem.setRowCount(0);
        for (BeliRow r : items) {
            modelItem.addRow(new Object[]{
                    r.buku.getKodeBuku(), r.buku.getJudul(), r.qty, r.hargaBeli,
                    r.qty * r.hargaBeli
            });
        }
        lblTotal.setText("Total: " + hitungTotal());
    }

    private void doSimpan() {
        if (Sesi.userLogin == null) {
            JOptionPane.showMessageDialog(this, "Sesi habis, login ulang",
                    "Sesi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        ComboItem sup = (ComboItem) cmbSupplier.getSelectedItem();
        if (sup == null || sup.id < 0) {
            JOptionPane.showMessageDialog(this, "Pilih supplier dulu",
                    "Validasi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (items.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Item pembelian masih kosong",
                    "Validasi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            String noFaktur = NotaGenerator.next("FB", "pembelian", "no_faktur");
            Pembelian h = new Pembelian();
            h.setNoFaktur(noFaktur);
            h.setTanggal(LocalDateTime.now());
            h.setIdSupplier(sup.id);
            h.setIdUser(Sesi.userLogin.getIdUser());
            h.setTotal(hitungTotal());
            List<DetailPembelian> details = new ArrayList<>();
            for (BeliRow r : items) {
                DetailPembelian d = new DetailPembelian();
                d.setIdBuku(r.buku.getIdBuku());
                d.setKodeBuku(r.buku.getKodeBuku());
                d.setJudul(r.buku.getJudul());
                d.setQty(r.qty);
                d.setHargaBeli(r.hargaBeli);
                d.setSubtotal(r.qty * r.hargaBeli);
                details.add(d);
            }
            pembelianDAO.saveWithDetail(h, details);
            JOptionPane.showMessageDialog(this, "Pembelian tersimpan, stok bertambah");
            items.clear();
            txtQty.setText("");
            txtHargaBeli.setText("");
            refreshItem();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal simpan pembelian: " + ex.getMessage(),
                    "Gagal", JOptionPane.ERROR_MESSAGE);
        }
    }
}
