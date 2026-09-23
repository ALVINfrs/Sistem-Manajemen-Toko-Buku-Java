package view;

import dao.UserDAO;
import dao.UserDAOImpl;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import model.User;
import org.kordamp.ikonli.materialdesign2.MaterialDesignA;
import org.kordamp.ikonli.materialdesign2.MaterialDesignC;
import org.kordamp.ikonli.swing.FontIcon;
import util.HashUtil;
import util.NeoBrutalTheme;
import util.NeoShadowBorder;
import util.Validasi;

public class RegisterDialog extends JDialog {

    private final UserDAO userDAO = new UserDAOImpl();

    private JTextField txtUsername;
    private JTextField txtNama;
    private JPasswordField txtPassword;
    private JPasswordField txtKonfirmasi;

    public RegisterDialog(java.awt.Frame owner) {
        super(owner, "Daftar Akun Kasir", true);
        setSize(380, 460);
        setLocationRelativeTo(owner);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout(0, 10));
        root.setBackground(NeoBrutalTheme.BG);
        root.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JPanel header = new JPanel(new BorderLayout(0, 4));
        header.setBackground(NeoBrutalTheme.BG);
        JLabel lblJudul = new JLabel("Daftar Akun Kasir", SwingConstants.CENTER);
        lblJudul.setFont(new Font("Segoe UI Black", Font.BOLD, 18));
        lblJudul.setForeground(Color.BLACK);
        JLabel lblInfo = new JLabel("Akun baru otomatis menjadi Kasir", SwingConstants.CENTER);
        lblInfo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblInfo.setForeground(Color.BLACK);
        header.add(lblJudul, BorderLayout.NORTH);
        header.add(lblInfo, BorderLayout.CENTER);
        root.add(header, BorderLayout.NORTH);

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(NeoBrutalTheme.SURFACE);
        card.setBorder(new NeoShadowBorder());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 8, 5, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        txtUsername = field("username");
        txtNama = field("nama_lengkap");
        txtPassword = new JPasswordField(16);
        txtPassword.setName("password");
        txtPassword.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        txtKonfirmasi = new JPasswordField(16);
        txtKonfirmasi.setName("konfirmasi_password");
        txtKonfirmasi.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));

        int y = 0;
        y = addRow(card, gbc, y, "Username", txtUsername);
        y = addRow(card, gbc, y, "Nama Lengkap", txtNama);
        y = addRow(card, gbc, y, "Password", txtPassword);
        y = addRow(card, gbc, y, "Konfirmasi", txtKonfirmasi);
        root.add(card, BorderLayout.CENTER);

        JPanel pnlBtn = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        pnlBtn.setBackground(NeoBrutalTheme.BG);
        JButton btnDaftar = styledButton("Daftar",
                FontIcon.of(MaterialDesignA.ACCOUNT_PLUS, 18, Color.BLACK),
                NeoBrutalTheme.PRIMARY);
        JButton btnBatal = styledButton("Batal",
                FontIcon.of(MaterialDesignC.CANCEL, 18, Color.BLACK),
                NeoBrutalTheme.SECONDARY);
        btnDaftar.addActionListener(e -> doDaftar());
        btnBatal.addActionListener(e -> dispose());
        pnlBtn.add(btnDaftar);
        pnlBtn.add(btnBatal);
        root.add(pnlBtn, BorderLayout.SOUTH);

        setContentPane(root);
        getRootPane().setDefaultButton(btnDaftar);
    }

    private JTextField field(String name) {
        JTextField f = new JTextField(16);
        f.setName(name);
        f.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        return f;
    }

    private int addRow(JPanel card, GridBagConstraints gbc, int y, String label,
            java.awt.Component comp) {
        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.weightx = 0;
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI Semibold", Font.BOLD, 13));
        card.add(lbl, gbc);
        gbc.gridx = 1;
        gbc.gridy = y;
        gbc.weightx = 1.0;
        card.add(comp, gbc);
        return y + 1;
    }

    private JButton styledButton(String teks, FontIcon ikon, Color bg) {
        JButton b = new JButton(teks, ikon);
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

    private void doDaftar() {
        if (!Validasi.wajib(txtUsername, txtNama)) return;
        String pass = new String(txtPassword.getPassword());
        String konf = new String(txtKonfirmasi.getPassword());
        if (pass.isEmpty() || konf.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Password dan konfirmasi wajib diisi",
                    "Validasi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (pass.length() < 4) {
            JOptionPane.showMessageDialog(this, "Password minimal 4 karakter",
                    "Validasi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!pass.equals(konf)) {
            JOptionPane.showMessageDialog(this, "Password dan konfirmasi tidak sama",
                    "Validasi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (userDAO.getByUsername(txtUsername.getText().trim()) != null) {
            JOptionPane.showMessageDialog(this, "Username sudah dipakai",
                    "Gagal Daftar", JOptionPane.ERROR_MESSAGE);
            return;
        }
        User u = new User();
        u.setUsername(txtUsername.getText().trim());
        u.setNamaLengkap(txtNama.getText().trim());
        u.setPassword(HashUtil.sha256(pass));
        u.setRole("Kasir");
        try {
            userDAO.insert(u);
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "Gagal daftar: " + ex.getMessage(),
                    "Gagal Daftar", JOptionPane.ERROR_MESSAGE);
            return;
        }
        JOptionPane.showMessageDialog(this, "Akun Kasir dibuat, silakan login",
                "Berhasil", JOptionPane.INFORMATION_MESSAGE);
        dispose();
    }
}
