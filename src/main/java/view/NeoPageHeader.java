package view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import util.NeoBrutalTheme;

/**
 * Header halaman internal: kicker mono, judul display, subtitle, dan area aksi.
 */
public class NeoPageHeader extends JPanel {

    public NeoPageHeader(String kicker, String title, String subtitle, JComponent actions) {
        super(new BorderLayout(16, 0));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(0, 0, 14, 0));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));

        JLabel lblKicker = new JLabel(safe(kicker).toUpperCase(Locale.ROOT));
        lblKicker.setName("pageKicker");
        lblKicker.setFont(NeoBrutalTheme.mono(10));
        lblKicker.setForeground(NeoBrutalTheme.BRAND_TEAL);
        lblKicker.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblTitle = new JLabel(safe(title));
        lblTitle.setName("pageTitle");
        lblTitle.setFont(NeoBrutalTheme.display(28));
        lblTitle.setForeground(NeoBrutalTheme.INK);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblSubtitle = new JLabel(safe(subtitle));
        lblSubtitle.setName("pageSubtitle");
        lblSubtitle.setFont(NeoBrutalTheme.body(java.awt.Font.PLAIN, 12));
        lblSubtitle.setForeground(NeoBrutalTheme.TEXT_MUTED);
        lblSubtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        text.add(lblKicker);
        text.add(Box.createVerticalStrut(2));
        text.add(lblTitle);
        text.add(Box.createVerticalStrut(3));
        text.add(lblSubtitle);

        add(text, BorderLayout.CENTER);
        if (actions != null) {
            actions.setOpaque(false);
            add(actions, BorderLayout.EAST);
        }
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
