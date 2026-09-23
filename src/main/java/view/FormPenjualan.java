package view;

import dao.BukuDAO;
import dao.BukuDAOImpl;
import dao.MemberDAO;
import dao.MemberDAOImpl;
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
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import model.Buku;
import model.DetailPenjualan;
import model.Member;
import model.Penjualan;
import org.kordamp.ikonli.materialdesign2.MaterialDesignC;
import org.kordamp.ikonli.materialdesign2.MaterialDesignD;
import org.kordamp.ikonli.materialdesign2.MaterialDesignM;
import org.kordamp.ikonli.swing.FontIcon;
import util.ComboItem;
import util.NeoBrutalTheme;
import util.NeoShadowBorder;
import util.NotaGenerator;
import util.Sesi;

public class FormPenjualan extends JPanel {

    private final BukuDAO bukuDAO = new BukuDAOImpl();
    private final MemberDAO memberDAO = new MemberDAOImpl();
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
    private JComboBox<ComboItem> cmbMember;
    private JLabel lblTotal;
    private JLabel lblKembalian;
    private JTextField txtBayar;

    public FormPenjualan() {
        setLayout(new BorderLayout(12, 12));
        setBackground(NeoBrutalTheme.BG);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JLabel lblJudul = new JLabel("Penjualan (POS)");
        lblJudul.setFont(new Font("Segoe UI Black", Font.BOLD, 22));
        add(lblJudul, BorderLayout.NORTH);

        JPanel cols = new JPanel(new GridLayout(1, 3, 12, 0));
        cols.setBackground(NeoBrutalTheme.BG);
        cols.add(buildKiri());
        cols.add(buildTengah());
        cols.add(buildKanan());
        add(cols, BorderLayout.CENTER);

        reloadMember();
        refreshCart();
    }

    private JPanel buildKiri() {
        JPanel card = new JPanel(new BorderLayout(8, 8));
        card.setBackground(NeoBrutalTheme.SURFACE);
        card.setBorder(new NeoShadowBorder());

        JLabel lbl = new JLabel("Cari Buku");
        lbl.setFont(new Font("Segoe UI Semibold", Font.BOLD, 14));
        card.add(lbl, BorderLayout.NORTH);

        JPanel pnlCari = new JPanel(new BorderLayout(6, 6));
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
        card.add(pnlCari, BorderLayout.CENTER);

        modelHasil = new DefaultTableModel(new String[]{"Kode", "Judul", "Harga", "Stok"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblHasil = new JTable(modelHasil);
        tblHasil.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) tambahKeKeranjang();
            }
        });
        JScrollPane scroll = new JScrollPane(tblHasil);
        scroll.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));

        JButton btnTambah = styledButton("Tambah", null, NeoBrutalTheme.SUCCESS);
        btnTambah.addActionListener(e -> tambahKeKeranjang());

        JPanel pnlBawah = new JPanel(new BorderLayout(6, 6));
        pnlBawah.setBackground(NeoBrutalTheme.SURFACE);
        pnlBawah.add(scroll, BorderLayout.CENTER);
        pnlBawah.add(btnTambah, BorderLayout.SOUTH);
        card.add(pnlBawah, BorderLayout.SOUTH);
        return card;
    }

    private JPanel buildTengah() {
        JPanel card = new JPanel(new BorderLayout(8, 8));
        card.setBackground(NeoBrutalTheme.SURFACE);
        card.setBorder(new NeoShadowBorder());

        JLabel lbl = new JLabel("Keranjang");
        lbl.setFont(new Font("Segoe UI Semibold", Font.BOLD, 14));
        card.add(lbl, BorderLayout.NORTH);

        modelCart = new DefaultTableModel(new String[]{"Kode", "Judul", "Harga", "Qty", "Subtotal"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblCart = new JTable(modelCart);
        JScrollPane scroll = new JScrollPane(tblCart);
        scroll.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        card.add(scroll, BorderLayout.CENTER);

        JPanel pnlQty = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pnlQty.setBackground(NeoBrutalTheme.SURFACE);
        JButton btnPlus = styledButton("+", null, NeoBrutalTheme.SUCCESS);
        JButton btnMinus = styledButton("-", null, NeoBrutalTheme.WARNING);
        JButton btnHapus = styledButton("Hapus",
                FontIcon.of(MaterialDesignD.DELETE, 18, Color.BLACK),
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
        JPanel card = new JPanel(new GridLayout(0, 1, 6, 6));
        card.setBackground(NeoBrutalTheme.SURFACE);
        card.setBorder(new NeoShadowBorder());

        JLabel lbl = new JLabel("Ringkasan");
        lbl.setFont(new Font("Segoe UI Semibold", Font.BOLD, 14));
        card.add(lbl);

        card.add(new JLabel("Member"));
        cmbMember = new JComboBox<>();
        cmbMember.setBackground(NeoBrutalTheme.SURFACE);
        cmbMember.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        card.add(cmbMember);

        lblTotal = new JLabel("Total: 0");
        lblTotal.setFont(new Font("Segoe UI Black", Font.BOLD, 22));
        lblTotal.setForeground(NeoBrutalTheme.PRIMARY);
        card.add(lblTotal);

        card.add(new JLabel("Bayar"));
        txtBayar = new JTextField();
        txtBayar.setName("bayar");
        txtBayar.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        txtBayar.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { hitungKembalian(); }
            @Override public void removeUpdate(DocumentEvent e) { hitungKembalian(); }
            @Override public void changedUpdate(DocumentEvent e) { hitungKembalian(); }
        });
        card.add(txtBayar);

        lblKembalian = new JLabel("Kembalian: 0");
        lblKembalian.setFont(new Font("Segoe UI Black", Font.BOLD, 20));
        lblKembalian.setForeground(NeoBrutalTheme.PRIMARY);
        card.add(lblKembalian);

        JButton btnBayar = styledButton("Bayar / Simpan",
                FontIcon.of(MaterialDesignC.CONTENT_SAVE, 18, Color.BLACK),
                NeoBrutalTheme.PRIMARY);
        btnBayar.addActionListener(e -> doSimpan());
        card.add(btnBayar);
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

    private void reloadMember() {
        cmbMember.removeAllItems();
        cmbMember.addItem(ComboItem.EMPTY);
        for (Member m : memberDAO.getAll()) {
            cmbMember.addItem(new ComboItem(m.getIdMember(), m.getNama()));
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
            modelHasil.addRow(new Object[]{b.getKodeBuku(), b.getJudul(), b.getHargaJual(), b.getStok()});
        }
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

    private double hitungTotal() {
        double total = 0;
        for (CartRow r : cart) {
            total += r.qty * r.buku.getHargaJual();
        }
        return total;
    }

    private void refreshCart() {
        modelCart.setRowCount(0);
        for (CartRow r : cart) {
            modelCart.addRow(new Object[]{
                    r.buku.getKodeBuku(), r.buku.getJudul(), r.buku.getHargaJual(),
                    r.qty, r.qty * r.buku.getHargaJual()
            });
        }
        double total = hitungTotal();
        lblTotal.setText("Total: " + total);
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
        double total = hitungTotal();
        double bayar;
        try {
            bayar = Double.parseDouble(txtBayar.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Uang bayar harus angka",
                    "Validasi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (bayar < total) {
            JOptionPane.showMessageDialog(this, "Uang bayar kurang",
                    "Validasi", JOptionPane.ERROR_MESSAGE);
            return;
        }
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
            ComboItem mi = (ComboItem) cmbMember.getSelectedItem();
            Integer idMember = (mi == null || mi.id < 0) ? null : mi.id;
            Penjualan h = new Penjualan();
            h.setNoNota(noNota);
            h.setTanggal(LocalDateTime.now());
            h.setIdUser(Sesi.userLogin.getIdUser());
            h.setIdMember(idMember);
            h.setTotal(total);
            h.setBayar(bayar);
            h.setKembalian(bayar - total);
            List<DetailPenjualan> items = new ArrayList<>();
            for (CartRow r : cart) {
                DetailPenjualan d = new DetailPenjualan();
                d.setIdBuku(r.buku.getIdBuku());
                d.setKodeBuku(r.buku.getKodeBuku());
                d.setJudul(r.buku.getJudul());
                d.setQty(r.qty);
                d.setHargaJual(r.buku.getHargaJual());
                d.setSubtotal(r.qty * r.buku.getHargaJual());
                items.add(d);
            }
            penjualanDAO.saveWithDetail(h, items);
            String kasir = Sesi.userLogin.getNamaLengkap();
            String member = (mi == null || mi.id < 0) ? "-" : mi.label;
            Frame owner = (Frame) SwingUtilities.getWindowAncestor(this);
            new StrukDialog(owner, h, items, kasir, member).setVisible(true);
            cart.clear();
            txtBayar.setText("");
            refreshCart();
            cariBuku(txtCari.getText());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal simpan penjualan: " + ex.getMessage(),
                    "Gagal", JOptionPane.ERROR_MESSAGE);
        }
    }
}
