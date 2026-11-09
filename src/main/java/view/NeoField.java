package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.AbstractBorder;
import org.kordamp.ikonli.materialdesign2.MaterialDesignE;
import org.kordamp.ikonli.swing.FontIcon;
import util.Motion;
import util.NeoBrutalTheme;

/**
 * Field auth dengan label mono, border fokus animasi, dan eye toggle.
 */
public class NeoField extends JPanel {

    private static final char BULLET = '\u2022';

    private final JTextField field;
    private final JPanel fieldWrap;
    private final FocusBorder focusBorder;
    private final JLabel label;

    public NeoField(String labelText, boolean password, String name) {
        super(new BorderLayout(0, 6));
        setOpaque(false);

        label = new JLabel(labelText == null ? "" : labelText.toUpperCase());
        label.setFont(NeoBrutalTheme.mono(11));
        label.setForeground(NeoBrutalTheme.INK);
        label.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0));
        add(label, BorderLayout.NORTH);

        field = password ? new JPasswordField() : new JTextField();
        field.setName(name);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setForeground(NeoBrutalTheme.INK);
        field.setBackground(NeoBrutalTheme.SURFACE);
        field.setCaretColor(NeoBrutalTheme.INK);
        field.setSelectionColor(NeoBrutalTheme.WARNING);
        field.setSelectedTextColor(NeoBrutalTheme.INK);
        field.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        fieldWrap = new JPanel(new BorderLayout());
        fieldWrap.setOpaque(true);
        fieldWrap.setBackground(NeoBrutalTheme.SURFACE);
        focusBorder = new FocusBorder();
        fieldWrap.setBorder(focusBorder);
        fieldWrap.add(field, BorderLayout.CENTER);

        if (password) {
            JPasswordField passwordField = (JPasswordField) field;
            passwordField.setEchoChar(BULLET);

            JButton eye = new JButton();
            eye.setName(name + "Eye");
            eye.setPreferredSize(new Dimension(32, 32));
            eye.setBorder(BorderFactory.createEmptyBorder());
            eye.setContentAreaFilled(false);
            eye.setFocusPainted(false);
            eye.setOpaque(false);
            eye.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            eye.setIcon(FontIcon.of(MaterialDesignE.EYE, 16, NeoBrutalTheme.INK));
            eye.addActionListener(e -> {
                if (passwordField.getEchoChar() == (char) 0) {
                    passwordField.setEchoChar(BULLET);
                    eye.setIcon(FontIcon.of(MaterialDesignE.EYE, 16, NeoBrutalTheme.INK));
                } else {
                    passwordField.setEchoChar((char) 0);
                    eye.setIcon(FontIcon.of(MaterialDesignE.EYE_OFF, 16, NeoBrutalTheme.INK));
                }
            });
            fieldWrap.add(eye, BorderLayout.EAST);
        }

        add(fieldWrap, BorderLayout.CENTER);

        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                animateFocus(1f);
            }

            @Override
            public void focusLost(FocusEvent e) {
                animateFocus(0f);
            }
        });
    }

    private void animateFocus(float target) {
        final float start = focusBorder.getFocus();
        Motion.animate(180, Motion.EASE_OUT_CUBIC, p -> {
            focusBorder.setFocus((float) (start + (target - start) * p));
            fieldWrap.repaint();
        }, null);
    }

    public JTextField field() {
        return field;
    }

    public String text() {
        return field.getText().trim();
    }

    public char[] password() {
        if (field instanceof JPasswordField) {
            return ((JPasswordField) field).getPassword();
        }
        return field.getText().toCharArray();
    }

    public void shake() {
        Motion.shake(fieldWrap, 8, 4, null);
    }

    public void setFieldEnabled(boolean enabled) {
        field.setEnabled(enabled);
        label.setForeground(enabled ? NeoBrutalTheme.INK : Color.GRAY);
    }

    private static class FocusBorder extends AbstractBorder {

        private float focus;

        float getFocus() {
            return focus;
        }

        void setFocus(float focus) {
            this.focus = Math.max(0f, Math.min(1f, focus));
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(10, 12, 10, 12);
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.set(10, 12, 10, 12);
            return insets;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            int thickness = 2 + Math.round(focus * 2);
            g2.setColor(NeoBrutalTheme.INK);
            g2.fillRect(x, y, w, thickness);
            g2.fillRect(x, y + h - thickness, w, thickness);
            g2.fillRect(x, y, thickness, h);
            g2.fillRect(x + w - thickness, y, thickness, h);
            g2.dispose();
        }
    }
}
