package view;

import dao.PenjualanDAO;
import dao.PenjualanDAOImpl;
import dao.ReturDAO;
import dao.ReturDAOImpl;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import model.DetailPenjualan;
import model.Penjualan;
import model.Retur;
import org.kordamp.ikonli.materialdesign2.MaterialDesignA;
import org.kordamp.ikonli.materialdesign2.MaterialDesignC;
import org.kordamp.ikonli.materialdesign2.MaterialDesignH;
import org.kordamp.ikonli.materialdesign2.MaterialDesignM;
import org.kordamp.ikonli.materialdesign2.MaterialDesignP;
import org.kordamp.ikonli.materialdesign2.MaterialDesignR;
import org.kordamp.ikonli.swing.FontIcon;
import util.NeoBrutalTheme;
import util.NeoShadowBorder;
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

        // Header Title
        JPanel pnlHeader = new JPanel(new BorderLayout(8, 4));
        pnlHeader.setOpaque(false);
        JLabel lblJudul = new JLabel("Retur Penjualan Buku");
        lblJudul.setFont(new Font("Segoe UI Black", Font.BOLD, 22));
        JLabel lblSub = new JLabel("Layanan pengembalian barang, refund dana kasir, & cetak bukti retur");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(new Color(90, 90, 90));
        pnlHeader.add(lblJudul, BorderLayout.NORTH);
        pnlHeader.add(lblSub, BorderLayout.SOUTH);
        add(pnlHeader, BorderLayout.NORTH);

        // Tabbed Pane (Input Retur & Riwayat Retur)
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI Semibold", Font.BOLD, 14));
        tabbedPane.setBackground(NeoBrutalTheme.BG);

        tabbedPane.addTab("Input Retur",
                FontIcon.of(MaterialDesignC.CART_ARROW_DOWN, 18, Color.BLACK),
                buildTabInput());

        tabbedPane.addTab("Riwayat Retur",
                FontIcon.of(MaterialDesignH.HISTORY, 18, Color.BLACK),
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
        JPanel card = new JPanel(new BorderLayout(8, 8));
        card.setBackground(NeoBrutalTheme.SURFACE);
        card.setBorder(new NeoShadowBorder());

        // Header Card Kiri
        JPanel pnlHeaderKiri = new JPanel(new BorderLayout(4, 4));
        pnlHeaderKiri.setBackground(NeoBrutalTheme.SURFACE);
        JLabel lbl = new JLabel("1. Cari & Pilih Nota Penjualan");
        lbl.setFont(new Font("Segoe UI Bold", Font.BOLD, 15));
        pnlHeaderKiri.add(lbl, BorderLayout.NORTH);

        JPanel pnlCari = new JPanel(new BorderLayout(6, 0));
        pnlCari.setBackground(NeoBrutalTheme.SURFACE);
        txtNoNota = new JTextField();
        txtNoNota.setName("no_nota");
        txtNoNota.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 13));
        txtNoNota.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        txtNoNota.addActionListener(e -> cariNota());

        JButton btnCari = styledButton("Cari",
                FontIcon.of(MaterialDesignM.MAGNIFY, 18, Color.BLACK),
                NeoBrutalTheme.SECONDARY);
        btnCari.addActionListener(e -> cariNota());
        pnlCari.add(txtNoNota, BorderLayout.CENTER);
        pnlCari.add(btnCari, BorderLayout.EAST);
        pnlHeaderKiri.add(pnlCari, BorderLayout.SOUTH);
        card.add(pnlHeaderKiri, BorderLayout.NORTH);

        // Tabel Daftar Nota
        JPanel pnlTengah = new JPanel(new BorderLayout(6, 6));
        pnlTengah.setBackground(NeoBrutalTheme.SURFACE);

        JPanel pnlFilter = new JPanel(new BorderLayout(4, 0));
        pnlFilter.setBackground(NeoBrutalTheme.SURFACE);
        JLabel lblFilter = new JLabel("Filter No. Nota: ");
        lblFilter.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtFilter = new JTextField();
        txtFilter.setName("filter_nota");
        txtFilter.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        txtFilter.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { tampilkanDaftar(txtFilter.getText()); }
            @Override public void removeUpdate(DocumentEvent e) { tampilkanDaftar(txtFilter.getText()); }
            @Override public void changedUpdate(DocumentEvent e) { tampilkanDaftar(txtFilter.getText()); }
        });
        pnlFilter.add(lblFilter, BorderLayout.WEST);
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
        tblNota.setRowHeight(26);
        tblNota.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) pilihNotaDariDaftar();
        });
        JScrollPane scroll = new JScrollPane(tblNota);
        scroll.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        pnlTengah.add(scroll, BorderLayout.CENTER);
        card.add(pnlTengah, BorderLayout.CENTER);

        // Info Nota Terpilih
        lblInfoNota = new JLabel("Silakan klik salah satu nota dari daftar di atas.");
        lblInfoNota.setFont(new Font("Segoe UI Semibold", Font.BOLD, 12));
        lblInfoNota.setForeground(new Color(70, 70, 70));
        JPanel pnlInfo = new JPanel(new BorderLayout());
        pnlInfo.setBackground(NeoBrutalTheme.SURFACE);
        pnlInfo.setBorder(BorderFactory.createEmptyBorder(6, 4, 4, 4));
        pnlInfo.add(lblInfoNota, BorderLayout.CENTER);
        card.add(pnlInfo, BorderLayout.SOUTH);

        return card;
    }

    private JPanel buildKanan() {
        JPanel card = new JPanel(new BorderLayout(8, 8));
        card.setBackground(NeoBrutalTheme.SURFACE);
        card.setBorder(new NeoShadowBorder());

        // Header Card Kanan
        JLabel lbl = new JLabel("2. Pilih Item yang Diretur & Nominal Refund");
        lbl.setFont(new Font("Segoe UI Bold", Font.BOLD, 15));
        card.add(lbl, BorderLayout.NORTH);

        // Tabel Item dalam Nota
        modelItem = new DefaultTableModel(
                new String[]{"No", "Kode", "Judul Buku", "Qty Beli", "Sudah", "Sisa", "Harga Jual"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblItem = new JTable(modelItem);
        tblItem.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblItem.setRowHeight(26);
        tblItem.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) itemNotaDipilih();
        });
        JScrollPane scroll = new JScrollPane(tblItem);
        scroll.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        card.add(scroll, BorderLayout.CENTER);

        // Form Input Retur di Bawah
        JPanel pnlBawah = new JPanel(new BorderLayout(8, 8));
        pnlBawah.setBackground(NeoBrutalTheme.SURFACE);

        lblItemPilihan = new JLabel("ℹ️ Klik salah satu item buku pada tabel di atas untuk retur.");
        lblItemPilihan.setFont(new Font("Segoe UI Semibold", Font.BOLD, 12));
        lblItemPilihan.setForeground(new Color(40, 40, 40));
        pnlBawah.add(lblItemPilihan, BorderLayout.NORTH);

        JPanel pnlInput = new JPanel(new GridLayout(2, 2, 8, 6));
        pnlInput.setBackground(NeoBrutalTheme.SURFACE);

        txtQty = new JTextField();
        txtQty.setName("qty_retur");
        txtQty.setFont(new Font("Segoe UI Bold", Font.PLAIN, 13));
        txtQty.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        txtQty.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { updateLiveRefund(); }
            @Override public void removeUpdate(DocumentEvent e) { updateLiveRefund(); }
            @Override public void changedUpdate(DocumentEvent e) { updateLiveRefund(); }
        });

        txtAlasan = new JTextField();
        txtAlasan.setName("alasan");
        txtAlasan.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtAlasan.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));

        pnlInput.add(new JLabel("Qty Retur (Pcs):"));
        pnlInput.add(new JLabel("Alasan Pengembalian:"));
        pnlInput.add(txtQty);
        pnlInput.add(txtAlasan);

        // Panel Refund Dinamis & Tombol
        JPanel pnlAksi = new JPanel(new BorderLayout(8, 8));
        pnlAksi.setBackground(NeoBrutalTheme.SURFACE);

        lblRefundInfo = new JLabel("💰 Uang Kembali (Refund): Rp 0", SwingConstants.LEFT);
        lblRefundInfo.setFont(new Font("Segoe UI Black", Font.BOLD, 14));
        lblRefundInfo.setOpaque(true);
        lblRefundInfo.setBackground(new Color(254, 240, 138)); // Highlight yellow
        lblRefundInfo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.BLACK, 2),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));

        JPanel pnlBtn = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlBtn.setBackground(NeoBrutalTheme.SURFACE);

        JButton btnReset = styledButton("Reset", null, NeoBrutalTheme.SECONDARY);
        btnReset.addActionListener(e -> resetFormInput());

        JButton btnSimpan = styledButton("Simpan & Cetak Bukti",
                FontIcon.of(MaterialDesignC.CONTENT_SAVE, 18, Color.BLACK),
                NeoBrutalTheme.PRIMARY);
        btnSimpan.addActionListener(e -> doSimpan());

        pnlBtn.add(btnReset);
        pnlBtn.add(btnSimpan);

        pnlAksi.add(lblRefundInfo, BorderLayout.CENTER);
        pnlAksi.add(pnlBtn, BorderLayout.EAST);

        JPanel pnlGroupInput = new JPanel(new BorderLayout(6, 6));
        pnlGroupInput.setBackground(NeoBrutalTheme.SURFACE);
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

        JPanel card = new JPanel(new BorderLayout(8, 8));
        card.setBackground(NeoBrutalTheme.SURFACE);
        card.setBorder(new NeoShadowBorder());

        // Header & Pencarian Riwayat
        JPanel pnlHeader = new JPanel(new BorderLayout(8, 8));
        pnlHeader.setBackground(NeoBrutalTheme.SURFACE);

        JLabel lblTitle = new JLabel("Riwayat Transaksi Retur Buku Masuk Gudang");
        lblTitle.setFont(new Font("Segoe UI Bold", Font.BOLD, 15));
        pnlHeader.add(lblTitle, BorderLayout.NORTH);

        JPanel pnlSearch = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pnlSearch.setBackground(NeoBrutalTheme.SURFACE);

        JLabel lblCari = new JLabel("Pencarian (No. Retur / Nota / Buku):");
        lblCari.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 13));
        pnlSearch.add(lblCari);

        txtCariRiwayat = new JTextField(24);
        txtCariRiwayat.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        txtCariRiwayat.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { cariRiwayatLive(); }
            @Override public void removeUpdate(DocumentEvent e) { cariRiwayatLive(); }
            @Override public void changedUpdate(DocumentEvent e) { cariRiwayatLive(); }
        });
        pnlSearch.add(txtCariRiwayat);

        JButton btnRefresh = styledButton("Segarkan",
                FontIcon.of(MaterialDesignR.REFRESH, 18, Color.BLACK),
                NeoBrutalTheme.SECONDARY);
        btnRefresh.addActionListener(e -> loadRiwayat());
        pnlSearch.add(btnRefresh);

        pnlHeader.add(pnlSearch, BorderLayout.SOUTH);
        card.add(pnlHeader, BorderLayout.NORTH);

        // Tabel Riwayat
        modelRiwayat = new DefaultTableModel(
                new String[]{"No. Retur", "Tanggal", "No. Nota Asli", "Kode Buku", "Judul Buku", "Qty", "Harga Satuan", "Total Refund", "Alasan"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblRiwayat = new JTable(modelRiwayat);
        tblRiwayat.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblRiwayat.setRowHeight(26);
        JScrollPane scroll = new JScrollPane(tblRiwayat);
        scroll.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        card.add(scroll, BorderLayout.CENTER);

        // Bottom Bar (Summary & Tombol Cetak Ulang)
        JPanel pnlBottom = new JPanel(new BorderLayout(10, 10));
        pnlBottom.setBackground(NeoBrutalTheme.SURFACE);

        lblRiwayatSummary = new JLabel("Memuat data riwayat retur...");
        lblRiwayatSummary.setFont(new Font("Segoe UI Semibold", Font.BOLD, 13));
        pnlBottom.add(lblRiwayatSummary, BorderLayout.WEST);

        JButton btnCetakUlang = styledButton("Cetak Ulang Bukti",
                FontIcon.of(MaterialDesignP.PRINTER, 18, Color.BLACK),
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
            String cust = (p.getKodeMember() != null && !p.getKodeMember().isBlank())
                    ? "Member (" + p.getKodeMember() + ")" : "Pelanggan Umum";
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
        String cust = (p.getKodeMember() != null && !p.getKodeMember().isBlank())
                ? "Member (" + p.getKodeMember() + ")" : "Umum";

        lblInfoNota.setText("📄 Nota: " + p.getNoNota() + " | Tgl: " + tgl + " | Pelanggan: " + cust + " | Total: " + formatRp(p.getTotal()));
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
            lblItemPilihan.setText("ℹ️ Klik salah satu item buku pada tabel di atas untuk retur.");
            lblRefundInfo.setText("💰 Uang Kembali (Refund): Rp 0");
            return;
        }
        DetailPenjualan d = detailNota.get(row);
        int sudah = returDAO.getReturQty(nota.getIdPenjualan(), d.getIdBuku());
        int sisa = d.getQty() - sudah;

        lblItemPilihan.setText("👉 [" + d.getKodeBuku() + "] " + d.getJudul()
                + " | Harga: " + formatRp(d.getHargaJual())
                + " | Maksimal retur: " + sisa + " pcs");

        if (sisa <= 0) {
            txtQty.setText("0");
            txtQty.setEnabled(false);
            txtAlasan.setEnabled(false);
            lblRefundInfo.setText("⚠️ Semua kuantitas buku ini sudah selesai diretur.");
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
            lblRefundInfo.setText("💰 Uang Kembali (Refund): Rp 0");
            return;
        }
        DetailPenjualan d = detailNota.get(row);
        String text = txtQty.getText().trim();
        if (text.isEmpty()) {
            lblRefundInfo.setText("💰 Uang Kembali (Refund): Rp 0");
            return;
        }
        try {
            int q = Integer.parseInt(text);
            int sudah = returDAO.getReturQty(nota.getIdPenjualan(), d.getIdBuku());
            int sisa = d.getQty() - sudah;

            if (q <= 0) {
                lblRefundInfo.setText("⚠️ Qty retur harus minimal 1 pcs");
            } else if (q > sisa) {
                lblRefundInfo.setText("⚠️ Melebihi batas sisa (" + sisa + " pcs)!");
            } else {
                double refund = q * d.getHargaJual();
                lblRefundInfo.setText("💰 Uang Kembali (Refund): " + formatRp(refund) + " (" + q + " x " + formatRp(d.getHargaJual()) + ")");
            }
        } catch (NumberFormatException e) {
            lblRefundInfo.setText("⚠️ Masukkan angka kuantitas yang valid");
        }
    }

    private void resetFormInput() {
        tblItem.clearSelection();
        txtQty.setText("");
        txtAlasan.setText("");
        txtQty.setEnabled(true);
        txtAlasan.setEnabled(true);
        lblItemPilihan.setText("ℹ️ Klik salah satu item buku pada tabel di atas untuk retur.");
        lblRefundInfo.setText("💰 Uang Kembali (Refund): Rp 0");
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
                + "• Buku: [" + d.getKodeBuku() + "] " + d.getJudul() + "\n"
                + "• Qty Retur: " + qtyRetur + " pcs\n"
                + "• Alasan: " + alasan + "\n"
                + "• Total Refund (Dana Kembali): " + formatRp(totalRefund) + "\n\n"
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
            String namaPelanggan = (nota.getKodeMember() != null && !nota.getKodeMember().isBlank())
                    ? "Member (" + nota.getKodeMember() + ")" : "Pelanggan Umum";

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

        lblRiwayatSummary.setText("📊 Total: " + list.size() + " Transaksi Retur | "
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

    private String formatRp(double val) {
        return "Rp " + FMT_RP.format((long) Math.round(val));
    }
}
