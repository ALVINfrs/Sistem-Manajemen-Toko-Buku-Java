package view;

import dao.PenjualanDAO;
import dao.PenjualanDAOImpl;
import dao.ReturDAO;
import dao.ReturDAOImpl;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import model.DetailPenjualan;
import model.Penjualan;
import model.Retur;
import org.kordamp.ikonli.materialdesign2.MaterialDesignC;
import org.kordamp.ikonli.materialdesign2.MaterialDesignH;
import org.kordamp.ikonli.materialdesign2.MaterialDesignM;
import org.kordamp.ikonli.materialdesign2.MaterialDesignP;
import org.kordamp.ikonli.materialdesign2.MaterialDesignR;
import org.kordamp.ikonli.swing.FontIcon;
import util.NeoBrutalTheme;
import util.NotaGenerator;
import util.Sesi;

public class FormRetur extends JPanel {

    private static final NumberFormat FMT_RP = NumberFormat.getInstance(new Locale("id", "ID"));
    private static final DateTimeFormatter FMT_TGL = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    private final PenjualanDAO penjualanDAO = new PenjualanDAOImpl();
    private final ReturDAO returDAO = new ReturDAOImpl();

    // Data Tab 1 (Input Retur)
    private Penjualan nota;
    private List<DetailPenjualan> detailNota = new ArrayList<>();
    private List<Penjualan> daftarNota = new ArrayList<>();
    private List<Penjualan> daftarTampil = new ArrayList<>();

    // Komponen Tab 1
    private JTextField txtNoNota;
    private JTextField txtFilter;
    private JTable tblNota;
    private DefaultTableModel modelNota;
    private JLabel lblInfoNota;

    private JTable tblItem;
    private DefaultTableModel modelItem;
    private JLabel lblItemPilihan;
    private JTextField txtQty;
    private JTextField txtAlasan;
    private JLabel lblRefundInfo;

    // Data & Komponen Tab 2 (Riwayat Retur)
    private List<Retur> listRiwayat = new ArrayList<>();
    private JTable tblRiwayat;
    private DefaultTableModel modelRiwayat;
    private JTextField txtCariRiwayat;
    private JLabel lblRiwayatSummary;

    private JTabbedPane tabbedPane;

    public FormRetur() {
        setLayout(new BorderLayout(12, 12));
        setBackground(NeoBrutalTheme.BG);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        add(new NeoPageHeader("TRANSAKSI", "Retur Penjualan",
                "Layanan pengembalian barang, refund dana kasir, dan cetak bukti retur.",
                null), BorderLayout.NORTH);

        // Tabbed Pane (Input Retur & Riwayat Retur)
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(NeoBrutalTheme.display(12));
        tabbedPane.setBackground(NeoBrutalTheme.BG);
        tabbedPane.setForeground(NeoBrutalTheme.INK);

        tabbedPane.addTab("Input Retur",
                FontIcon.of(MaterialDesignC.CART_ARROW_DOWN, 16, NeoBrutalTheme.INK),
                buildTabInput());

        tabbedPane.addTab("Riwayat Retur",
                FontIcon.of(MaterialDesignH.HISTORY, 16, NeoBrutalTheme.INK),
                buildTabRiwayat());

        tabbedPane.addChangeListener(e -> {
            if (tabbedPane.getSelectedIndex() == 1) {
                loadRiwayat();
            }
        });

        add(tabbedPane, BorderLayout.CENTER);

        // Inisialisasi data
        loadTable();
        loadRiwayat();
    }

    // =========================================================================
    // TAB 1: INPUT RETUR
    // =========================================================================

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

        // Header Card Kiri
        JPanel pnlHeaderKiri = new JPanel(new BorderLayout(4, 4));
        pnlHeaderKiri.setOpaque(false);
        pnlHeaderKiri.add(NeoFormStyle.section("1. Cari & Pilih Nota"), BorderLayout.NORTH);

        JPanel pnlCari = new JPanel(new BorderLayout(6, 0));
        pnlCari.setOpaque(false);
        txtNoNota = NeoFormStyle.field("no_nota");
        txtNoNota.addActionListener(e -> cariNota());

        JButton btnCari = styledButton("Cari",
                FontIcon.of(MaterialDesignM.MAGNIFY, 16, NeoBrutalTheme.INK),
                NeoBrutalTheme.SECONDARY);
        btnCari.addActionListener(e -> cariNota());
        pnlCari.add(txtNoNota, BorderLayout.CENTER);
        pnlCari.add(btnCari, BorderLayout.EAST);
        pnlHeaderKiri.add(pnlCari, BorderLayout.SOUTH);
        card.add(pnlHeaderKiri, BorderLayout.NORTH);

        // Tabel Daftar Nota
        JPanel pnlTengah = new JPanel(new BorderLayout(6, 6));
        pnlTengah.setOpaque(false);

        JPanel pnlFilter = new JPanel(new BorderLayout(4, 0));
        pnlFilter.setOpaque(false);
        pnlFilter.add(NeoFormStyle.label("Filter No. Nota"), BorderLayout.WEST);
        txtFilter = NeoFormStyle.field("filter_nota");
        txtFilter.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { tampilkanDaftar(txtFilter.getText()); }
            @Override public void removeUpdate(DocumentEvent e) { tampilkanDaftar(txtFilter.getText()); }
            @Override public void changedUpdate(DocumentEvent e) { tampilkanDaftar(txtFilter.getText()); }
        });
        pnlFilter.add(txtFilter, BorderLayout.CENTER);
        pnlTengah.add(pnlFilter, BorderLayout.NORTH);

        modelNota = new DefaultTableModel(
                new String[]{"No Nota", "Tanggal", "Pelanggan", "Total Tagihan"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblNota = new JTable(modelNota);
        tblNota.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        NeoTableStyle.apply(tblNota);
        tblNota.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) pilihNotaDariDaftar();
        });
        JScrollPane scroll = NeoTableStyle.wrap(tblNota);
        pnlTengah.add(scroll, BorderLayout.CENTER);
        card.add(pnlTengah, BorderLayout.CENTER);

        // Info Nota Terpilih
        lblInfoNota = new JLabel("Silakan klik salah satu nota dari daftar di atas.");
        lblInfoNota.setFont(NeoBrutalTheme.body(java.awt.Font.PLAIN, 11));
        lblInfoNota.setForeground(NeoBrutalTheme.TEXT_MUTED);
        JPanel pnlInfo = new JPanel(new BorderLayout());
        pnlInfo.setOpaque(false);
        pnlInfo.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
        pnlInfo.add(lblInfoNota, BorderLayout.CENTER);
        card.add(pnlInfo, BorderLayout.SOUTH);

        return card;
    }

    private JPanel buildKanan() {
        JPanel card = NeoFormStyle.card(new BorderLayout(8, 8));
        card.add(NeoFormStyle.section("2. Pilih Item yang Diretur"), BorderLayout.NORTH);

        // Tabel Item dalam Nota
        modelItem = new DefaultTableModel(
                new String[]{"No", "Kode", "Nama Barang", "Qty Beli", "Sudah", "Sisa", "Harga Jual"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblItem = new JTable(modelItem);
        tblItem.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        NeoTableStyle.apply(tblItem);
        tblItem.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        int[] lebarItem = {32, 55, 120, 55, 45, 45, 72};
        for (int i = 0; i < lebarItem.length; i++) {
            tblItem.getColumnModel().getColumn(i).setPreferredWidth(lebarItem[i]);
        }
        tblItem.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) itemNotaDipilih();
        });
        JScrollPane scroll = NeoTableStyle.wrap(tblItem);
        card.add(scroll, BorderLayout.CENTER);

        // Form Input Retur di Bawah
        JPanel pnlBawah = new JPanel(new BorderLayout(6, 6));
        pnlBawah.setOpaque(false);

        lblItemPilihan = new JLabel("Klik salah satu item buku pada tabel di atas untuk retur.");
        lblItemPilihan.setFont(NeoBrutalTheme.body(java.awt.Font.PLAIN, 11));
        lblItemPilihan.setForeground(NeoBrutalTheme.TEXT_MUTED);
        pnlBawah.add(lblItemPilihan, BorderLayout.NORTH);

        JPanel pnlInput = new JPanel(new GridLayout(2, 2, 6, 4));
        pnlInput.setOpaque(false);

        txtQty = NeoFormStyle.field("qty_retur");
        txtQty.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { updateLiveRefund(); }
            @Override public void removeUpdate(DocumentEvent e) { updateLiveRefund(); }
            @Override public void changedUpdate(DocumentEvent e) { updateLiveRefund(); }
        });

        txtAlasan = NeoFormStyle.field("alasan");

        pnlInput.add(NeoFormStyle.label("Qty Retur"));
        pnlInput.add(NeoFormStyle.label("Alasan"));
        pnlInput.add(txtQty);
        pnlInput.add(txtAlasan);

        // Panel Refund Dinamis & Tombol
        JPanel pnlAksi = new JPanel(new BorderLayout(6, 6));
        pnlAksi.setOpaque(false);

        lblRefundInfo = new JLabel("Uang Kembali (Refund): Rp 0");
        lblRefundInfo.setFont(NeoBrutalTheme.display(14));
        lblRefundInfo.setForeground(NeoBrutalTheme.INK);
        lblRefundInfo.setOpaque(true);
        lblRefundInfo.setBackground(NeoBrutalTheme.WARNING);
        lblRefundInfo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(NeoBrutalTheme.INK, 2),
                BorderFactory.createEmptyBorder(5, 9, 5, 9)
        ));

        JPanel pnlBtn = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        pnlBtn.setOpaque(false);

        JButton btnReset = styledButton("Reset", null, NeoBrutalTheme.SECONDARY);
        btnReset.addActionListener(e -> resetFormInput());

        JButton btnSimpan = styledButton("Simpan & Cetak",
                FontIcon.of(MaterialDesignC.CONTENT_SAVE, 16, NeoBrutalTheme.INK),
                NeoBrutalTheme.PRIMARY);
        btnSimpan.addActionListener(e -> doSimpan());

        pnlBtn.add(btnReset);
        pnlBtn.add(btnSimpan);

        pnlAksi.add(lblRefundInfo, BorderLayout.NORTH);
        pnlAksi.add(pnlBtn, BorderLayout.SOUTH);

        JPanel pnlGroupInput = new JPanel(new BorderLayout(6, 6));
        pnlGroupInput.setOpaque(false);
        pnlGroupInput.add(pnlInput, BorderLayout.NORTH);
        pnlGroupInput.add(pnlAksi, BorderLayout.SOUTH);

        pnlBawah.add(pnlGroupInput, BorderLayout.CENTER);
        card.add(pnlBawah, BorderLayout.SOUTH);
        return card;
    }
    // =========================================================================
    // TAB 2: RIWAYAT RETUR & CETAK ULANG
    // =========================================================================

    private JPanel buildTabRiwayat() {
        JPanel pnl = new JPanel(new BorderLayout(10, 10));
        pnl.setBackground(NeoBrutalTheme.BG);
        pnl.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JPanel card = NeoFormStyle.card(new BorderLayout(8, 8));

        // Header & Pencarian Riwayat
        JPanel pnlHeader = new JPanel(new BorderLayout(6, 6));
        pnlHeader.setOpaque(false);
        pnlHeader.add(NeoFormStyle.section("Riwayat Transaksi Retur"), BorderLayout.NORTH);

        JPanel pnlSearch = new JPanel(new BorderLayout(6, 0));
        pnlSearch.setOpaque(false);
        pnlSearch.add(NeoFormStyle.label("Cari No. Retur / Nota / Buku"), BorderLayout.WEST);

        txtCariRiwayat = NeoFormStyle.field("cari_riwayat");
        txtCariRiwayat.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { cariRiwayatLive(); }
            @Override public void removeUpdate(DocumentEvent e) { cariRiwayatLive(); }
            @Override public void changedUpdate(DocumentEvent e) { cariRiwayatLive(); }
        });

        JButton btnRefresh = styledButton("Segarkan",
                FontIcon.of(MaterialDesignR.REFRESH, 16, NeoBrutalTheme.INK),
                NeoBrutalTheme.SECONDARY);
        btnRefresh.addActionListener(e -> loadRiwayat());

        JPanel pnlCariAksi = new JPanel(new BorderLayout(6, 0));
        pnlCariAksi.setOpaque(false);
        pnlCariAksi.add(txtCariRiwayat, BorderLayout.CENTER);
        pnlCariAksi.add(btnRefresh, BorderLayout.EAST);
        pnlSearch.add(pnlCariAksi, BorderLayout.CENTER);

        pnlHeader.add(pnlSearch, BorderLayout.SOUTH);
        card.add(pnlHeader, BorderLayout.NORTH);

        // Tabel Riwayat
        modelRiwayat = new DefaultTableModel(
                new String[]{"No. Retur", "Tanggal", "No. Nota Asli", "Kode", "Nama Barang", "Qty", "Harga Satuan", "Total Refund", "Alasan"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblRiwayat = new JTable(modelRiwayat);
        tblRiwayat.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        NeoTableStyle.apply(tblRiwayat);
        tblRiwayat.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        int[] lebar = {70, 110, 80, 55, 140, 40, 80, 85, 130};
        for (int i = 0; i < lebar.length; i++) {
            tblRiwayat.getColumnModel().getColumn(i).setPreferredWidth(lebar[i]);
        }
        JScrollPane scroll = NeoTableStyle.wrap(tblRiwayat);
        card.add(scroll, BorderLayout.CENTER);

        // Bottom Bar (Summary & Tombol Cetak Ulang)
        JPanel pnlBottom = new JPanel(new BorderLayout(10, 10));
        pnlBottom.setOpaque(false);

        lblRiwayatSummary = new JLabel("Memuat data riwayat retur...");
        lblRiwayatSummary.setFont(NeoBrutalTheme.body(java.awt.Font.PLAIN, 11));
        lblRiwayatSummary.setForeground(NeoBrutalTheme.TEXT_MUTED);
        pnlBottom.add(lblRiwayatSummary, BorderLayout.WEST);

        JButton btnCetakUlang = styledButton("Cetak Ulang",
                FontIcon.of(MaterialDesignP.PRINTER, 16, NeoBrutalTheme.INK),
                NeoBrutalTheme.PRIMARY);
        btnCetakUlang.addActionListener(e -> cetakUlangRetur());
        pnlBottom.add(btnCetakUlang, BorderLayout.EAST);

        card.add(pnlBottom, BorderLayout.SOUTH);
        pnl.add(card, BorderLayout.CENTER);
        return pnl;
    }
    // =========================================================================
    // LOGIKA TAB 1 (INPUT RETUR)
    // =========================================================================

    public void loadTable() {
        daftarNota = penjualanDAO.listTerbaru(50);
        tampilkanDaftar(txtFilter != null ? txtFilter.getText() : "");
    }

    private void tampilkanDaftar(String keyword) {
        String kw = keyword == null ? "" : keyword.trim().toLowerCase();
        daftarTampil = new ArrayList<>();
        for (Penjualan p : daftarNota) {
            if (kw.isEmpty() || (p.getNoNota() != null && p.getNoNota().toLowerCase().contains(kw))) {
                daftarTampil.add(p);
            }
        }
        modelNota.setRowCount(0);
        for (Penjualan p : daftarTampil) {
            String tgl = p.getTanggal() != null ? p.getTanggal().format(FMT_TGL) : "-";
            String cust = "Pelanggan Umum";
            modelNota.addRow(new Object[]{p.getNoNota(), tgl, cust, formatRp(p.getTotal())});
        }
    }

    private void pilihNotaDariDaftar() {
        int row = tblNota.getSelectedRow();
        if (row < 0 || row >= daftarTampil.size()) return;
        tampilkanNota(daftarTampil.get(row));
    }

    private void tampilkanNota(Penjualan p) {
        nota = p;
        detailNota = penjualanDAO.getDetailByPenjualan(p.getIdPenjualan());
        String tgl = p.getTanggal() != null ? p.getTanggal().format(FMT_TGL) : "-";
        String cust = "Umum";

        lblInfoNota.setText("Nota: " + p.getNoNota() + " | Tgl: " + tgl + " | Pelanggan: " + cust + " | Total: " + formatRp(p.getTotal()));
        refreshItem();
        resetFormInput();
    }

    private void cariNota() {
        String noNota = txtNoNota.getText().trim();
        if (noNota.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Silakan masukkan nomor nota penjualan terlebih dahulu.",
                    "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Penjualan p = penjualanDAO.getByNoNota(noNota);
        if (p == null) {
            JOptionPane.showMessageDialog(this, "Nota " + noNota + " tidak ditemukan di sistem.",
                    "Tidak Ditemukan", JOptionPane.ERROR_MESSAGE);
            return;
        }
        tampilkanNota(p);
    }

    private void refreshItem() {
        modelItem.setRowCount(0);
        if (nota == null) return;
        int no = 1;
        for (DetailPenjualan d : detailNota) {
            int sudah = returDAO.getReturQty(nota.getIdPenjualan(), d.getIdBuku());
            int sisa = d.getQty() - sudah;
            modelItem.addRow(new Object[]{
                    no++, d.getKodeBuku(), d.getJudul(), d.getQty(), sudah, sisa, formatRp(d.getHargaJual())
            });
        }
    }

    private void itemNotaDipilih() {
        int row = tblItem.getSelectedRow();
        if (row < 0 || row >= detailNota.size()) {
            lblItemPilihan.setText("Klik salah satu item buku pada tabel di atas untuk retur.");
            lblRefundInfo.setText("Uang Kembali (Refund): Rp 0");
            return;
        }
        DetailPenjualan d = detailNota.get(row);
        int sudah = returDAO.getReturQty(nota.getIdPenjualan(), d.getIdBuku());
        int sisa = d.getQty() - sudah;

        lblItemPilihan.setText("[" + d.getKodeBuku() + "] " + d.getJudul()
                + " | Harga: " + formatRp(d.getHargaJual())
                + " | Maksimal retur: " + sisa + " pcs");

        if (sisa <= 0) {
            txtQty.setText("0");
            txtQty.setEnabled(false);
            txtAlasan.setEnabled(false);
            lblRefundInfo.setText("Semua kuantitas buku ini sudah selesai diretur.");
        } else {
            txtQty.setEnabled(true);
            txtAlasan.setEnabled(true);
            if (txtQty.getText().trim().isEmpty() || "0".equals(txtQty.getText().trim())) {
                txtQty.setText("1");
            }
            updateLiveRefund();
        }
    }

    private void updateLiveRefund() {
        int row = tblItem.getSelectedRow();
        if (row < 0 || row >= detailNota.size()) {
            lblRefundInfo.setText("Uang Kembali (Refund): Rp 0");
            return;
        }
        DetailPenjualan d = detailNota.get(row);
        String text = txtQty.getText().trim();
        if (text.isEmpty()) {
            lblRefundInfo.setText("Uang Kembali (Refund): Rp 0");
            return;
        }
        try {
            int q = Integer.parseInt(text);
            int sudah = returDAO.getReturQty(nota.getIdPenjualan(), d.getIdBuku());
            int sisa = d.getQty() - sudah;

            if (q <= 0) {
                lblRefundInfo.setText("Qty retur harus minimal 1 pcs");
            } else if (q > sisa) {
                lblRefundInfo.setText("Melebihi batas sisa (" + sisa + " pcs)!");
            } else {
                double refund = q * d.getHargaJual();
                lblRefundInfo.setText("Uang Kembali (Refund): " + formatRp(refund) + " (" + q + " x " + formatRp(d.getHargaJual()) + ")");
            }
        } catch (NumberFormatException e) {
            lblRefundInfo.setText("Masukkan angka kuantitas yang valid");
        }
    }

    private void resetFormInput() {
        tblItem.clearSelection();
        txtQty.setText("");
        txtAlasan.setText("");
        txtQty.setEnabled(true);
        txtAlasan.setEnabled(true);
        lblItemPilihan.setText("Klik salah satu item buku pada tabel di atas untuk retur.");
        lblRefundInfo.setText("Uang Kembali (Refund): Rp 0");
    }

    private void doSimpan() {
        if (Sesi.userLogin == null) {
            JOptionPane.showMessageDialog(this, "Sesi Anda telah habis. Silakan login kembali.",
                    "Sesi Habis", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (nota == null) {
            JOptionPane.showMessageDialog(this, "Pilih atau cari nota penjualan terlebih dahulu.",
                    "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int row = tblItem.getSelectedRow();
        if (row < 0 || row >= detailNota.size()) {
            JOptionPane.showMessageDialog(this, "Pilih baris item buku yang ingin diretur.",
                    "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int qtyRetur;
        try {
            qtyRetur = Integer.parseInt(txtQty.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Qty retur harus berupa angka bulat positif.",
                    "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        DetailPenjualan d = detailNota.get(row);
        int sudah = returDAO.getReturQty(nota.getIdPenjualan(), d.getIdBuku());
        int sisa = d.getQty() - sudah;
        if (qtyRetur <= 0 || qtyRetur > sisa) {
            JOptionPane.showMessageDialog(this,
                    "Qty retur (" + qtyRetur + " pcs) tidak valid! Sisa yang dapat diretur adalah " + sisa + " pcs.",
                    "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String alasan = txtAlasan.getText().trim();
        if (alasan.isEmpty()) {
            alasan = "Rusak / Cacat Cetak";
        }

        double totalRefund = qtyRetur * d.getHargaJual();
        int konfirm = JOptionPane.showConfirmDialog(this,
                "Konfirmasi Penyelesaian Retur:\n"
                + "Buku: [" + d.getKodeBuku() + "] " + d.getJudul() + "\n"
                + "Qty Retur: " + qtyRetur + " pcs\n"
                + "Alasan: " + alasan + "\n"
                + "Total Refund (Dana Kembali): " + formatRp(totalRefund) + "\n\n"
                + "Lanjutkan simpan dan cetak bukti retur?",
                "Konfirmasi Retur", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (konfirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            String noRetur = NotaGenerator.next("RT", "retur", "no_retur");
            Retur r = new Retur();
            r.setNoRetur(noRetur);
            r.setTanggal(LocalDateTime.now());
            r.setIdPenjualan(nota.getIdPenjualan());
            r.setNoNota(nota.getNoNota());
            r.setIdBuku(d.getIdBuku());
            r.setKodeBuku(d.getKodeBuku());
            r.setJudul(d.getJudul());
            r.setQty(qtyRetur);
            r.setAlasan(alasan);
            r.setHargaJual(d.getHargaJual());

            returDAO.saveRetur(r);

            // Buka dialog struk retur
            Frame owner = (Frame) SwingUtilities.getWindowAncestor(this);
            String namaPetugas = Sesi.userLogin != null ? Sesi.userLogin.getNamaLengkap() : "Admin";
            String namaPelanggan = "Pelanggan Umum";

            StrukReturDialog dialog = new StrukReturDialog(owner, r, d.getHargaJual(), namaPetugas, namaPelanggan);
            dialog.setVisible(true);

            // Refresh tampilan
            resetFormInput();
            refreshItem();
            loadRiwayat();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal memproses transaksi retur: " + ex.getMessage(),
                    "Gagal Simpan", JOptionPane.ERROR_MESSAGE);
        }
    }

    // =========================================================================
    // LOGIKA TAB 2 (RIWAYAT RETUR)
    // =========================================================================

    public void loadRiwayat() {
        listRiwayat = returDAO.getAll();
        tampilkanRiwayat(listRiwayat);
    }

    private void cariRiwayatLive() {
        String kw = txtCariRiwayat != null ? txtCariRiwayat.getText().trim() : "";
        if (kw.isEmpty()) {
            loadRiwayat();
        } else {
            listRiwayat = returDAO.search(kw);
            tampilkanRiwayat(listRiwayat);
        }
    }

    private void tampilkanRiwayat(List<Retur> list) {
        modelRiwayat.setRowCount(0);
        int totalPcs = 0;
        double totalRefundSemua = 0.0;

        for (Retur r : list) {
            String tgl = r.getTanggal() != null ? r.getTanggal().format(FMT_TGL) : "-";
            double refund = r.getTotalRefund();
            totalPcs += r.getQty();
            totalRefundSemua += refund;

            modelRiwayat.addRow(new Object[]{
                    r.getNoRetur(),
                    tgl,
                    r.getNoNota(),
                    r.getKodeBuku(),
                    r.getJudul(),
                    r.getQty() + " pcs",
                    formatRp(r.getHargaJual()),
                    formatRp(refund),
                    r.getAlasan()
            });
        }

        lblRiwayatSummary.setText("Total: " + list.size() + " Transaksi Retur | "
                + totalPcs + " Pcs Buku Masuk Gudang | Total Dana Refund: " + formatRp(totalRefundSemua));
    }

    private void cetakUlangRetur() {
        int row = tblRiwayat.getSelectedRow();
        if (row < 0 || row >= listRiwayat.size()) {
            JOptionPane.showMessageDialog(this, "Pilih salah satu baris retur dari tabel riwayat di atas untuk dicetak ulang.",
                    "Pilih Transaksi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Retur r = listRiwayat.get(row);
        Frame owner = (Frame) SwingUtilities.getWindowAncestor(this);
        String namaPetugas = Sesi.userLogin != null ? Sesi.userLogin.getNamaLengkap() : "Petugas";
        StrukReturDialog dialog = new StrukReturDialog(owner, r, r.getHargaJual(), namaPetugas, "Pelanggan");
        dialog.setVisible(true);
    }

    // =========================================================================
    // HELPER UI
    // =========================================================================

    private NeoButton styledButton(String teks, FontIcon ikon, Color bg) {
        return NeoFormStyle.button(teks, ikon, bg);
    }

    private String formatRp(double val) {
        return "Rp " + FMT_RP.format((long) Math.round(val));
    }
}
