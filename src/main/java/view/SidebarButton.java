package view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import org.kordamp.ikonli.swing.FontIcon;
import util.NeoBrutalTheme;

/**
 * Tombol sidebar dengan state aktif teal + hard shadow dan hover maroon gelap.
 */
public class SidebarButton extends JButton {

    private static final int SHADOW = 4;

    private final FontIcon fontIcon;
    private boolean active;
    private boolean hover;

    public SidebarButton(String text, FontIcon icon) {
        super(text, icon);
        this.fontIcon = icon;
        setFont(NeoBrutalTheme.display(12));
        setForeground(NeoBrutalTheme.BG);
        setHorizontalAlignment(LEFT);
        setIconTextGap(10);
        setFocusPainted(false);
        setFocusable(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setOpaque(false);
        setRolloverEnabled(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
        setPreferredSize(new Dimension(216, 34));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        setMinimumSize(new Dimension(120, 34));
        setAlignmentX(LEFT_ALIGNMENT);
        if (fontIcon != null) {
            fontIcon.setIconColor(NeoBrutalTheme.BG);
        }
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hover = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hover = false;
                repaint();
            }
        });
    }

    public void setActive(boolean active) {
        this.active = active;
        setForeground(active ? NeoBrutalTheme.INK : NeoBrutalTheme.BG);
        if (fontIcon != null) {
            fontIcon.setIconColor(active ? NeoBrutalTheme.INK : NeoBrutalTheme.BG);
        }
        repaint();
    }

    public boolean isActive() {
        return active;
    }

    public Color getActiveFill() {
        return NeoBrutalTheme.BRAND_TEAL;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        if (active) {
            int width = Math.max(0, getWidth() - SHADOW);
            int height = Math.max(0, getHeight() - SHADOW);
            g2.setColor(NeoBrutalTheme.INK);
            g2.fillRect(SHADOW, SHADOW, width, height);
            g2.setColor(NeoBrutalTheme.BRAND_TEAL);
            g2.fillRect(0, 0, width, height);
            g2.setColor(NeoBrutalTheme.INK);
            g2.setStroke(new BasicStroke(2f));
            g2.drawRect(1, 1, Math.max(0, width - 2), Math.max(0, height - 2));
        } else {
            g2.setColor(hover
                    ? NeoBrutalTheme.BRAND_MAROON_DARK : NeoBrutalTheme.BRAND_MAROON);
            g2.fillRect(0, 0, getWidth(), getHeight());
        }

        g2.dispose();
        super.paintComponent(g);
    }
}
