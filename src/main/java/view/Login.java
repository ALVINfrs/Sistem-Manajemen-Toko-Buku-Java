package view;

import dao.UserDAO;
import dao.UserDAOImpl;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import model.User;
import util.AppConfig;
import util.HashUtil;
import util.NeoBrutalTheme;
import util.Sesi;

/**
 * Frame login: halaman penuh split-screen, memakai AuthScaffold + LoginPage.
 */
public class Login extends JFrame {

    private final UserDAO userDAO = new UserDAOImpl();
    private final LoginPage loginPage;

    public Login() {
        super(AppConfig.APP_NAME + " - Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1080, 680);
        setMinimumSize(new Dimension(960, 600));
        setLocationRelativeTo(null);

        loginPage = new LoginPage(this::doLogin,
                () -> new RegisterDialog(this).setVisible(true));
        setContentPane(loginPage);
        getRootPane().setDefaultButton(loginPage.loginButton());
        SwingUtilities.invokeLater(loginPage::playIntro);
    }

    private void doLogin() {
        String username = loginPage.username();
        String password = loginPage.password();
        if (username.isEmpty() || password.isEmpty()) {
            loginPage.showError("Username dan password wajib diisi");
            return;
        }
        try {
            User user = userDAO.getByUsername(username);
            if (user == null || !HashUtil.sha256(password).equals(user.getPassword())) {
                loginPage.showError("Username atau password salah");
                return;
            }
            Sesi.userLogin = user;
            Sesi.role = user.getRole();
            new MenuUtama().setVisible(true);
            dispose();
        } catch (RuntimeException ex) {
            loginPage.showError("Gagal terhubung database: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        NeoBrutalTheme.apply();
        new SplashScreen(() -> new Login().setVisible(true)).start();
    }
}
