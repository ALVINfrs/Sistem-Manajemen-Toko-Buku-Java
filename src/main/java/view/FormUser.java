package view;

import dao.UserDAO;
import dao.UserDAOImpl;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import model.User;
import org.kordamp.ikonli.materialdesign2.MaterialDesignC;
import org.kordamp.ikonli.materialdesign2.MaterialDesignD;
import org.kordamp.ikonli.materialdesign2.MaterialDesignM;
import org.kordamp.ikonli.materialdesign2.MaterialDesignP;
import org.kordamp.ikonli.swing.FontIcon;
import util.HashUtil;
import util.NeoBrutalTheme;
import util.Sesi;
import util.Validasi;

public class FormUser extends JPanel {

    private final UserDAO userDAO = new UserDAOImpl();

    private int selectedId = -1;

    private JTable table;
    private DefaultTableModel model;
    private JTextField txtSearch;
    private JTextField txtUsername;
    private JTextField txtNama;
    private JComboBox<String> cmbRole;
    private JPasswordField txtPassword;
    private JPasswordField txtKonfirmasi;

    public FormUser() {
        setLayout(new BorderLayout(12, 12));
        setBackground(NeoBrutalTheme.BG);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        add(buildNorth(), BorderLayout.NORTH);

        JPanel cardInput = buildInputCard();

        model = new DefaultTableModel(new String[]{"ID", "Username", "Nama Lengkap", "Role"}, 0) {
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

        return new NeoPageHeader("MASTER DATA", "Data User",
                "Kelola akun dan hak akses pengguna.", pnlSearch);
    }

    private JPanel buildInputCard() {
        JPanel card = NeoFormStyle.card(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 8, 4, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        txtUsername = NeoFormStyle.field("username");
        txtNama = NeoFormStyle.field("nama_lengkap");
        cmbRole = new JComboBox<>(new String[]{"Admin", "Kasir"});
        cmbRole.setName("role");
        NeoFormStyle.styleCombo(cmbRole);
        txtPassword = new JPasswordField(16);
        txtPassword.setName("password");
        txtPassword.setBorder(BorderFactory.createLineBorder(NeoBrutalTheme.INK, 2));
        txtKonfirmasi = new JPasswordField(16);
        txtKonfirmasi.setName("konfirmasi_password");
        txtKonfirmasi.setBorder(BorderFactory.createLineBorder(NeoBrutalTheme.INK, 2));

        int y = 0;
        y = addRow(card, gbc, y, "Username", txtUsername);
        y = addRow(card, gbc, y, "Nama Lengkap", txtNama);
        y = addRow(card, gbc, y, "Role", cmbRole);
        y = addRow(card, gbc, y, "Password", txtPassword);
        y = addRow(card, gbc, y, "Konfirmasi", txtKonfirmasi);

        JLabel lblHint = new JLabel("Kosongkan password bila tidak diubah (mode edit)");
        lblHint.setFont(NeoBrutalTheme.body(java.awt.Font.PLAIN, 11));
        lblHint.setForeground(NeoBrutalTheme.TEXT_MUTED);
        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        card.add(lblHint, gbc);

        gbc.gridx = 0;
        gbc.gridy = y + 1;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        card.add(javax.swing.Box.createGlue(), gbc);
        return card;
    }


    private int addRow(JPanel card, GridBagConstraints gbc, int y, String label,
            java.awt.Component comp) {
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.weightx = 0;
        card.add(NeoFormStyle.section(label), gbc);
        gbc.gridx = 1;
        gbc.gridy = y;
        gbc.weightx = 1.0;
        card.add(comp, gbc);
        return y + 1;
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
        isiTabel(userDAO.getAll());
    }

    public void cari(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            loadTable();
            return;
        }
        isiTabel(userDAO.search(keyword.trim()));
    }

    private void isiTabel(List<User> list) {
        model.setRowCount(0);
        for (User u : list) {
            model.addRow(new Object[]{u.getIdUser(), u.getUsername(),
                    u.getNamaLengkap(), u.getRole()});
        }
    }

    private void isiFieldDariBaris() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        int id = (int) model.getValueAt(row, 0);
        User u = userDAO.getById(id);
        if (u == null) return;
        selectedId = u.getIdUser();
        txtUsername.setText(u.getUsername() != null ? u.getUsername() : "");
        txtNama.setText(u.getNamaLengkap() != null ? u.getNamaLengkap() : "");
        cmbRole.setSelectedItem(u.getRole());
        txtPassword.setText("");
        txtKonfirmasi.setText("");
    }

    private boolean sesiValid() {
        if (Sesi.userLogin == null) {
            JOptionPane.showMessageDialog(this, "Sesi habis, login ulang",
                    "Sesi", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    private boolean passwordSamaDanKuat(String pass, String konf) {
        if (pass.length() < 4) {
            JOptionPane.showMessageDialog(this, "Password minimal 4 karakter",
                    "Validasi", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (!pass.equals(konf)) {
            JOptionPane.showMessageDialog(this, "Password dan konfirmasi tidak sama",
                    "Validasi", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    private void doSimpan() {
        if (!sesiValid()) return;
        if (!Validasi.wajib(txtUsername, txtNama)) return;
        String pass = new String(txtPassword.getPassword());
        String konf = new String(txtKonfirmasi.getPassword());
        if (pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Password wajib diisi",
                    "Validasi", JOptionPane.ERROR_MESSAGE);
            txtPassword.requestFocus();
            return;
        }
        if (!passwordSamaDanKuat(pass, konf)) return;
        if (userDAO.getByUsername(txtUsername.getText().trim()) != null) {
            JOptionPane.showMessageDialog(this, "Username sudah dipakai",
                    "Gagal Simpan", JOptionPane.ERROR_MESSAGE);
            return;
        }
        User u = new User();
        u.setUsername(txtUsername.getText().trim());
        u.setNamaLengkap(txtNama.getText().trim());
        u.setRole((String) cmbRole.getSelectedItem());
        u.setPassword(HashUtil.sha256(pass));
        try {
            userDAO.insert(u);
            reset();
            loadTable();
            JOptionPane.showMessageDialog(this, "User tersimpan");
        } catch (RuntimeException ex) {
            if (isIntegrityViolation(ex)) {
                JOptionPane.showMessageDialog(this, "Username sudah dipakai",
                        "Gagal Simpan", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Gagal simpan user: " + ex.getMessage(),
                        "Gagal Simpan", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void doEdit() {
        if (!sesiValid()) return;
        if (selectedId < 0) {
            JOptionPane.showMessageDialog(this, "Pilih data user dulu (klik baris tabel)");
            return;
        }
        if (!Validasi.wajib(txtNama)) return;
        User lama = userDAO.getById(selectedId);
        if (lama == null) {
            JOptionPane.showMessageDialog(this, "Data user tidak ditemukan",
                    "Gagal Edit", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String pass = new String(txtPassword.getPassword());
        String konf = new String(txtKonfirmasi.getPassword());
        String passwordHash = lama.getPassword();
        if (!pass.isEmpty() || !konf.isEmpty()) {
            if (!passwordSamaDanKuat(pass, konf)) return;
            passwordHash = HashUtil.sha256(pass);
        }
        User u = new User();
        u.setIdUser(selectedId);
        u.setUsername(lama.getUsername());
        u.setNamaLengkap(txtNama.getText().trim());
        u.setRole((String) cmbRole.getSelectedItem());
        u.setPassword(passwordHash);
        try {
            userDAO.update(u);
            reset();
            loadTable();
            JOptionPane.showMessageDialog(this, "User terupdate");
        } catch (RuntimeException ex) {
            if (isIntegrityViolation(ex)) {
                JOptionPane.showMessageDialog(this, "Username sudah dipakai",
                        "Gagal Edit", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Gagal edit user: " + ex.getMessage(),
                        "Gagal Edit", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void doHapus() {
        if (!sesiValid()) return;
        if (selectedId < 0) {
            JOptionPane.showMessageDialog(this, "Pilih data user dulu (klik baris tabel)");
            return;
        }
        int y = JOptionPane.showConfirmDialog(this, "Hapus user ini?",
                "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (y != JOptionPane.YES_OPTION) return;
        if (selectedId == Sesi.userLogin.getIdUser()) {
            JOptionPane.showMessageDialog(this, "Tidak bisa menghapus akun sendiri",
                    "Gagal Hapus", JOptionPane.ERROR_MESSAGE);
            return;
        }
        User target = userDAO.getById(selectedId);
        if (target != null && "Admin".equals(target.getRole()) && hitungAdmin() <= 1) {
            JOptionPane.showMessageDialog(this, "Minimal harus ada 1 Admin",
                    "Gagal Hapus", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            userDAO.delete(selectedId);
            reset();
            loadTable();
            JOptionPane.showMessageDialog(this, "User terhapus");
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "Gagal hapus user: " + ex.getMessage(),
                    "Gagal Hapus", JOptionPane.ERROR_MESSAGE);
        }
    }

    private int hitungAdmin() {
        int n = 0;
        for (User u : userDAO.getAll()) {
            if ("Admin".equals(u.getRole())) n++;
        }
        return n;
    }

    private void reset() {
        selectedId = -1;
        txtUsername.setText("");
        txtNama.setText("");
        cmbRole.setSelectedIndex(1);
        txtPassword.setText("");
        txtKonfirmasi.setText("");
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
