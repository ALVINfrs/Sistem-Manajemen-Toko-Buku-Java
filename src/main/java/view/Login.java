package view;

import dao.UserDAO;
import dao.UserDAOImpl;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import model.User;
import org.kordamp.ikonli.materialdesign2.MaterialDesignA;
import org.kordamp.ikonli.materialdesign2.MaterialDesignB;
import org.kordamp.ikonli.materialdesign2.MaterialDesignL;
import org.kordamp.ikonli.swing.FontIcon;
import util.AppConfig;
import util.HashUtil;
import util.NeoBrutalTheme;
import util.NeoShadowBorder;
import util.Sesi;

public class Login extends JFrame {

    private final UserDAO userDAO = new UserDAOImpl();
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;

    public Login() {
        super(AppConfig.APP_NAME + " - Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(800, 540);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(NeoBrutalTheme.BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(16, 16, 16, 16);

        // Panel ilustrasi kiri
        JPanel pnlIlustrasi = buildIlustrasi();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        root.add(pnlIlustrasi, gbc);

        // Card login kanan
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(NeoBrutalTheme.SURFACE);
        card.setBorder(new NeoShadowBorder());
        card.setPreferredSize(new Dimension(320, 440));

        JLabel lblJudul = new JLabel(AppConfig.APP_NAME, SwingConstants.CENTER);
        lblJudul.setFont(new Font("Segoe UI Black", Font.BOLD, 20));
        lblJudul.setForeground(Color.BLACK);

        JLabel lblLogo = new JLabel(FontIcon.of(MaterialDesignB.BOOK_OPEN_PAGE_VARIANT, 48, Color.BLACK));
        lblLogo.setHorizontalAlignment(SwingConstants.CENTER);
        JLabel lblSub = new JLabel("Sistem Informasi Penjualan Buku", SwingConstants.CENTER);
        lblSub.setFont(new Font("Segoe UI Semibold", Font.BOLD, 12));
        lblSub.setForeground(Color.BLACK);

        JPanel pnlHeader = new JPanel(new BorderLayout(0, 2));
        pnlHeader.setBackground(NeoBrutalTheme.SURFACE);
        pnlHeader.add(lblLogo, BorderLayout.NORTH);
        pnlHeader.add(lblJudul, BorderLayout.CENTER);
        pnlHeader.add(lblSub, BorderLayout.SOUTH);
        card.add(pnlHeader, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(NeoBrutalTheme.SURFACE);
        GridBagConstraints fgc = new GridBagConstraints();
        fgc.gridx = 0;
        fgc.fill = GridBagConstraints.HORIZONTAL;
        fgc.weightx = 1.0;
        fgc.insets = new Insets(6, 12, 6, 12);

        JLabel lblUser = new JLabel("Username");
        lblUser.setFont(new Font("Segoe UI Semibold", Font.BOLD, 13));
        fgc.gridy = 0;
        form.add(lblUser, fgc);

        txtUsername = new JTextField(20);
        txtUsername.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        fgc.gridy = 1;
        form.add(txtUsername, fgc);

        JLabel lblPass = new JLabel("Password");
        lblPass.setFont(new Font("Segoe UI Semibold", Font.BOLD, 13));
        fgc.gridy = 2;
        form.add(lblPass, fgc);

        txtPassword = new JPasswordField(20);
        txtPassword.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        fgc.gridy = 3;
        form.add(txtPassword, fgc);

        btnLogin = new JButton("Login", FontIcon.of(MaterialDesignL.LOGIN, 20, Color.BLACK));
        btnLogin.setIconTextGap(8);
        btnLogin.setBackground(NeoBrutalTheme.PRIMARY);
        btnLogin.setForeground(Color.BLACK);
        btnLogin.setFont(new Font("Segoe UI Semibold", Font.BOLD, 14));
        btnLogin.setBorder(new NeoShadowBorder());
        btnLogin.setFocusPainted(false);
        addPressEffect(btnLogin);
        fgc.gridy = 4;
        fgc.insets = new Insets(14, 12, 6, 12);
        form.add(btnLogin, fgc);

        JButton btnDaftar = new JButton("Daftar Akun Kasir",
                FontIcon.of(MaterialDesignA.ACCOUNT_PLUS, 18, Color.BLACK));
        btnDaftar.setIconTextGap(8);
        btnDaftar.setBackground(NeoBrutalTheme.SECONDARY);
        btnDaftar.setForeground(Color.BLACK);
        btnDaftar.setFont(new Font("Segoe UI Semibold", Font.BOLD, 13));
        btnDaftar.setBorder(new NeoShadowBorder());
        btnDaftar.setFocusPainted(false);
        addPressEffect(btnDaftar);
        btnDaftar.addActionListener(e -> new RegisterDialog(this).setVisible(true));
        fgc.gridy = 5;
        fgc.insets = new Insets(6, 12, 6, 12);
        form.add(btnDaftar, fgc);

        card.add(form, BorderLayout.CENTER);

        JLabel lblHint = new JLabel("Gunakan akun Admin/Kasir untuk masuk", SwingConstants.CENTER);
        lblHint.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblHint.setForeground(Color.BLACK);
        card.add(lblHint, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(btnLogin);

        btnLogin.addActionListener(e -> doLogin());

        gbc.gridx = 1;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.VERTICAL;
        JPanel wrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        wrap.setBackground(NeoBrutalTheme.BG);
        wrap.add(card);
        root.add(wrap, gbc);

        setContentPane(root);
    }

    private JPanel buildIlustrasi() {
        java.net.URL imgUrl = getClass().getResource("/images/login_bg.jpg");
        if (imgUrl != null) {
            javax.swing.ImageIcon raw = new javax.swing.ImageIcon(imgUrl);
            int maxW = 380;
            int maxH = 360;
            int w = raw.getIconWidth();
            int h = raw.getIconHeight();
            if (w > 0 && h > 0 && (w > maxW || h > maxH)) {
                double s = Math.min((double) maxW / w, (double) maxH / h);
                w = (int) Math.round(w * s);
                h = (int) Math.round(h * s);
            }
            java.awt.Image scaled = raw.getImage().getScaledInstance(w, h, java.awt.Image.SCALE_SMOOTH);
            JLabel lbl = new JLabel(new javax.swing.ImageIcon(scaled));
            lbl.setBorder(BorderFactory.createLineBorder(Color.BLACK, 4));
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            JPanel p = new JPanel(new BorderLayout());
            p.setBackground(NeoBrutalTheme.BG);
            p.add(lbl, BorderLayout.CENTER);
            return p;
        }
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(NeoBrutalTheme.SECONDARY);
        p.setBorder(new NeoShadowBorder());
        JLabel ikon = new JLabel(FontIcon.of(MaterialDesignB.BOOK_OPEN_PAGE_VARIANT, 120, Color.BLACK));
        ikon.setHorizontalAlignment(SwingConstants.CENTER);
        p.add(ikon, BorderLayout.CENTER);
        return p;
    }

    private void addPressEffect(JButton btn) {
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                btn.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                btn.setBorder(new NeoShadowBorder());
            }
        });
    }

    private void doLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());
        User u = userDAO.getByUsername(username);
        if (u == null || !HashUtil.sha256(password).equals(u.getPassword())) {
            JOptionPane.showMessageDialog(this, "Username atau password salah",
                    "Login Gagal", JOptionPane.ERROR_MESSAGE);
            return;
        }
        Sesi.userLogin = u;
        Sesi.role = u.getRole();
        new MenuUtama().setVisible(true);
        dispose();
    }

    public static void main(String[] args) {
        NeoBrutalTheme.apply();
        SwingUtilities.invokeLater(() -> new Login().setVisible(true));
    }
}
