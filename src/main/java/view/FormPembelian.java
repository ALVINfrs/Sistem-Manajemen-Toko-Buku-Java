package view;

import com.github.lgooddatepicker.components.DatePicker;
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
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import model.Buku;
import model.DetailPembelian;
import model.LapPembelian;
import model.Pembelian;
import model.Supplier;
import org.kordamp.ikonli.materialdesign2.MaterialDesignC;
import org.kordamp.ikonli.materialdesign2.MaterialDesignF;
import org.kordamp.ikonli.materialdesign2.MaterialDesignH;
import org.kordamp.ikonli.materialdesign2.MaterialDesignM;
import org.kordamp.ikonli.materialdesign2.MaterialDesignR;
import org.kordamp.ikonli.swing.FontIcon;
import util.ComboItem;
import util.NeoBrutalTheme;
import util.NotaGenerator;
import util.Sesi;
import util.Validasi;

public class FormPembelian extends JPanel {

    private static final NumberFormat FMT_RP = NumberFormat.getInstance(new Locale("id", "ID"));
    private static final DateTimeFormatter FMT_TGL_RIWAYAT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

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

    private JTabbedPane tabbedPane;
    private DatePicker dpDari;
    private DatePicker dpSampai;
    private JTextField txtCariRiwayat;
    private JTable tblRiwayat;
    private DefaultTableModel modelRiwayat;
    private JLabel lblRiwayatSummary;
    private List<LapPembelian> cacheRiwayat = new ArrayList<>();

    public FormPembelian() {
        setLayout(new BorderLayout(12, 12));
        setBackground(NeoBrutalTheme.BG);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel pnlNorth = new JPanel(new BorderLayout(0, 6));
        pnlNorth.setBackground(NeoBrutalTheme.BG);

        lblFakturDraft = new JLabel("Draft No. Faktur: -");
        lblFakturDraft.setName("lblFakturDraft");
        lblFakturDraft.setFont(NeoBrutalTheme.mono(10));
        lblFakturDraft.setForeground(NeoBrutalTheme.TEXT_MUTED);
        JPanel pnlDraft = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        pnlDraft.setOpaque(false);
        pnlDraft.add(lblFakturDraft);

        pnlNorth.add(new NeoPageHeader("TRANSAKSI", "Pembelian & Pengadaan",
                "Pilih supplier → isi qty & harga beli → tambah → simpan faktur.", pnlDraft),
                BorderLayout.NORTH);
        add(pnlNorth, BorderLayout.NORTH);

        tabbedPane = new JTabbedPane();
        tabbedPane.setName("tabPembelian");
        tabbedPane.setFont(NeoBrutalTheme.display(12));
        tabbedPane.setBackground(NeoBrutalTheme.BG);
        tabbedPane.setForeground(NeoBrutalTheme.INK);
        tabbedPane.addTab("Input Pembelian",
                FontIcon.of(MaterialDesignC.CART_ARROW_DOWN, 16, NeoBrutalTheme.INK),
                buildTabInput());
        tabbedPane.addTab("Riwayat Faktur",
                FontIcon.of(MaterialDesignH.HISTORY, 16, NeoBrutalTheme.INK),
                buildTabRiwayat());
        tabbedPane.addChangeListener(e -> {
            boolean input = tabbedPane.getSelectedIndex() == 0;
            lblFakturDraft.setVisible(input);
            if (!input) {
                loadRiwayat();
            }
        });
        add(tabbedPane, BorderLayout.CENTER);

        reloadSupplier();
        cariBuku("");
        updateDraftFaktur();
        loadRiwayat();
    }

    private JPanel buildTabInput() {
        JPanel pnl = new JPanel(new GridLayout(1, 2, 12, 0));
        pnl.setBackground(NeoBrutalTheme.BG);
        pnl.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        pnl.add(buildKiri());
        pnl.add(buildKanan());
        return pnl;
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
                new String[]{"Kode", "Judul", "Jenis", "Stok Awal", "Qty Beli", "Stok Akhir", "Harga", "Subtotal"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblItem = new JTable(modelItem);
        NeoTableStyle.apply(tblItem);
        tblItem.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        int[] lebar = {50, 110, 48, 72, 66, 78, 64, 64};
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

    private JPanel buildTabRiwayat() {
        JPanel pnl = new JPanel(new BorderLayout(10, 10));
        pnl.setBackground(NeoBrutalTheme.BG);
        pnl.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JPanel card = NeoFormStyle.card(new BorderLayout(8, 8));

        JPanel pnlHeader = new JPanel(new BorderLayout(6, 6));
        pnlHeader.setOpaque(false);
        pnlHeader.add(NeoFormStyle.section("Riwayat Faktur Pembelian"), BorderLayout.NORTH);

        JPanel pnlFilter = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        pnlFilter.setOpaque(false);

        pnlFilter.add(NeoFormStyle.label("Dari"));
        dpDari = new DatePicker();
        dpDari.setName("tglDariPembelian");
        dpDari.setDate(LocalDate.now().minusDays(30));
        dpDari.addDateChangeListener(e -> loadRiwayat());
        pnlFilter.add(dpDari);

        pnlFilter.add(NeoFormStyle.label("Sampai"));
        dpSampai = new DatePicker();
        dpSampai.setName("tglSampaiPembelian");
        dpSampai.setDateToToday();
        dpSampai.addDateChangeListener(e -> loadRiwayat());
        pnlFilter.add(dpSampai);

        pnlFilter.add(NeoFormStyle.label("Cari"));
        txtCariRiwayat = NeoFormStyle.field("cari_riwayat_pembelian");
        txtCariRiwayat.setColumns(18);
        txtCariRiwayat.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                applyFilterRiwayat();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                applyFilterRiwayat();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                applyFilterRiwayat();
            }
        });
        pnlFilter.add(txtCariRiwayat);
        pnlHeader.add(pnlFilter, BorderLayout.SOUTH);
        card.add(pnlHeader, BorderLayout.NORTH);

        modelRiwayat = new DefaultTableModel(
                new String[]{"No. Faktur", "Tanggal", "Supplier", "Penerima", "Total"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblRiwayat = new JTable(modelRiwayat);
        tblRiwayat.setName("tblRiwayatPembelian");
        tblRiwayat.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        NeoTableStyle.apply(tblRiwayat);
        tblRiwayat.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        tblRiwayat.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    bukaFaktur();
                }
            }
        });
        tblRiwayat.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "bukaFaktur");
        tblRiwayat.getActionMap().put("bukaFaktur", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                bukaFaktur();
            }
        });
        card.add(NeoTableStyle.wrap(tblRiwayat), BorderLayout.CENTER);

        JPanel pnlBottom = new JPanel(new BorderLayout(8, 0));
        pnlBottom.setOpaque(false);
        lblRiwayatSummary = new JLabel("Memuat riwayat faktur...");
        lblRiwayatSummary.setFont(NeoBrutalTheme.body(Font.PLAIN, 11));
        lblRiwayatSummary.setForeground(NeoBrutalTheme.TEXT_MUTED);
        pnlBottom.add(lblRiwayatSummary, BorderLayout.WEST);

        JPanel pnlBtn = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        pnlBtn.setOpaque(false);

        JButton btnSegarkan = styledButton("Segarkan",
                FontIcon.of(MaterialDesignR.REFRESH, 16, NeoBrutalTheme.INK),
                NeoBrutalTheme.SECONDARY);
        btnSegarkan.setName("btnSegarkanRiwayat");
        btnSegarkan.addActionListener(e -> loadRiwayat());

        JButton btnBuka = styledButton("Buka Faktur",
                FontIcon.of(MaterialDesignF.FILE_DOCUMENT, 16, NeoBrutalTheme.INK),
                NeoBrutalTheme.PRIMARY);
        btnBuka.setName("btnBukaFaktur");
        btnBuka.addActionListener(e -> bukaFaktur());

        pnlBtn.add(btnSegarkan);
        pnlBtn.add(btnBuka);
        pnlBottom.add(pnlBtn, BorderLayout.EAST);
        card.add(pnlBottom, BorderLayout.SOUTH);

        pnl.add(card, BorderLayout.CENTER);
        return pnl;
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
                lblSupplierInfo.setText("<html><b>Alamat:</b> " + almt
                        + " &nbsp;|&nbsp; <b>Telp:</b> " + telp + "</html>");
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
            loadRiwayat();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal simpan pembelian: " + ex.getMessage(),
                    "Gagal", JOptionPane.ERROR_MESSAGE);
        }
    }

    // =========================================================================
    // LOGIKA TAB RIWAYAT FAKTUR
    // =========================================================================

    static List<LapPembelian> filterRiwayat(List<LapPembelian> data, String keyword) {
        List<LapPembelian> hasil = new ArrayList<>();
        if (data == null) {
            return hasil;
        }
        String key = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        for (LapPembelian l : data) {
            if (l == null) {
                continue;
            }
            boolean cocok = key.isEmpty()
                    || (l.getNoFaktur() != null
                        && l.getNoFaktur().toLowerCase(Locale.ROOT).contains(key))
                    || (l.getSupplier() != null
                        && l.getSupplier().toLowerCase(Locale.ROOT).contains(key));
            if (cocok) {
                hasil.add(l);
            }
        }
        return hasil;
    }

    static Object[] riwayatRow(LapPembelian l) {
        String tgl = l.getTanggal() != null ? l.getTanggal().format(FMT_TGL_RIWAYAT) : "-";
        return new Object[]{
            l.getNoFaktur() != null ? l.getNoFaktur() : "-",
            tgl,
            l.getSupplier() != null ? l.getSupplier() : "-",
            l.getKasir() != null ? l.getKasir() : "-",
            "Rp " + FMT_RP.format((long) Math.round(l.getTotal()))
        };
    }

    public void loadRiwayat() {
        if (dpDari == null || dpSampai == null || modelRiwayat == null) {
            return;
        }
        LocalDate a = dpDari.getDate();
        LocalDate b = dpSampai.getDate();
        if (a == null || b == null) {
            return;
        }
        try {
            cacheRiwayat = pembelianDAO.lapPembelian(a, b);
            cacheRiwayat.sort(Comparator.comparing(
                    LapPembelian::getTanggal,
                    Comparator.nullsLast(Comparator.naturalOrder())).reversed());
            applyFilterRiwayat();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Gagal memuat riwayat faktur: " + ex.getMessage(),
                    "Gagal", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void applyFilterRiwayat() {
        if (modelRiwayat == null) {
            return;
        }
        List<LapPembelian> tampil = filterRiwayat(cacheRiwayat,
                txtCariRiwayat != null ? txtCariRiwayat.getText() : "");
        modelRiwayat.setRowCount(0);
        double total = 0;
        for (LapPembelian l : tampil) {
            modelRiwayat.addRow(riwayatRow(l));
            total += l.getTotal();
        }
        if (lblRiwayatSummary != null) {
            lblRiwayatSummary.setText(tampil.size() + " faktur | Total " + formatRp(total));
        }
    }

    private void bukaFaktur() {
        int row = tblRiwayat.getSelectedRow();
        if (row < 0 || row >= modelRiwayat.getRowCount()) {
            JOptionPane.showMessageDialog(this, "Pilih baris faktur dulu",
                    "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String noFaktur = String.valueOf(modelRiwayat.getValueAt(row, 0));
        String penerima = String.valueOf(modelRiwayat.getValueAt(row, 3));
        try {
            Pembelian h = null;
            for (Pembelian p : pembelianDAO.search(noFaktur)) {
                if (p.getNoFaktur() != null
                        && p.getNoFaktur().equalsIgnoreCase(noFaktur)) {
                    h = pembelianDAO.getById(p.getIdPembelian());
                    break;
                }
            }
            if (h == null) {
                JOptionPane.showMessageDialog(this,
                        "Faktur " + noFaktur + " tidak ditemukan",
                        "Tidak Ditemukan", JOptionPane.ERROR_MESSAGE);
                return;
            }
            List<DetailPembelian> detail = pembelianDAO
                    .getDetailByPembelian(h.getIdPembelian());
            Supplier supplier = supplierDAO.getById(h.getIdSupplier());
            Frame owner = (Frame) SwingUtilities.getWindowAncestor(this);
            new FakturPembelianFormalDialog(owner, h, detail, supplier, penerima)
                    .setVisible(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Gagal buka faktur: " + ex.getMessage(),
                    "Gagal", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String formatRp(double val) {
        return "Rp " + FMT_RP.format((long) Math.round(val));
    }
}
