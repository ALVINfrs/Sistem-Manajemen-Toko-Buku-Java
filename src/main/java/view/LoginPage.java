package view;

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
import org.kordamp.ikonli.materialdesign2.MaterialDesignA;
import org.kordamp.ikonli.materialdesign2.MaterialDesignL;
import org.kordamp.ikonli.swing.FontIcon;
import util.Motion;
import util.NeoBrutalTheme;

/**
 * Halaman form login (kanan) dengan brand panel kiri.
 */
public class LoginPage extends JPanel {

    private final NeoField fUsername = new NeoField("USERNAME", false, "username");
    private final NeoField fPassword = new NeoField("PASSWORD", true, "password");
    private final NeoButton btnLogin = new NeoButton("MASUK",
            FontIcon.of(MaterialDesignL.LOGIN, 18, NeoBrutalTheme.INK), NeoBrutalTheme.PRIMARY);
    private final NeoButton btnDaftar = new NeoButton("DAFTAR AKUN",
            FontIcon.of(MaterialDesignA.ACCOUNT_PLUS, 18, NeoBrutalTheme.INK), NeoBrutalTheme.WARNING);
    private final JLabel lblError = new JLabel(" ");
    private final JLabel lblInfo = new JLabel(" ");
    private final AuthScaffold scaffold;

    public LoginPage(Runnable onLogin, Runnable onDaftar) {
        super(new BorderLayout());
        setOpaque(false);

        btnLogin.setName("btnLogin");
        btnDaftar.setName("btnDaftar");
        lblError.setName("loginError");
        lblError.setForeground(NeoBrutalTheme.DANGER);
        lblError.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblInfo.setName("loginInfo");
        lblInfo.setForeground(NeoBrutalTheme.SUCCESS);
        lblInfo.setFont(new Font("Segoe UI", Font.BOLD, 12));

        JPanel form = buildForm();
        scaffold = new AuthScaffold(NeoBrutalTheme.BRAND_MAROON, Color.WHITE,
                "TOKO BUKU & ALAT TULIS", new String[]{"ALMIRA JAYA", "ABADI"}, form);
        add(scaffold, BorderLayout.CENTER);

        btnLogin.addActionListener(e -> onLogin.run());
        btnDaftar.addActionListener(e -> onDaftar.run());
    }

    private JPanel buildForm() {
        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        JLabel kicker = new JLabel("SISTEM INFORMASI");
        kicker.setFont(NeoBrutalTheme.mono(11));
        kicker.setForeground(NeoBrutalTheme.BRAND_TEAL);
        kicker.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(kicker);
        form.add(Box.createVerticalStrut(6));

        JLabel title = new JLabel("MASUK");
        title.setFont(NeoBrutalTheme.display(56));
        title.setForeground(NeoBrutalTheme.INK);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(title);
        form.add(Box.createVerticalStrut(8));

        JLabel subtitle = new JLabel("Selamat datang kembali. Masuk untuk mengelola toko.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(new Color(70, 70, 70));
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(subtitle);
        form.add(Box.createVerticalStrut(26));

        fUsername.setAlignmentX(Component.LEFT_ALIGNMENT);
        fUsername.setMaximumSize(new Dimension(420, 76));
        form.add(fUsername);
        form.add(Box.createVerticalStrut(14));

        fPassword.setAlignmentX(Component.LEFT_ALIGNMENT);
        fPassword.setMaximumSize(new Dimension(420, 76));
        form.add(fPassword);
        form.add(Box.createVerticalStrut(6));

        lblError.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(lblError);
        lblInfo.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(lblInfo);
        form.add(Box.createVerticalStrut(16));

        btnLogin.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnLogin.setMaximumSize(new Dimension(420, 50));
        form.add(btnLogin);
        form.add(Box.createVerticalStrut(10));

        btnDaftar.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnDaftar.setMaximumSize(new Dimension(420, 50));
        form.add(btnDaftar);
        form.add(Box.createVerticalStrut(16));

        JLabel hint = new JLabel("Masuk sebagai Admin atau Kasir.");
        hint.setFont(NeoBrutalTheme.mono(10));
        hint.setForeground(new Color(90, 90, 90));
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(hint);

        return form;
    }

    public String username() {
        return fUsername.text();
    }

    public String password() {
        return new String(fPassword.password());
    }

    public javax.swing.JButton loginButton() {
        return btnLogin;
    }

    public void showError(String message) {
        lblError.setText(message == null ? " " : message);
        lblInfo.setText(" ");
        fUsername.shake();
        Motion.delay(60, fPassword::shake);
    }

    public void showInfo(String message) {
        lblInfo.setText(message == null ? " " : message);
        lblError.setText(" ");
    }

    public void clearInfo() {
        lblInfo.setText(" ");
    }

    public void playIntro() {
        scaffold.playIntro();
        SwingUtilities.invokeLater(() -> fUsername.field().requestFocusInWindow());
    }
}
