package view;

import dao.UserDAO;
import dao.UserDAOImpl;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import model.User;
import org.kordamp.ikonli.materialdesign2.MaterialDesignA;
import org.kordamp.ikonli.swing.FontIcon;
import util.HashUtil;
import util.NeoBrutalTheme;

/**
 * Halaman penuh pendaftaran akun kasir.
 */
public class RegisterPage extends JPanel {

    private final UserDAO userDAO = new UserDAOImpl();
    private final NeoField fUsername = new NeoField("USERNAME", false, "username");
    private final NeoField fNama = new NeoField("NAMA LENGKAP", false, "nama_lengkap");
    private final NeoField fPassword = new NeoField("PASSWORD", true, "password");
    private final NeoField fKonfirmasi = new NeoField("KONFIRMASI", true, "konfirmasi_password");
    private final NeoButton btnRegister = new NeoButton("BUAT AKUN",
            FontIcon.of(MaterialDesignA.ACCOUNT_PLUS, 18, NeoBrutalTheme.INK),
            NeoBrutalTheme.BRAND_TEAL);
    private final NeoButton btnBack = new NeoButton("KEMBALI",
            FontIcon.of(MaterialDesignA.ARROW_LEFT, 18, NeoBrutalTheme.INK),
            NeoBrutalTheme.WARNING);
    private final JLabel lblError = new JLabel(" ");
    private final AuthScaffold scaffold;

    public RegisterPage(Runnable onBack, Runnable onRegistered) {
        super(new BorderLayout());
        setOpaque(false);

        btnRegister.setName("btnRegister");
        btnBack.setName("btnBack");
        lblError.setName("registerError");
        lblError.setForeground(NeoBrutalTheme.DANGER);
        lblError.setFont(new Font("Segoe UI", Font.BOLD, 12));

        JPanel form = buildForm();
        scaffold = new AuthScaffold(NeoBrutalTheme.BRAND_TEAL, Color.BLACK,
                "AKUN KASIR BARU", new String[]{"CV. Almira Jaya", "Abadi"}, form);
        add(scaffold, BorderLayout.CENTER);

        btnBack.addActionListener(e -> onBack.run());
        btnRegister.addActionListener(e -> doDaftar(onRegistered));
    }

    private JPanel buildForm() {
        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        JLabel kicker = new JLabel("PENDAFTARAN");
        kicker.setFont(NeoBrutalTheme.mono(11));
        kicker.setForeground(NeoBrutalTheme.BRAND_MAROON);
        kicker.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(kicker);
        form.add(Box.createVerticalStrut(6));

        JLabel title = new JLabel("DAFTAR");
        title.setFont(NeoBrutalTheme.display(52));
        title.setForeground(NeoBrutalTheme.INK);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(title);
        form.add(Box.createVerticalStrut(8));

        JLabel subtitle = new JLabel("Buat akun kasir baru untuk mulai bekerja.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(new Color(70, 70, 70));
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(subtitle);
        form.add(Box.createVerticalStrut(22));

        fUsername.setAlignmentX(Component.LEFT_ALIGNMENT);
        fUsername.setMaximumSize(new Dimension(420, 72));
        form.add(fUsername);
        form.add(Box.createVerticalStrut(10));

        fNama.setAlignmentX(Component.LEFT_ALIGNMENT);
        fNama.setMaximumSize(new Dimension(420, 72));
        form.add(fNama);
        form.add(Box.createVerticalStrut(10));

        fPassword.setAlignmentX(Component.LEFT_ALIGNMENT);
        fPassword.setMaximumSize(new Dimension(420, 72));
        form.add(fPassword);
        form.add(Box.createVerticalStrut(10));

        fKonfirmasi.setAlignmentX(Component.LEFT_ALIGNMENT);
        fKonfirmasi.setMaximumSize(new Dimension(420, 72));
        form.add(fKonfirmasi);
        form.add(Box.createVerticalStrut(4));

        lblError.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(lblError);
        form.add(Box.createVerticalStrut(14));

        btnRegister.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnRegister.setMaximumSize(new Dimension(420, 50));
        form.add(btnRegister);
        form.add(Box.createVerticalStrut(8));

        btnBack.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnBack.setMaximumSize(new Dimension(420, 50));
        form.add(btnBack);
        form.add(Box.createVerticalStrut(14));

        JLabel hint = new JLabel("Akun baru otomatis menjadi Kasir.");
        hint.setFont(NeoBrutalTheme.mono(10));
        hint.setForeground(new Color(90, 90, 90));
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(hint);

        return form;
    }

    private void doDaftar(Runnable onRegistered) {
        String username = fUsername.text();
        String nama = fNama.text();
        String pass = new String(fPassword.password());
        String konf = new String(fKonfirmasi.password());

        if (username.isEmpty() || nama.isEmpty()) {
            showError("Username dan nama lengkap wajib diisi", fUsername, fNama);
            return;
        }
        if (pass.isEmpty() || konf.isEmpty()) {
            showError("Password dan konfirmasi wajib diisi", fPassword, fKonfirmasi);
            return;
        }
        if (pass.length() < 4) {
            showError("Password minimal 4 karakter", fPassword);
            return;
        }
        if (!pass.equals(konf)) {
            showError("Password dan konfirmasi tidak sama", fPassword, fKonfirmasi);
            return;
        }
        try {
            if (userDAO.getByUsername(username) != null) {
                showError("Username sudah dipakai", fUsername);
                return;
            }
            User user = new User();
            user.setUsername(username);
            user.setNamaLengkap(nama);
            user.setPassword(HashUtil.sha256(pass));
            user.setRole("Kasir");
            userDAO.insert(user);
        } catch (RuntimeException ex) {
            showError("Gagal daftar: " + ex.getMessage(), fUsername);
            return;
        }
        onRegistered.run();
    }

    private void showError(String message, NeoField... fields) {
        lblError.setText(message == null ? " " : message);
        for (int i = 0; i < fields.length; i++) {
            final NeoField field = fields[i];
            if (i == 0) {
                field.shake();
            } else {
                util.Motion.delay(60 * i, field::shake);
            }
        }
    }

    public String username() {
        return fUsername.text();
    }

    public String nama() {
        return fNama.text();
    }

    public String password() {
        return new String(fPassword.password());
    }

    public String konfirmasi() {
        return new String(fKonfirmasi.password());
    }

    public javax.swing.JButton registerButton() {
        return btnRegister;
    }

    public void playIntro() {
        scaffold.playIntro();
        SwingUtilities.invokeLater(() -> fUsername.field().requestFocusInWindow());
    }
}
