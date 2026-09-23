package view;

import dao.PenjualanDAO;
import dao.PenjualanDAOImpl;
import dao.ReturDAO;
import dao.ReturDAOImpl;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import model.DetailPenjualan;
import model.Penjualan;
import model.Retur;
import org.kordamp.ikonli.materialdesign2.MaterialDesignC;
import org.kordamp.ikonli.materialdesign2.MaterialDesignM;
import org.kordamp.ikonli.swing.FontIcon;
import util.NeoBrutalTheme;
import util.NeoShadowBorder;
import util.NotaGenerator;
import util.Sesi;

public class FormRetur extends JPanel {

    private final PenjualanDAO penjualanDAO = new PenjualanDAOImpl();
    private final ReturDAO returDAO = new ReturDAOImpl();

    private Penjualan nota;
    private List<DetailPenjualan> detailNota = new ArrayList<>();
    private List<Penjualan> daftarNota = new ArrayList<>();
    private List<Penjualan> daftarTampil = new ArrayList<>();

    private JTextField txtNoNota;
    private JTextField txtFilter;
    private JTable tblNota;
    private DefaultTableModel modelNota;
    private JLabel lblInfo;
    private JTable tblItem;
    private DefaultTableModel modelItem;
    private JTextField txtQty;
    private JTextField txtAlasan;

    public FormRetur() {
        setLayout(new BorderLayout(12, 12));
        setBackground(NeoBrutalTheme.BG);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JLabel lblJudul = new JLabel("Retur Penjualan");
        lblJudul.setFont(new Font("Segoe UI Black", Font.BOLD, 22));
        add(lblJudul, BorderLayout.NORTH);

        JPanel cols = new JPanel(new GridLayout(1, 2, 12, 0));
        cols.setBackground(NeoBrutalTheme.BG);
        cols.add(buildKiri());
        cols.add(buildKanan());
        add(cols, BorderLayout.CENTER);

        loadTable();
    }

    private JPanel buildKiri() {
        JPanel card = new JPanel(new BorderLayout(8, 8));
        card.setBackground(NeoBrutalTheme.SURFACE);
        card.setBorder(new NeoShadowBorder());

        JLabel lbl = new JLabel("Cari Nota");
        lbl.setFont(new Font("Segoe UI Semibold", Font.BOLD, 14));
        card.add(lbl, BorderLayout.NORTH);

        JPanel pnlCari = new JPanel(new BorderLayout(6, 0));
        pnlCari.setBackground(NeoBrutalTheme.SURFACE);
        txtNoNota = new JTextField();
        txtNoNota.setName("no_nota");
        txtNoNota.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        JButton btnCari = styledButton("Cari",
                FontIcon.of(MaterialDesignM.MAGNIFY, 18, Color.BLACK),
                NeoBrutalTheme.SECONDARY);
        btnCari.addActionListener(e -> cariNota());
        pnlCari.add(txtNoNota, BorderLayout.CENTER);
        pnlCari.add(btnCari, BorderLayout.EAST);

        JPanel pnlTengah = new JPanel(new BorderLayout(6, 6));
        pnlTengah.setBackground(NeoBrutalTheme.SURFACE);
        pnlTengah.add(pnlCari, BorderLayout.NORTH);
        pnlTengah.add(buildDaftar(), BorderLayout.CENTER);
        card.add(pnlTengah, BorderLayout.CENTER);

        lblInfo = new JLabel("50 nota terbaru — ketik untuk filter");
        lblInfo.setFont(new Font("Segoe UI Semibold", Font.BOLD, 13));
        JPanel pnlInfo = new JPanel(new BorderLayout());
        pnlInfo.setBackground(NeoBrutalTheme.SURFACE);
        pnlInfo.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        pnlInfo.add(lblInfo, BorderLayout.NORTH);
        card.add(pnlInfo, BorderLayout.SOUTH);
        return card;
    }

    private JPanel buildDaftar() {
        JPanel pnl = new JPanel(new BorderLayout(6, 6));
        pnl.setBackground(NeoBrutalTheme.SURFACE);
        txtFilter = new JTextField();
        txtFilter.setName("filter_nota");
        txtFilter.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        txtFilter.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { tampilkanDaftar(txtFilter.getText()); }
            @Override public void removeUpdate(DocumentEvent e) { tampilkanDaftar(txtFilter.getText()); }
            @Override public void changedUpdate(DocumentEvent e) { tampilkanDaftar(txtFilter.getText()); }
        });
        pnl.add(txtFilter, BorderLayout.NORTH);

        modelNota = new DefaultTableModel(
                new String[]{"No Nota", "Tanggal", "Total"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblNota = new JTable(modelNota);
        tblNota.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tblNota.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) pilihNotaDariDaftar();
        });
        JScrollPane scroll = new JScrollPane(tblNota);
        scroll.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        pnl.add(scroll, BorderLayout.CENTER);
        return pnl;
    }

    private JPanel buildKanan() {
        JPanel card = new JPanel(new BorderLayout(8, 8));
        card.setBackground(NeoBrutalTheme.SURFACE);
        card.setBorder(new NeoShadowBorder());

        JLabel lbl = new JLabel("Item Nota");
        lbl.setFont(new Font("Segoe UI Semibold", Font.BOLD, 14));
        card.add(lbl, BorderLayout.NORTH);

        modelItem = new DefaultTableModel(
                new String[]{"No", "Kode", "Judul", "Qty Beli", "Sudah Diretur", "Sisa"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblItem = new JTable(modelItem);
        JScrollPane scroll = new JScrollPane(tblItem);
        scroll.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        card.add(scroll, BorderLayout.CENTER);

        JPanel pnlRetur = new JPanel(new GridLayout(0, 2, 6, 6));
        pnlRetur.setBackground(NeoBrutalTheme.SURFACE);
        txtQty = new JTextField();
        txtQty.setName("qty_retur");
        txtQty.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        txtAlasan = new JTextField();
        txtAlasan.setName("alasan");
        txtAlasan.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        pnlRetur.add(new JLabel("Qty Retur"));
        pnlRetur.add(new JLabel("Alasan"));
        pnlRetur.add(txtQty);
        pnlRetur.add(txtAlasan);

        JButton btnSimpan = styledButton("Simpan Retur",
                FontIcon.of(MaterialDesignC.CONTENT_SAVE, 18, Color.BLACK),
                NeoBrutalTheme.PRIMARY);
        btnSimpan.addActionListener(e -> doSimpan());

        JPanel pnlBawah = new JPanel(new BorderLayout(6, 6));
        pnlBawah.setBackground(NeoBrutalTheme.SURFACE);
        pnlBawah.add(pnlRetur, BorderLayout.CENTER);
        JPanel pnlBtn = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnlBtn.setBackground(NeoBrutalTheme.SURFACE);
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

    public void loadTable() {
        daftarNota = penjualanDAO.listTerbaru(50);
        tampilkanDaftar(txtFilter != null ? txtFilter.getText() : "");
    }

    private void tampilkanDaftar(String keyword) {
        String kw = keyword == null ? "" : keyword.trim().toLowerCase();
        daftarTampil = new ArrayList<>();
        for (Penjualan p : daftarNota) {
            if (kw.isEmpty() || (p.getNoNota() != null
                    && p.getNoNota().toLowerCase().contains(kw))) {
                daftarTampil.add(p);
            }
        }
        modelNota.setRowCount(0);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        for (Penjualan p : daftarTampil) {
            String tgl = p.getTanggal() != null ? p.getTanggal().format(fmt) : "-";
            modelNota.addRow(new Object[]{p.getNoNota(), tgl, p.getTotal()});
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
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        String tgl = p.getTanggal() != null ? p.getTanggal().format(fmt) : "-";
        lblInfo.setText("Nota: " + p.getNoNota() + " | Tgl: " + tgl + " | Total: " + p.getTotal());
        refreshItem();
    }

    private void cariNota() {
        String noNota = txtNoNota.getText().trim();
        if (noNota.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Isi no nota dulu",
                    "Validasi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        Penjualan p = penjualanDAO.getByNoNota(noNota);
        if (p == null) {
            JOptionPane.showMessageDialog(this, "Nota tidak ditemukan",
                    "Validasi", JOptionPane.ERROR_MESSAGE);
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
                    no++, d.getKodeBuku(), d.getJudul(), d.getQty(), sudah, sisa
            });
        }
    }

    private void doSimpan() {
        if (Sesi.userLogin == null) {
            JOptionPane.showMessageDialog(this, "Sesi habis, login ulang",
                    "Sesi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (nota == null) {
            JOptionPane.showMessageDialog(this, "Cari nota dulu",
                    "Validasi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int row = tblItem.getSelectedRow();
        if (row < 0 || row >= detailNota.size()) {
            JOptionPane.showMessageDialog(this, "Pilih baris item nota dulu",
                    "Validasi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int qtyRetur;
        try {
            qtyRetur = Integer.parseInt(txtQty.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Qty retur harus bilangan bulat",
                    "Validasi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        DetailPenjualan d = detailNota.get(row);
        int sudah = returDAO.getReturQty(nota.getIdPenjualan(), d.getIdBuku());
        int sisa = d.getQty() - sudah;
        if (qtyRetur <= 0 || qtyRetur > sisa) {
            JOptionPane.showMessageDialog(this,
                    "Qty retur melebihi sisa " + sisa + " untuk " + d.getJudul(),
                    "Validasi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            String noRetur = NotaGenerator.next("RT", "retur", "no_retur");
            Retur r = new Retur();
            r.setNoRetur(noRetur);
            r.setTanggal(LocalDateTime.now());
            r.setIdPenjualan(nota.getIdPenjualan());
            r.setIdBuku(d.getIdBuku());
            r.setQty(qtyRetur);
            r.setAlasan(txtAlasan.getText().trim());
            returDAO.saveRetur(r);
            JOptionPane.showMessageDialog(this, "Retur tersimpan, stok bertambah");
            txtQty.setText("");
            txtAlasan.setText("");
            refreshItem();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal simpan retur: " + ex.getMessage(),
                    "Gagal", JOptionPane.ERROR_MESSAGE);
        }
    }
}
