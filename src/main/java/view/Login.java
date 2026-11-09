package view;

import dao.UserDAO;
import dao.UserDAOImpl;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import model.User;
import util.AppConfig;
import util.HashUtil;
import util.LogoUtil;
import util.Motion;
import util.NeoBrutalTheme;
import util.Sesi;

/**
 * Frame login/register: satu window, dua halaman, transisi curtain.
 */
public class Login extends JFrame {

    private static final int CURTAIN_MS = 340;

    private final UserDAO userDAO = new UserDAOImpl();
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel pageHost = new JPanel(cardLayout);
    private final LoginPage loginPage;
    private final RegisterPage registerPage;
    private final JLayeredPane layered;
    private final JPanel curtain;
    private boolean transitioning;

    public Login() {
        super(AppConfig.APP_NAME + " - Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1080, 680);
        setMinimumSize(new Dimension(960, 600));
        setLocationRelativeTo(null);

        loginPage = new LoginPage(this::doLogin, this::openRegister);
        registerPage = new RegisterPage(() -> showPage("login"), this::afterRegister);

        pageHost.setOpaque(false);
        pageHost.add(loginPage, "login");
        pageHost.add(registerPage, "register");

        curtain = buildCurtain();
        layered = new AuthLayers(pageHost, curtain);
        setContentPane(layered);
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

    private void afterRegister() {
        showPage("login");
        loginPage.showInfo("Akun berhasil dibuat. Silakan masuk.");
    }

    private void openRegister() {
        loginPage.clearInfo();
        showPage("register");
    }

    private void showPage(String name) {
        if (transitioning) {
            return;
        }
        transitioning = true;

        int h = layered.getHeight() > 0 ? layered.getHeight() : getHeight();
        int w = layered.getWidth() > 0 ? layered.getWidth() : getWidth();
        curtain.setBounds(0, -h, w, h);
        curtain.setVisible(true);
        layered.repaint();

        Motion.animate(CURTAIN_MS, Motion.EASE_IN_OUT_CUBIC,
                p -> curtain.setLocation(0, (int) Math.round(-h + p * h)),
                () -> {
                    cardLayout.show(pageHost, name);
                    getRootPane().setDefaultButton("register".equals(name)
                            ? registerPage.registerButton()
                            : loginPage.loginButton());
                    if ("register".equals(name)) {
                        registerPage.playIntro();
                    } else {
                        loginPage.playIntro();
                    }
                    Motion.animate(CURTAIN_MS, Motion.EASE_OUT_CUBIC,
                            p -> curtain.setLocation(0, (int) Math.round(-p * h)),
                            () -> {
                                curtain.setVisible(false);
                                transitioning = false;
                            });
                });
    }

    private JPanel buildCurtain() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(true);
        panel.setBackground(NeoBrutalTheme.BRAND_MAROON);
        panel.setBorder(BorderFactory.createLineBorder(NeoBrutalTheme.INK, 3));

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        JLabel logo = new JLabel(LogoUtil.icon(72));
        logo.setAlignmentX(CENTER_ALIGNMENT);
        center.add(logo);
        center.add(Box.createVerticalStrut(10));

        JLabel wordmark = new JLabel("ALMIRA JAYA ABADI");
        wordmark.setFont(NeoBrutalTheme.display(20));
        wordmark.setForeground(NeoBrutalTheme.BG);
        wordmark.setAlignmentX(CENTER_ALIGNMENT);
        center.add(wordmark);

        JPanel centerWrap = new JPanel(new GridBagLayout());
        centerWrap.setOpaque(false);
        centerWrap.add(center);
        panel.add(centerWrap, BorderLayout.CENTER);

        JPanel strip = new JPanel(new BorderLayout());
        strip.setBackground(NeoBrutalTheme.BRAND_TEAL);
        strip.setPreferredSize(new Dimension(0, 14));
        JLabel stripText = new JLabel("  SISTEM TOKO BUKU  ");
        stripText.setFont(NeoBrutalTheme.mono(9));
        stripText.setForeground(NeoBrutalTheme.INK);
        stripText.setVerticalAlignment(SwingConstants.CENTER);
        strip.add(stripText, BorderLayout.WEST);
        panel.add(strip, BorderLayout.SOUTH);

        return panel;
    }

    private static final class AuthLayers extends JLayeredPane {

        private final JComponent base;
        private final JComponent curtain;

        AuthLayers(JComponent base, JComponent curtain) {
            this.base = base;
            this.curtain = curtain;
            setOpaque(true);
            setBackground(NeoBrutalTheme.BG);
            add(base, Integer.valueOf(DEFAULT_LAYER));
            add(curtain, Integer.valueOf(PALETTE_LAYER));
            curtain.setVisible(false);
        }

        @Override
        public void doLayout() {
            base.setBounds(0, 0, getWidth(), getHeight());
            curtain.setBounds(0, curtain.getY(), getWidth(), getHeight());
        }
    }

    public static void main(String[] args) {
        NeoBrutalTheme.apply();
        new SplashScreen(() -> new Login().setVisible(true)).start();
    }
}
