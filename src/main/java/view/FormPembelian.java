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
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
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
import org.kordamp.ikonli.materialdesign2.MaterialDesignR;
import org.kordamp.ikonli.swing.FontIcon;
import util.ComboItem;
import util.NeoBrutalTheme;
import util.NeoShadowBorder;
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

        JPanel pnlNorth = new JPanel(new BorderLayout(6, 6));
        pnlNorth.setBackground(NeoBrutalTheme.BG);

        JPanel pnlJudulBar = new JPanel(new BorderLayout());
        pnlJudulBar.setBackground(NeoBrutalTheme.BG);
        JLabel lblJudul = new JLabel("Pembelian & Pengadaan Buku");
        lblJudul.setFont(new Font("Segoe UI Black", Font.BOLD, 22));
        pnlJudulBar.add(lblJudul, BorderLayout.WEST);

        lblFakturDraft = new JLabel("Draft No. Faktur: -");
        lblFakturDraft.setFont(new Font("Segoe UI Semibold", Font.BOLD, 13));
        lblFakturDraft.setForeground(new Color(0x33, 0x33, 0x99));
        pnlJudulBar.add(lblFakturDraft, BorderLayout.EAST);
        pnlNorth.add(pnlJudulBar, BorderLayout.NORTH);

        JLabel lblHint = new JLabel("<html><b>Alur Pengadaan:</b> 1) Pilih Rekanan Supplier 2) Pilih buku dari katalog + tentukan Qty & Harga Beli 3) Klik Tambah 4) Klik Simpan Faktur untuk auto-restock & cetak bukti penerimaan barang.</html>");
        lblHint.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblHint.setForeground(Color.BLACK);
        pnlNorth.add(lblHint, BorderLayout.SOUTH);
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
        JPanel card = new JPanel(new BorderLayout(8, 8));
        card.setBackground(NeoBrutalTheme.SURFACE);
        card.setBorder(new NeoShadowBorder());

        JPanel pnlTop = new JPanel(new GridLayout(0, 1, 4, 4));
        pnlTop.setBackground(NeoBrutalTheme.SURFACE);
        pnlTop.add(new JLabel("Pilih Supplier (Vendor Kulakan):"));
        cmbSupplier = new JComboBox<>();
        cmbSupplier.setBackground(NeoBrutalTheme.SURFACE);
        cmbSupplier.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        cmbSupplier.addActionListener(e -> updateSupplierInfo());
        pnlTop.add(cmbSupplier);

        lblSupplierInfo = new JLabel("Pilih supplier untuk melihat alamat & kontak.");
        lblSupplierInfo.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblSupplierInfo.setForeground(new Color(0x55, 0x55, 0x55));
        pnlTop.add(lblSupplierInfo);

        pnlTop.add(new JLabel("Cari Buku di Katalog:"));
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
        JScrollPane scroll = new JScrollPane(tblHasil);
        scroll.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        card.add(scroll, BorderLayout.CENTER);

        JPanel pnlTambah = new JPanel(new GridLayout(0, 2, 6, 6));
        pnlTambah.setBackground(NeoBrutalTheme.SURFACE);
        txtQty = new JTextField("1");
        txtQty.setName("qty");
        txtQty.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        txtHargaBeli = new JTextField();
        txtHargaBeli.setName("harga_beli");
        txtHargaBeli.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        pnlTambah.add(new JLabel("Qty Beli (Pcs):"));
        pnlTambah.add(new JLabel("Harga Beli Satuan (Rp):"));
        pnlTambah.add(txtQty);
        pnlTambah.add(txtHargaBeli);

        JButton btnTambah = styledButton("Tambahkan ke Keranjang Faktur",
                FontIcon.of(MaterialDesignC.CART_ARROW_DOWN, 18, Color.BLACK),
                NeoBrutalTheme.SUCCESS);
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

        JLabel lbl = new JLabel("Daftar Buku Masuk (Keranjang Faktur)");
        lbl.setFont(new Font("Segoe UI Semibold", Font.BOLD, 14));
        card.add(lbl, BorderLayout.NORTH);

        modelItem = new DefaultTableModel(
                new String[]{"Kode", "Judul", "Stok Lama", "Qty Beli", "Stok Baru", "Harga Beli", "Subtotal"}, 0) {
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

        JPanel pnlMetrics = new JPanel(new GridLayout(2, 1, 2, 2));
        pnlMetrics.setBackground(NeoBrutalTheme.SURFACE);
        lblSummaryItem = new JLabel("Total: 0 jenis buku | 0 pcs");
        lblSummaryItem.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 13));
        lblTotal = new JLabel("Total Biaya: Rp 0");
        lblTotal.setFont(new Font("Segoe UI Black", Font.BOLD, 20));
        lblTotal.setForeground(NeoBrutalTheme.PRIMARY);
        pnlMetrics.add(lblSummaryItem);
        pnlMetrics.add(lblTotal);
        pnlBawah.add(pnlMetrics, BorderLayout.NORTH);

        JPanel pnlBtn = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlBtn.setBackground(NeoBrutalTheme.SURFACE);

        JButton btnReset = styledButton("Reset / Kosongkan",
                FontIcon.of(MaterialDesignR.REFRESH, 18, Color.BLACK),
                NeoBrutalTheme.SECONDARY);
        btnReset.addActionListener(e -> doResetKeranjang());

        JButton btnHapus = styledButton("Hapus Baris",
                FontIcon.of(MaterialDesignD.DELETE, 18, Color.BLACK),
                NeoBrutalTheme.DANGER);
        btnHapus.addActionListener(e -> hapusItem());

        JButton btnSimpan = styledButton("Simpan Faktur Pembelian",
                FontIcon.of(MaterialDesignC.CONTENT_SAVE, 18, Color.BLACK),
                NeoBrutalTheme.PRIMARY);
        btnSimpan.addActionListener(e -> doSimpan());

        pnlBtn.add(btnReset);
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
                lblSupplierInfo.setText("<html>📍 <b>Alamat:</b> " + almt + " &nbsp;|&nbsp; 📞 <b>Telp:</b> " + telp + "</html>");
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
