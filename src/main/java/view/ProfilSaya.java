package view;

import dao.UserDAO;
import dao.UserDAOImpl;
import java.awt.BorderLayout;
import java.awt.Color;
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
import util.NeoShadowBorder;
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

        JLabel lblJudul = new JLabel("Profil Saya");
        lblJudul.setFont(new Font("Segoe UI Black", Font.BOLD, 20));
        add(lblJudul, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.setBackground(NeoBrutalTheme.SURFACE);
        form.setBorder(BorderFactory.createCompoundBorder(
                new NeoShadowBorder(),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));

        String username = Sesi.userLogin != null ? Sesi.userLogin.getUsername() : "-";
        String nama = Sesi.userLogin != null ? Sesi.userLogin.getNamaLengkap() : "";
        lblUsername = new JLabel(username);
        lblUsername.setName("profil_username");
        lblUsername.setFont(new Font("Segoe UI Semibold", Font.BOLD, 13));
        txtNama = new JTextField(nama);
        txtNama.setName("profil_nama_field");
        txtNama.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        pfBaru = new JPasswordField();
        pfBaru.setName("profil_pass_baru");
        pfBaru.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        pfKonfirmasi = new JPasswordField();
        pfKonfirmasi.setName("profil_pass_konfirmasi");
        pfKonfirmasi.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));

        form.add(new JLabel("Username"));
        form.add(lblUsername);
        form.add(new JLabel("Nama Lengkap"));
        form.add(txtNama);
        form.add(new JLabel("Password Baru"));
        form.add(pfBaru);
        form.add(new JLabel("Konfirmasi"));
        form.add(pfKonfirmasi);
        add(form, BorderLayout.CENTER);

        JPanel aksi = new JPanel();
        aksi.setBackground(NeoBrutalTheme.BG);
        JButton btnSimpan = new JButton("Simpan");
        btnSimpan.setName("profil_simpan");
        btnSimpan.setFont(new Font("Segoe UI Semibold", Font.BOLD, 13));
        btnSimpan.setBackground(NeoBrutalTheme.SUCCESS);
        btnSimpan.setForeground(Color.BLACK);
        btnSimpan.setBorder(new NeoShadowBorder());
        btnSimpan.setFocusPainted(false);
        btnSimpan.addActionListener(e -> simpan());
        JButton btnBatal = new JButton("Batal");
        btnBatal.setFont(new Font("Segoe UI Semibold", Font.BOLD, 13));
        btnBatal.setBackground(NeoBrutalTheme.SURFACE);
        btnBatal.setForeground(Color.BLACK);
        btnBatal.setBorder(new NeoShadowBorder());
        btnBatal.setFocusPainted(false);
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
