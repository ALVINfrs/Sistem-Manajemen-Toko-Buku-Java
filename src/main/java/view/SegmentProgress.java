package view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JComponent;
import util.NeoBrutalTheme;

/**
 * Progress chunky berbentuk blok-blok; blok baru memantul singkat.
 */
public class SegmentProgress extends JComponent {

    private static final Color[] DEFAULT_COLORS = {
        NeoBrutalTheme.BRAND_MAROON,
        NeoBrutalTheme.BRAND_TEAL,
        NeoBrutalTheme.WARNING,
        NeoBrutalTheme.PRIMARY,
        NeoBrutalTheme.SECONDARY
    };

    private final Color[] colors;
    private int value;
    private int popIndex = -1;
    private long popAt;

    public SegmentProgress(Color... colors) {
        this.colors = colors == null || colors.length == 0 ? DEFAULT_COLORS : colors;
        setPreferredSize(new Dimension(280, 18));
        setMinimumSize(new Dimension(120, 18));
        setOpaque(false);
    }

    public void setValue(int newValue) {
        int clamped = Math.max(0, Math.min(colors.length, newValue));
        if (clamped > value) {
            popIndex = clamped - 1;
            popAt = System.currentTimeMillis();
        }
        value = clamped;
        repaint();
    }

    public int getValue() {
        return value;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int count = colors.length;
        int gap = 6;
        int width = (getWidth() - gap * (count - 1)) / count;
        if (width <= 0) {
            g2.dispose();
            return;
        }
        long now = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            int x = i * (width + gap);
            double scale = 1.0;
            if (i == popIndex) {
                long elapsed = now - popAt;
                if (elapsed >= 0 && elapsed < 150) {
                    scale = 1.0 + 0.12 * (1.0 - elapsed / 150.0);
                }
            }
            int extra = (int) Math.round(width * (scale - 1.0) / 2.0);
            int bx = x - extra;
            int by = -extra;
            int bw = width + extra * 2;
            int bh = getHeight() + extra * 2;
            g2.setColor(i < value ? colors[i] : NeoBrutalTheme.SURFACE);
            g2.fillRect(bx, by, bw, bh);
            g2.setColor(NeoBrutalTheme.INK);
            g2.setStroke(new BasicStroke(2f));
            g2.drawRect(bx + 1, by + 1, Math.max(0, bw - 2), Math.max(0, bh - 2));
        }
        g2.dispose();
    }
}
