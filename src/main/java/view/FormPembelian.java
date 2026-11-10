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
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import model.Buku;
import model.DetailPembelian;
import model.Pembelian;
import model.Supplier;
import org.kordamp.ikonli.materialdesign2.MaterialDesignC;
import org.kordamp.ikonli.materialdesign2.MaterialDesignM;
import org.kordamp.ikonli.swing.FontIcon;
import util.ComboItem;
import util.NeoBrutalTheme;
import util.NotaGenerator;
import util.Sesi;
import util.Validasi;

public class FormPembelian extends JPanel {

    private static final NumberFormat FMT_RP = NumberFormat.getInstance(new Locale("id", "ID"));

    private final BukuDAO bukuDAO = new BukuDAOImpl();
    private final SupplierDAO supplierDAO = new SupplierDAOImpl();
    private final PembelianDAO pembelianDAO = new PembelianDAOImpl();

    private static class BeliRow {
        Buku buku;
        int qty;
        double hargaBeli;
    }
    private final List<BeliRow> items = new ArrayList<>();
    private final List<Supplier> listSupplier = new ArrayList<>();
    private List<Buku> hasilCari = new ArrayList<>();

    private JLabel lblFakturDraft;
    private JComboBox<ComboItem> cmbSupplier;
    private JLabel lblSupplierInfo;
    private JTextField txtCari;
    private JTable tblHasil;
    private DefaultTableModel modelHasil;
    private JTextField txtQty;
    private JTextField txtHargaBeli;
    private JTable tblItem;
    private DefaultTableModel modelItem;
    private JLabel lblSummaryItem;
    private JLabel lblTotal;

    public FormPembelian() {
        setLayout(new BorderLayout(12, 12));
        setBackground(NeoBrutalTheme.BG);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel pnlNorth = new JPanel(new BorderLayout(0, 6));
        pnlNorth.setBackground(NeoBrutalTheme.BG);

        lblFakturDraft = new JLabel("Draft No. Faktur: -");
        lblFakturDraft.setFont(NeoBrutalTheme.mono(10));
        lblFakturDraft.setForeground(NeoBrutalTheme.TEXT_MUTED);
        JPanel pnlDraft = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        pnlDraft.setOpaque(false);
        pnlDraft.add(lblFakturDraft);

        pnlNorth.add(new NeoPageHeader("TRANSAKSI", "Pembelian & Pengadaan",
                "Pilih supplier â†’ isi qty & harga beli â†’ tambah â†’ simpan faktur.", pnlDraft),
                BorderLayout.NORTH);
        add(pnlNorth, BorderLayout.NORTH);

        JPanel cols = new JPanel(new GridLayout(1, 2, 12, 0));
        cols.setBackground(NeoBrutalTheme.BG);
        cols.add(buildKiri());
        cols.add(buildKanan());
        add(cols, BorderLayout.CENTER);

        reloadSupplier();
        cariBuku("");
        updateDraftFaktur();
    }

    private JPanel buildKiri() {
        JPanel card = NeoFormStyle.card(new BorderLayout(8, 8));

        JPanel pnlTop = new JPanel(new GridBagLayout());
        pnlTop.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 0, 2, 0);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        pnlTop.add(NeoFormStyle.label("Pilih Supplier"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        cmbSupplier = NeoFormStyle.combo();
        cmbSupplier.addActionListener(e -> updateSupplierInfo());
        pnlTop.add(cmbSupplier, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        lblSupplierInfo = new JLabel("Pilih supplier untuk melihat alamat & kontak.");
        lblSupplierInfo.setFont(NeoBrutalTheme.body(java.awt.Font.ITALIC, 10));
        lblSupplierInfo.setForeground(NeoBrutalTheme.TEXT_MUTED);
        pnlTop.add(lblSupplierInfo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        pnlTop.add(NeoFormStyle.label("Cari Barang"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        JPanel pnlCari = new JPanel(new BorderLayout(6, 0));
        pnlCari.setOpaque(false);
        txtCari = NeoFormStyle.field("cari");
        txtCari.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { cariBuku(txtCari.getText()); }
            @Override public void removeUpdate(DocumentEvent e) { cariBuku(txtCari.getText()); }
            @Override public void changedUpdate(DocumentEvent e) { cariBuku(txtCari.getText()); }
        });
        JButton btnCari = styledButton("Cari",
                FontIcon.of(MaterialDesignM.MAGNIFY, 16, NeoBrutalTheme.INK),
                NeoBrutalTheme.SECONDARY);
        btnCari.addActionListener(e -> cariBuku(txtCari.getText()));
        pnlCari.add(txtCari, BorderLayout.CENTER);
        pnlCari.add(btnCari, BorderLayout.EAST);
        pnlTop.add(pnlCari, gbc);
        card.add(pnlTop, BorderLayout.NORTH);

        modelHasil = new DefaultTableModel(new String[]{"Kode", "Judul", "Harga Beli", "Stok Toko"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblHasil = new JTable(modelHasil);
        tblHasil.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tblHasil.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) isiHargaBeliOtomatis();
        });
        NeoTableStyle.apply(tblHasil);
        JScrollPane scroll = NeoTableStyle.wrap(tblHasil);
        card.add(scroll, BorderLayout.CENTER);

        JPanel pnlTambah = new JPanel(new GridLayout(0, 2, 4, 4));
        pnlTambah.setOpaque(false);
        txtQty = NeoFormStyle.field("qty");
        txtQty.setText("1");
        txtHargaBeli = NeoFormStyle.field("harga_beli");
        pnlTambah.add(NeoFormStyle.label("Qty Beli"));
        pnlTambah.add(NeoFormStyle.label("Harga Beli Satuan"));
        pnlTambah.add(txtQty);
        pnlTambah.add(txtHargaBeli);

        JButton btnTambah = styledButton("Tambah ke Faktur",
                FontIcon.of(MaterialDesignC.CART_ARROW_DOWN, 16, NeoBrutalTheme.INK),
                NeoBrutalTheme.SUCCESS);
        btnTambah.addActionListener(e -> tambahItem());

        JPanel pnlBawah = new JPanel(new BorderLayout(6, 6));
        pnlBawah.setOpaque(false);
        pnlBawah.add(pnlTambah, BorderLayout.CENTER);
        pnlBawah.add(btnTambah, BorderLayout.SOUTH);
        card.add(pnlBawah, BorderLayout.SOUTH);
        return card;
    }

    private JPanel buildKanan() {
        JPanel card = NeoFormStyle.card(new BorderLayout(8, 8));
        card.add(NeoFormStyle.section("Daftar Barang Masuk"), BorderLayout.NORTH);

        modelItem = new DefaultTableModel(
                new String[]{"Kode", "Judul", "Jenis", "Stok Lama", "Qty Beli", "Stok Baru", "Harga Beli", "Subtotal"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblItem = new JTable(modelItem);
        NeoTableStyle.apply(tblItem);
        tblItem.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        int[] lebar = {55, 130, 50, 62, 55, 58, 72, 82};
        for (int i = 0; i < lebar.length; i++) {
            tblItem.getColumnModel().getColumn(i).setPreferredWidth(lebar[i]);
        }
        JScrollPane scroll = NeoTableStyle.wrap(tblItem);
        card.add(scroll, BorderLayout.CENTER);

        JPanel pnlBawah = new JPanel(new BorderLayout(6, 6));
        pnlBawah.setOpaque(false);

        JPanel pnlMetrics = new JPanel(new GridLayout(2, 1, 1, 1));
        pnlMetrics.setOpaque(false);
        lblSummaryItem = new JLabel("Total: 0 jenis buku | 0 pcs");
        lblSummaryItem.setFont(NeoBrutalTheme.body(java.awt.Font.PLAIN, 11));
        lblSummaryItem.setForeground(NeoBrutalTheme.TEXT_MUTED);
        lblTotal = new JLabel("Total Biaya: Rp 0");
        lblTotal.setFont(NeoBrutalTheme.display(18));
        lblTotal.setForeground(NeoBrutalTheme.BRAND_MAROON);
        pnlMetrics.add(lblSummaryItem);
        pnlMetrics.add(lblTotal);
        pnlBawah.add(pnlMetrics, BorderLayout.NORTH);

        JPanel pnlBtn = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        pnlBtn.setOpaque(false);

        JButton btnReset = styledButton("Reset", null, NeoBrutalTheme.SECONDARY);
        btnReset.addActionListener(e -> doResetKeranjang());

        JButton btnHapus = styledButton("Hapus", null, NeoBrutalTheme.DANGER);
        btnHapus.addActionListener(e -> hapusItem());

        JButton btnSimpan = styledButton("Simpan",
                FontIcon.of(MaterialDesignC.CONTENT_SAVE, 16, NeoBrutalTheme.INK),
                NeoBrutalTheme.PRIMARY);
        btnSimpan.addActionListener(e -> doSimpan());

        pnlBtn.add(btnReset);
        pnlBtn.add(btnHapus);
        pnlBtn.add(btnSimpan);
        pnlBawah.add(pnlBtn, BorderLayout.SOUTH);
        card.add(pnlBawah, BorderLayout.SOUTH);
        return card;
    }

    private NeoButton styledButton(String teks, FontIcon ikon, Color bg) {
        return NeoFormStyle.button(teks, ikon, bg);
    }

    private void updateDraftFaktur() {
        try {
            String noFaktur = NotaGenerator.next("FB", "pembelian", "no_faktur");
            lblFakturDraft.setText("Draft No. Faktur: " + noFaktur);
        } catch (Exception ex) {
            lblFakturDraft.setText("Draft No. Faktur: FB-AUTO");
        }
    }

    private void reloadSupplier() {
        cmbSupplier.removeAllItems();
        listSupplier.clear();
        cmbSupplier.addItem(ComboItem.EMPTY);
        listSupplier.addAll(supplierDAO.getAll());
        for (Supplier s : listSupplier) {
            cmbSupplier.addItem(new ComboItem(s.getIdSupplier(), s.getNamaSupplier()));
        }
    }

    private void updateSupplierInfo() {
        ComboItem selected = (ComboItem) cmbSupplier.getSelectedItem();
        if (selected == null || selected.id < 0) {
            lblSupplierInfo.setText("Pilih supplier untuk melihat alamat & kontak.");
            return;
        }
        for (Supplier s : listSupplier) {
            if (s.getIdSupplier() == selected.id) {
                String almt = (s.getAlamat() != null && !s.getAlamat().isBlank()) ? s.getAlamat() : "-";
                String telp = (s.getNoTelp() != null && !s.getNoTelp().isBlank()) ? s.getNoTelp() : "-";
                lblSupplierInfo.setText("<html>ðŸ“ <b>Alamat:</b> " + almt + " &nbsp;|&nbsp; ðŸ“ž <b>Telp:</b> " + telp + "</html>");
                return;
            }
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
            modelHasil.addRow(new Object[]{
                    b.getKodeBuku(),
                    b.getJudul(),
                    formatRp(b.getHargaBeli()),
                    b.getStok()
            });
        }
    }

    private void isiHargaBeliOtomatis() {
        int row = tblHasil.getSelectedRow();
        if (row < 0 || row >= hasilCari.size()) return;
        Buku b = hasilCari.get(row);
        txtHargaBeli.setText(String.valueOf((long) Math.round(b.getHargaBeli())));
        if (txtQty.getText().trim().isEmpty() || "0".equals(txtQty.getText().trim())) {
            txtQty.setText("1");
        }
    }

    private void tambahItem() {
        int row = tblHasil.getSelectedRow();
        if (row < 0 || row >= hasilCari.size()) {
            JOptionPane.showMessageDialog(this, "Pilih buku terlebih dahulu dari tabel katalog",
                    "Validasi", JOptionPane.WARNING_MESSAGE);
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
            JOptionPane.showMessageDialog(this, "Pilih baris item yang ingin dihapus");
            return;
        }
        items.remove(row);
        refreshItem();
    }

    private void doResetKeranjang() {
        if (items.isEmpty()) return;
        int opt = JOptionPane.showConfirmDialog(this,
                "Kosongkan semua item di keranjang faktur?",
                "Konfirmasi Reset", JOptionPane.YES_NO_OPTION);
        if (opt == JOptionPane.YES_OPTION) {
            items.clear();
            txtQty.setText("1");
            txtHargaBeli.setText("");
            refreshItem();
        }
    }

    private double hitungTotal() {
        double total = 0;
        for (BeliRow r : items) {
            total += r.qty * r.hargaBeli;
        }
        return total;
    }

    private int hitungTotalQty() {
        int total = 0;
        for (BeliRow r : items) {
            total += r.qty;
        }
        return total;
    }

    private void refreshItem() {
        modelItem.setRowCount(0);
        for (BeliRow r : items) {
            int stokLama = r.buku.getStok();
            int stokBaru = stokLama + r.qty;
            modelItem.addRow(new Object[]{
                    r.buku.getKodeBuku(),
                    r.buku.getJudul(),
                    r.buku.getJenis() != null && !r.buku.getJenis().isBlank() ? r.buku.getJenis() : "Buku",
                    stokLama,
                    r.qty,
                    stokBaru,
                    formatRp(r.hargaBeli),
                    formatRp(r.qty * r.hargaBeli)
            });
        }
        lblSummaryItem.setText("Total: " + items.size() + " jenis buku | " + hitungTotalQty() + " pcs");
        lblTotal.setText("Total Biaya: " + formatRp(hitungTotal()));
    }

    private void doSimpan() {
        if (Sesi.userLogin == null) {
            JOptionPane.showMessageDialog(this, "Sesi habis, login ulang",
                    "Sesi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        ComboItem sup = (ComboItem) cmbSupplier.getSelectedItem();
        if (sup == null || sup.id < 0) {
            JOptionPane.showMessageDialog(this, "Pilih supplier vendor terlebih dahulu",
                    "Validasi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (items.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Keranjang faktur pembelian masih kosong",
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

            // Cari info supplier untuk cetak faktur
            String supNama = sup.label;
            String supAlamat = "-";
            String supTelp = "-";
            for (Supplier s : listSupplier) {
                if (s.getIdSupplier() == sup.id) {
                    if (s.getAlamat() != null) supAlamat = s.getAlamat();
                    if (s.getNoTelp() != null) supTelp = s.getNoTelp();
                    break;
                }
            }

            // Tampilkan dialog bukti penerimaan barang masuk
            Frame owner = (Frame) SwingUtilities.getWindowAncestor(this);
            FakturPembelianDialog dlg = new FakturPembelianDialog(owner, h, details, supNama, supAlamat, supTelp, Sesi.userLogin.getNamaLengkap());
            dlg.setVisible(true);

            // Bersihkan keranjang dan segarkan tampilan katalog
            items.clear();
            txtQty.setText("1");
            txtHargaBeli.setText("");
            refreshItem();
            cariBuku(txtCari.getText());
            updateDraftFaktur();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal simpan pembelian: " + ex.getMessage(),
                    "Gagal", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String formatRp(double val) {
        return "Rp " + FMT_RP.format((long) Math.round(val));
    }
}
