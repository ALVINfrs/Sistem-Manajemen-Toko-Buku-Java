package view;

import dao.UserDAO;
import dao.UserDAOImpl;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import model.User;
import util.HashUtil;
import util.NeoBrutalTheme;
import util.Sesi;

public class ProfilSaya extends JDialog {

    private final Runnable onSaved;
    private final UserDAO userDAO = new UserDAOImpl();

    private JLabel lblUsername;
    private JTextField txtNama;
    private JPasswordField pfBaru;
    private JPasswordField pfKonfirmasi;

    public ProfilSaya(Frame owner, Runnable onSaved) {
        super(owner, "Profil Saya", true);
        this.onSaved = onSaved;

        setLayout(new BorderLayout(12, 12));
        getContentPane().setBackground(NeoBrutalTheme.BG);
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel pnlHeader = new JPanel(new BorderLayout(0, 2));
        pnlHeader.setOpaque(false);
        pnlHeader.add(NeoFormStyle.section("Akun Saya"), BorderLayout.NORTH);
        JLabel lblJudul = new JLabel("Profil Saya");
        lblJudul.setFont(NeoBrutalTheme.display(20));
        lblJudul.setForeground(NeoBrutalTheme.INK);
        pnlHeader.add(lblJudul, BorderLayout.SOUTH);
        add(pnlHeader, BorderLayout.NORTH);

        JPanel form = NeoFormStyle.card(new GridLayout(0, 2, 8, 8));

        String username = Sesi.userLogin != null ? Sesi.userLogin.getUsername() : "-";
        String nama = Sesi.userLogin != null ? Sesi.userLogin.getNamaLengkap() : "";
        lblUsername = new JLabel(username);
        lblUsername.setName("profil_username");
        lblUsername.setFont(NeoBrutalTheme.body(java.awt.Font.BOLD, 13));
        lblUsername.setForeground(NeoBrutalTheme.INK);
        txtNama = NeoFormStyle.field("profil_nama_field");
        txtNama.setText(nama);
        pfBaru = new JPasswordField();
        pfBaru.setName("profil_pass_baru");
        pfBaru.setFont(NeoBrutalTheme.body(java.awt.Font.PLAIN, 13));
        pfBaru.setBackground(NeoBrutalTheme.SURFACE);
        pfBaru.setForeground(NeoBrutalTheme.INK);
        pfBaru.setCaretColor(NeoBrutalTheme.INK);
        pfBaru.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(NeoBrutalTheme.INK, 2),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)));
        pfKonfirmasi = new JPasswordField();
        pfKonfirmasi.setName("profil_pass_konfirmasi");
        pfKonfirmasi.setFont(NeoBrutalTheme.body(java.awt.Font.PLAIN, 13));
        pfKonfirmasi.setBackground(NeoBrutalTheme.SURFACE);
        pfKonfirmasi.setForeground(NeoBrutalTheme.INK);
        pfKonfirmasi.setCaretColor(NeoBrutalTheme.INK);
        pfKonfirmasi.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(NeoBrutalTheme.INK, 2),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)));

        form.add(NeoFormStyle.label("Username"));
        form.add(lblUsername);
        form.add(NeoFormStyle.label("Nama Lengkap"));
        form.add(txtNama);
        form.add(NeoFormStyle.label("Password Baru"));
        form.add(pfBaru);
        form.add(NeoFormStyle.label("Konfirmasi"));
        form.add(pfKonfirmasi);
        add(form, BorderLayout.CENTER);

        JPanel aksi = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 8, 0));
        aksi.setOpaque(false);
        JButton btnSimpan = NeoFormStyle.button("Simpan", null, NeoBrutalTheme.SUCCESS);
        btnSimpan.setName("profil_simpan");
        btnSimpan.addActionListener(e -> simpan());
        JButton btnBatal = NeoFormStyle.button("Batal", null, NeoBrutalTheme.SECONDARY);
        btnBatal.addActionListener(e -> dispose());
        aksi.add(btnSimpan);
        aksi.add(btnBatal);
        add(aksi, BorderLayout.SOUTH);

        pack();
        setSize(420, getHeight());
        setLocationRelativeTo(owner);
    }

    private void simpan() {
        if (Sesi.userLogin == null) {
            JOptionPane.showMessageDialog(this, "Sesi login tidak ditemukan",
                    "Validasi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String nama = txtNama.getText().trim();
        if (nama.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nama wajib diisi",
                    "Validasi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String baru = new String(pfBaru.getPassword());
        String konfirmasi = new String(pfKonfirmasi.getPassword());
        boolean gantiPassword = !baru.isEmpty() || !konfirmasi.isEmpty();
        if (gantiPassword) {
            if (baru.length() < 4) {
                JOptionPane.showMessageDialog(this, "Password minimal 4 karakter",
                        "Validasi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!baru.equals(konfirmasi)) {
                JOptionPane.showMessageDialog(this, "Konfirmasi password tidak sama",
                        "Validasi", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        try {
            User u = userDAO.getById(Sesi.userLogin.getIdUser());
            if (u == null) {
                JOptionPane.showMessageDialog(this, "User tidak ditemukan",
                        "Gagal", JOptionPane.ERROR_MESSAGE);
                return;
            }
            u.setNamaLengkap(nama);
            if (gantiPassword) {
                u.setPassword(HashUtil.sha256(baru));
            }
            userDAO.update(u);
            Sesi.userLogin = u;
            if (onSaved != null) {
                onSaved.run();
            }
            JOptionPane.showMessageDialog(this, "Profil tersimpan",
                    "Sukses", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal simpan profil: " + ex.getMessage(),
                    "Gagal", JOptionPane.ERROR_MESSAGE);
        }
    }
}
