package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import util.AppConfig;
import util.LogoUtil;
import util.Motion;
import util.NeoBrutalTheme;
import util.NeoShadowBorder;

/**
 * Kerangka halaman auth: panel brand kiri, form kanan, ticker bawah.
 */
public class AuthScaffold extends JPanel {

    private final MotionPanel leftPanel;
    private final MotionPanel rightPanel;

    public AuthScaffold(Color leftBg, Color leftFg, String sticker,
            String[] titleLines, JComponent formContent) {
        super(new BorderLayout(0, 0));
        setBackground(NeoBrutalTheme.BG);

        leftPanel = new MotionPanel(new GridBagLayout());
        leftPanel.setOpaque(true);
        leftPanel.setBackground(leftBg);
        leftPanel.setPreferredSize(new Dimension(440, 0));

        JPanel brand = new JPanel();
        brand.setOpaque(false);
        brand.setLayout(new BoxLayout(brand, BoxLayout.Y_AXIS));

        Color accent = NeoBrutalTheme.BRAND_TEAL.equals(leftBg)
                ? NeoBrutalTheme.WARNING : NeoBrutalTheme.BRAND_TEAL;
        StickerLabel stickerLabel = new StickerLabel(sticker, accent, leftFg, -3);
        stickerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        brand.add(stickerLabel);
        brand.add(Box.createVerticalStrut(18));

        JPanel logoCard = new JPanel(new BorderLayout());
        logoCard.setBackground(NeoBrutalTheme.SURFACE);
        logoCard.setPreferredSize(new Dimension(260, 260));
        logoCard.setMinimumSize(new Dimension(260, 260));
        logoCard.setMaximumSize(new Dimension(260, 260));
        logoCard.setBorder(BorderFactory.createCompoundBorder(
                new NeoShadowBorder(8, NeoBrutalTheme.INK, NeoBrutalTheme.INK),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)));
        JLabel logo = new JLabel(LogoUtil.icon(170), SwingConstants.CENTER);
        logo.setHorizontalAlignment(SwingConstants.CENTER);
        logoCard.add(logo, BorderLayout.CENTER);
        logoCard.setAlignmentX(Component.CENTER_ALIGNMENT);
        brand.add(logoCard);
        brand.add(Box.createVerticalStrut(22));

        String[] lines = titleLines == null ? new String[0] : titleLines;
        for (String line : lines) {
            JLabel title = new JLabel(line);
            title.setFont(NeoBrutalTheme.display(30));
            title.setForeground(leftFg);
            title.setAlignmentX(Component.CENTER_ALIGNMENT);
            brand.add(title);
        }
        brand.add(Box.createVerticalStrut(12));

        JLabel address = new JLabel(ringkas(AppConfig.ALAMAT_TOKO, 46));
        address.setFont(NeoBrutalTheme.mono(10));
        address.setForeground(leftFg);
        address.setToolTipText(AppConfig.ALAMAT_TOKO);
        address.setAlignmentX(Component.CENTER_ALIGNMENT);
        brand.add(address);

        GridBagConstraints leftGbc = new GridBagConstraints();
        leftGbc.gridx = 0;
        leftGbc.gridy = 0;
        leftGbc.anchor = GridBagConstraints.CENTER;
        leftPanel.add(brand, leftGbc);
        add(leftPanel, BorderLayout.WEST);

        rightPanel = new MotionPanel(new GridBagLayout());
        rightPanel.setOpaque(true);
        rightPanel.setBackground(NeoBrutalTheme.BG);

        JPanel formWrap = new JPanel(new BorderLayout()) {
            @Override
            public Dimension getPreferredSize() {
                Dimension size = super.getPreferredSize();
                return new Dimension(420, size.height);
            }
        };
        formWrap.setOpaque(false);
        formWrap.add(formContent, BorderLayout.CENTER);

        GridBagConstraints formGbc = new GridBagConstraints();
        formGbc.gridx = 0;
        formGbc.gridy = 0;
        formGbc.anchor = GridBagConstraints.CENTER;
        formGbc.insets = new Insets(0, 24, 0, 24);
        rightPanel.add(formWrap, formGbc);
        add(rightPanel, BorderLayout.CENTER);

        TickerPanel ticker = new TickerPanel(
                "  TOKO BUKU  •  ALAT TULIS  •  SISTEM INFORMASI MANAJEMEN  •  CV. ALMIRA JAYA ABADI  •  ",
                NeoBrutalTheme.BRAND_TEAL, NeoBrutalTheme.INK);
        add(ticker, BorderLayout.SOUTH);
    }

    public void playIntro() {
        leftPanel.setAlpha(0f);
        leftPanel.setOffset(-40, 0);
        rightPanel.setAlpha(0f);
        rightPanel.setOffset(0, 24);

        Motion.animate(520, Motion.EASE_OUT_CUBIC, p -> {
            leftPanel.setAlpha((float) p);
            leftPanel.setOffset((int) Math.round(-40 * (1 - p)), 0);
        }, null);
        Motion.delay(80, () -> Motion.animate(520, Motion.EASE_OUT_CUBIC, p -> {
            rightPanel.setAlpha((float) p);
            rightPanel.setOffset(0, (int) Math.round(24 * (1 - p)));
        }, null));
    }

    private static String ringkas(String text, int max) {
        if (text == null) {
            return "";
        }
        return text.length() <= max ? text : text.substring(0, max - 3) + "...";
    }
}
