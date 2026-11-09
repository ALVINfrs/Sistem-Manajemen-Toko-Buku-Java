package view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.Timer;
import util.Motion;
import util.NeoBrutalTheme;

/**
 * Tombol neobrutalism dengan hard shadow dan efek tekan.
 */
public class NeoButton extends JButton {

    private static final int SHADOW = 5;

    private Color fill;
    private double press;
    private Timer pressTimer;

    public NeoButton(String text, Icon icon, Color fill) {
        super(text);
        this.fill = fill == null ? NeoBrutalTheme.PRIMARY : fill;
        if (icon != null) {
            setIcon(icon);
            setIconTextGap(8);
        }
        setFont(new Font("Segoe UI Black", Font.BOLD, 13));
        setForeground(NeoBrutalTheme.INK);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setContentAreaFilled(false);
        setFocusPainted(false);
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (isEnabled()) {
                    animatePress(1.0);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                animatePress(0.0);
            }
        });
    }

    public void setFillColor(Color color) {
        this.fill = color == null ? NeoBrutalTheme.PRIMARY : color;
        repaint();
    }

    public Color getFillColor() {
        return fill;
    }

    private void animatePress(double target) {
        if (pressTimer != null) {
            pressTimer.stop();
        }
        final double start = press;
        pressTimer = Motion.animate(110, Motion.EASE_OUT_CUBIC, p -> {
            press = start + (target - start) * p;
            repaint();
        }, null);
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension size = super.getPreferredSize();
        size.width += SHADOW;
        size.height += SHADOW;
        return size;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int dx = (int) Math.round(press * 3);
        int width = Math.max(0, getWidth() - SHADOW - dx);
        int height = Math.max(0, getHeight() - SHADOW - dx);

        g2.setColor(NeoBrutalTheme.INK);
        g2.fillRect(dx + SHADOW, dx + SHADOW, width, height);

        g2.setColor(fill);
        g2.fillRect(dx, dx, width, height);

        g2.setColor(NeoBrutalTheme.INK);
        g2.setStroke(new BasicStroke(3f));
        g2.drawRect(dx + 1, dx + 1, Math.max(0, width - 2), Math.max(0, height - 2));

        g2.translate(dx, dx);
        super.paintComponent(g2);
        g2.dispose();
    }
}
