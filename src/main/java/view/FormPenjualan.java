package view;

import dao.BukuDAO;
import dao.BukuDAOImpl;
import dao.PenjualanDAO;
import dao.PenjualanDAOImpl;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
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
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import model.Buku;
import model.DetailPenjualan;
import model.Penjualan;
import org.kordamp.ikonli.materialdesign2.MaterialDesignC;
import org.kordamp.ikonli.materialdesign2.MaterialDesignD;
import org.kordamp.ikonli.materialdesign2.MaterialDesignM;
import org.kordamp.ikonli.swing.FontIcon;
import util.NeoBrutalTheme;
import util.NotaGenerator;
import util.Sesi;

public class FormPenjualan extends JPanel {

    private final BukuDAO bukuDAO = new BukuDAOImpl();
    private final PenjualanDAO penjualanDAO = new PenjualanDAOImpl();

    private static class CartRow {
        Buku buku;
        int qty;
    }
    private final List<CartRow> cart = new ArrayList<>();
    private List<Buku> hasilCari = new ArrayList<>();

    private JTable tblHasil;
    private DefaultTableModel modelHasil;
    private JTextField txtCari;
    private JTable tblCart;
    private DefaultTableModel modelCart;
    private JComboBox<String> cmbMetode;
    private JLabel lblDiskon;
    private JTextField txtDiskonPersen;
    private JTextField txtPotongan;
    private JLabel lblTotal;
    private JLabel lblKembalian;
    private JTextField txtBayar;

    public FormPenjualan() {
        setLayout(new BorderLayout(12, 12));
        setBackground(NeoBrutalTheme.BG);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        add(new NeoPageHeader("TRANSAKSI", "Penjualan (POS)",
                "Cari barang, susun keranjang, dan selesaikan transaksi.", null),
                BorderLayout.NORTH);

        JPanel cols = new JPanel(new GridLayout(1, 3, 12, 0));
        cols.setBackground(NeoBrutalTheme.BG);
        cols.add(buildKiri());
        cols.add(buildTengah());
        cols.add(buildKanan());
        add(cols, BorderLayout.CENTER);

        refreshCart();
        cariBuku("");
    }

    private JPanel buildKiri() {
        JPanel card = NeoFormStyle.card(new BorderLayout(8, 8));
        card.add(NeoFormStyle.section("Cari Barang"), BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(6, 6));
        body.setOpaque(false);

        JPanel pnlCari = new JPanel(new BorderLayout(6, 6));
        pnlCari.setOpaque(false);
        txtCari = NeoFormStyle.field("cari");
        txtCari.setColumns(10);
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
        body.add(pnlCari, BorderLayout.NORTH);

        modelHasil = new DefaultTableModel(new String[]{"Kode", "Judul", "Jenis", "Harga", "Stok"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblHasil = new JTable(modelHasil);
        NeoTableStyle.apply(tblHasil);
        tblHasil.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        int[] lebarHasil = {58, 135, 48, 68, 42};
        for (int i = 0; i < lebarHasil.length; i++) {
            tblHasil.getColumnModel().getColumn(i).setPreferredWidth(lebarHasil[i]);
        }
        tblHasil.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) tambahKeKeranjang();
            }
        });
        JScrollPane scroll = NeoTableStyle.wrap(tblHasil);
        body.add(scroll, BorderLayout.CENTER);

        JButton btnTambah = styledButton("Tambah", null, NeoBrutalTheme.SUCCESS);
        btnTambah.addActionListener(e -> tambahKeKeranjang());
        body.add(btnTambah, BorderLayout.SOUTH);

        card.add(body, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildTengah() {
        JPanel card = NeoFormStyle.card(new BorderLayout(8, 8));
        card.add(NeoFormStyle.section("Keranjang"), BorderLayout.NORTH);

        modelCart = new DefaultTableModel(new String[]{"Kode", "Judul", "Jenis", "Harga", "Qty", "Subtotal"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblCart = new JTable(modelCart);
        NeoTableStyle.apply(tblCart);
        tblCart.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        int[] lebarCart = {55, 120, 45, 68, 45, 80};
        for (int i = 0; i < lebarCart.length; i++) {
            tblCart.getColumnModel().getColumn(i).setPreferredWidth(lebarCart[i]);
        }
        JScrollPane scroll = NeoTableStyle.wrap(tblCart);
        card.add(scroll, BorderLayout.CENTER);

        JPanel pnlQty = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        pnlQty.setOpaque(false);
        JButton btnPlus = styledButton("+", null, NeoBrutalTheme.SUCCESS);
        JButton btnMinus = styledButton("-", null, NeoBrutalTheme.WARNING);
        JButton btnHapus = styledButton("Hapus",
                FontIcon.of(MaterialDesignD.DELETE, 16, NeoBrutalTheme.INK),
                NeoBrutalTheme.DANGER);
        btnPlus.addActionListener(e -> ubahQty(1));
        btnMinus.addActionListener(e -> ubahQty(-1));
        btnHapus.addActionListener(e -> hapusBaris());
        pnlQty.add(btnPlus);
        pnlQty.add(btnMinus);
        pnlQty.add(btnHapus);
        card.add(pnlQty, BorderLayout.SOUTH);
        return card;
    }

    private JPanel buildKanan() {
        JPanel card = NeoFormStyle.card(new GridLayout(0, 1, 3, 3));
        card.add(NeoFormStyle.section("Ringkasan"));

        card.add(NeoFormStyle.label("Metode Bayar"));
        cmbMetode = new JComboBox<>(new String[]{"Tunai", "Transfer", "QRIS"});
        cmbMetode.setName("metode_bayar");
        NeoFormStyle.styleCombo(cmbMetode);
        cmbMetode.addActionListener(e -> refreshCart());
        card.add(cmbMetode);

        card.add(NeoFormStyle.label("Diskon (%)"));
        txtDiskonPersen = NeoFormStyle.field("diskon_persen");
        txtDiskonPersen.setText("0");
        txtDiskonPersen.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { refreshCart(); }
            @Override public void removeUpdate(DocumentEvent e) { refreshCart(); }
            @Override public void changedUpdate(DocumentEvent e) { refreshCart(); }
        });
        card.add(txtDiskonPersen);

        card.add(NeoFormStyle.label("Potongan (Rp)"));
        txtPotongan = NeoFormStyle.field("potongan");
        txtPotongan.setText("0");
        txtPotongan.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { refreshCart(); }
            @Override public void removeUpdate(DocumentEvent e) { refreshCart(); }
            @Override public void changedUpdate(DocumentEvent e) { refreshCart(); }
        });
        card.add(txtPotongan);

        lblDiskon = new JLabel(" ");
        lblDiskon.setFont(NeoBrutalTheme.body(java.awt.Font.PLAIN, 11));
        lblDiskon.setForeground(NeoBrutalTheme.TEXT_MUTED);
        card.add(lblDiskon);

        lblTotal = new JLabel("Total: 0");
        lblTotal.setFont(NeoBrutalTheme.display(20));
        lblTotal.setForeground(NeoBrutalTheme.BRAND_MAROON);
        card.add(lblTotal);

        card.add(NeoFormStyle.label("Bayar"));
        txtBayar = NeoFormStyle.field("bayar");
        txtBayar.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { hitungKembalian(); }
            @Override public void removeUpdate(DocumentEvent e) { hitungKembalian(); }
            @Override public void changedUpdate(DocumentEvent e) { hitungKembalian(); }
        });
        card.add(txtBayar);

        lblKembalian = new JLabel("Kembalian: 0");
        lblKembalian.setFont(NeoBrutalTheme.display(18));
        lblKembalian.setForeground(NeoBrutalTheme.BRAND_MAROON);
        card.add(lblKembalian);

        JButton btnBayar = styledButton("Bayar / Simpan",
                FontIcon.of(MaterialDesignC.CONTENT_SAVE, 16, NeoBrutalTheme.INK),
                NeoBrutalTheme.PRIMARY);
        btnBayar.addActionListener(e -> doSimpan());
        card.add(btnBayar);
        return card;
    }

    private NeoButton styledButton(String teks, FontIcon ikon, Color bg) {
        return NeoFormStyle.button(teks, ikon, bg);
    }

    private void cariBuku(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            hasilCari = bukuDAO.getAll();
        } else {
            hasilCari = bukuDAO.search(keyword.trim());
        }
        modelHasil.setRowCount(0);
        for (Buku b : hasilCari) {
            modelHasil.addRow(new Object[]{b.getKodeBuku(), b.getJudul(), jenisTampil(b),
                    b.getHargaJual(), b.getStok()});
        }
    }

    private String jenisTampil(Buku b) {
        return b.getJenis() != null && !b.getJenis().isBlank() ? b.getJenis() : "Buku";
    }

    private String qtyTampil(CartRow r) {
        String satuan = r.buku.getSatuan();
        return r.qty + (satuan != null && !satuan.isBlank() ? " " + satuan : "");
    }

    private void tambahKeKeranjang() {
        int row = tblHasil.getSelectedRow();
        if (row < 0 || row >= hasilCari.size()) {
            JOptionPane.showMessageDialog(this, "Pilih buku dulu dari hasil pencarian");
            return;
        }
        Buku b = hasilCari.get(row);
        Buku fresh = bukuDAO.getById(b.getIdBuku());
        if (fresh == null) return;
        for (CartRow r : cart) {
            if (r.buku.getIdBuku() == fresh.getIdBuku()) {
                if (r.qty + 1 > fresh.getStok()) {
                    JOptionPane.showMessageDialog(this, "Stok tidak mencukupi",
                            "Stok", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                r.qty++;
                refreshCart();
                return;
            }
        }
        if (fresh.getStok() < 1) {
            JOptionPane.showMessageDialog(this, "Stok tidak mencukupi",
                    "Stok", JOptionPane.ERROR_MESSAGE);
            return;
        }
        CartRow r = new CartRow();
        r.buku = fresh;
        r.qty = 1;
        cart.add(r);
        refreshCart();
    }

    private void ubahQty(int delta) {
        int row = tblCart.getSelectedRow();
        if (row < 0 || row >= cart.size()) {
            JOptionPane.showMessageDialog(this, "Pilih baris keranjang dulu");
            return;
        }
        CartRow r = cart.get(row);
        int baru = r.qty + delta;
        if (baru <= 0) {
            cart.remove(row);
            refreshCart();
            return;
        }
        Buku fresh = bukuDAO.getById(r.buku.getIdBuku());
        if (fresh != null && baru > fresh.getStok()) {
            JOptionPane.showMessageDialog(this, "Stok tidak mencukupi",
                    "Stok", JOptionPane.ERROR_MESSAGE);
            return;
        }
        r.qty = baru;
        refreshCart();
        tblCart.setRowSelectionInterval(Math.min(row, cart.size() - 1), Math.min(row, cart.size() - 1));
    }

    private void hapusBaris() {
        int row = tblCart.getSelectedRow();
        if (row < 0 || row >= cart.size()) {
            JOptionPane.showMessageDialog(this, "Pilih baris keranjang dulu");
            return;
        }
        cart.remove(row);
        refreshCart();
    }

    /** Total = round(subtotal*(100-persen)/100) - nominal. */
    public static double hitungTotal(double subtotal, double persen, double nominal) {
        return Math.round(subtotal * (100 - persen) / 100.0) - nominal;
    }

    /** @return pesan error atau null bila diskon persen+nominal valid (0<=persen<=100; 0<=nominal<=sisa). */
    public static String validasiDiskon(double persen, double nominal, double sisa) {
        if (Double.isNaN(persen) || persen < 0 || persen > 100) return "Diskon persen 0-100";
        if (Double.isNaN(nominal) || nominal < 0 || nominal > sisa) return "Potongan melebihi sisa";
        return null;
    }

    /** @return pesan error atau null bila bayar valid (non-tunai selalu valid: auto=total). */
    public static String validasiBayar(String metode, Double bayar, double total) {
        if (!"Tunai".equals(metode)) return null;
        if (bayar == null) return "Uang bayar harus angka";
        if (bayar < total) return "Uang bayar kurang";
        return null;
    }

    private String metodeTerpilih() {
        Object m = cmbMetode.getSelectedItem();
        return m == null ? "Tunai" : m.toString();
    }

    private double hitungSubtotal() {
        double total = 0;
        for (CartRow r : cart) {
            total += r.qty * r.buku.getHargaJual();
        }
        return total;
    }

    private int hitungQtyTotal() {
        int qty = 0;
        for (CartRow r : cart) {
            qty += r.qty;
        }
        return qty;
    }

    private double bacaPotongan() {
        try {
            return Double.parseDouble(txtPotongan.getText().trim());
        } catch (NumberFormatException | NullPointerException e) {
            return Double.NaN;
        }
    }

    private double bacaPersen() {
        try {
            return Double.parseDouble(txtDiskonPersen.getText().trim());
        } catch (NumberFormatException | NullPointerException e) {
            return Double.NaN;
        }
    }

    private double hitungTotal() {
        double subtotal = hitungSubtotal();
        double persen = bacaPersen();
        double nominal = bacaPotongan();
        if (Double.isNaN(persen)) persen = 0;
        if (Double.isNaN(nominal)) nominal = 0;
        return hitungTotal(subtotal, persen, nominal);
    }

    private void refreshCart() {
        modelCart.setRowCount(0);
        for (CartRow r : cart) {
            modelCart.addRow(new Object[]{
                    r.buku.getKodeBuku(), r.buku.getJudul(), jenisTampil(r.buku), r.buku.getHargaJual(),
                    qtyTampil(r), r.qty * r.buku.getHargaJual()
            });
        }
        double subtotal = hitungSubtotal();
        double persen = bacaPersen();
        double nominal = bacaPotongan();
        if (Double.isNaN(persen)) persen = 0;
        if (Double.isNaN(nominal)) nominal = 0;
        double total = hitungTotal(subtotal, persen, nominal);
        double diskonRupiah = Math.round(subtotal * persen / 100.0);
        if (diskonRupiah > 0 || nominal > 0) {
            lblDiskon.setText("Diskon: -" + (diskonRupiah + nominal));
        } else {
            lblDiskon.setText(" ");
        }
        lblTotal.setText("Total: " + total);
        if (!"Tunai".equals(metodeTerpilih())) {
            txtBayar.setText(String.valueOf(total));
            txtBayar.setEnabled(false);
        } else {
            txtBayar.setEnabled(true);
        }
        hitungKembalian();
    }

    private void hitungKembalian() {
        double total = hitungTotal();
        double bayar;
        try {
            bayar = Double.parseDouble(txtBayar.getText().trim());
        } catch (NumberFormatException | NullPointerException e) {
            lblKembalian.setText("Kembalian: 0");
            return;
        }
        lblKembalian.setText("Kembalian: " + (bayar - total));
    }

    private void doSimpan() {
        if (Sesi.userLogin == null) {
            JOptionPane.showMessageDialog(this, "Sesi habis, login ulang",
                    "Sesi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (cart.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Keranjang masih kosong",
                    "Validasi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        double subtotal = hitungSubtotal();
        double persen = bacaPersen();
        double nominal = bacaPotongan();
        if (Double.isNaN(persen)) persen = 0;
        if (Double.isNaN(nominal)) nominal = 0;
        double sisaSetelahPersen = subtotal - Math.round(subtotal * persen / 100.0);
        String errDiskon = validasiDiskon(persen, nominal, sisaSetelahPersen);
        if (errDiskon != null) {
            JOptionPane.showMessageDialog(this, errDiskon,
                    "Validasi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        double total = hitungTotal(subtotal, persen, nominal);
        String metode = metodeTerpilih();
        Double bayarBox;
        if ("Tunai".equals(metode)) {
            try {
                bayarBox = Double.parseDouble(txtBayar.getText().trim());
            } catch (NumberFormatException e) {
                bayarBox = null;
            }
        } else {
            bayarBox = total;
        }
        String errBayar = validasiBayar(metode, bayarBox, total);
        if (errBayar != null) {
            JOptionPane.showMessageDialog(this, errBayar,
                    "Validasi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        double bayar = bayarBox;
        for (CartRow r : cart) {
            Buku fresh = bukuDAO.getById(r.buku.getIdBuku());
            if (fresh == null || r.qty > fresh.getStok()) {
                JOptionPane.showMessageDialog(this,
                        "Stok tidak mencukupi untuk " + r.buku.getJudul(),
                        "Stok", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        try {
            String noNota = NotaGenerator.next("NJ", "penjualan", "no_nota");
            double diskonPersenRp = Math.round(subtotal * persen / 100.0);
            Penjualan h = new Penjualan();
            h.setNoNota(noNota);
            h.setTanggal(LocalDateTime.now());
            h.setIdUser(Sesi.userLogin.getIdUser());
            h.setTotal(total);
            h.setBayar(bayar);
            h.setKembalian(bayar - total);
            h.setMetodeBayar(metode);
            h.setDiskon(diskonPersenRp + nominal);
            List<DetailPenjualan> items = new ArrayList<>();
            for (CartRow r : cart) {
                DetailPenjualan d = new DetailPenjualan();
                d.setIdBuku(r.buku.getIdBuku());
                d.setKodeBuku(r.buku.getKodeBuku());
                d.setJudul(r.buku.getJudul());
                d.setQty(r.qty);
                d.setHargaJual(r.buku.getHargaJual());
                d.setSubtotal(r.qty * r.buku.getHargaJual());
                d.setSatuan(r.buku.getSatuan());
                items.add(d);
            }
            penjualanDAO.saveWithDetail(h, items);
            String kasir = Sesi.userLogin.getNamaLengkap();
            Frame owner = (Frame) SwingUtilities.getWindowAncestor(this);
            new StrukDialog(owner, h, items, kasir, "-", persen, diskonPersenRp, nominal).setVisible(true);
            cart.clear();
            txtBayar.setText("");
            cmbMetode.setSelectedItem("Tunai");
            txtDiskonPersen.setText("0");
            txtPotongan.setText("0");
            refreshCart();
            cariBuku(txtCari.getText());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal simpan penjualan: " + ex.getMessage(),
                    "Gagal", JOptionPane.ERROR_MESSAGE);
        }
    }
}
