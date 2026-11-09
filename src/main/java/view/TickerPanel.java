package view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JComponent;
import javax.swing.Timer;
import util.NeoBrutalTheme;

/**
 * Marquee horizontal untuk aksen teks berjalan.
 */
public class TickerPanel extends JComponent {

    private final String text;
    private final Color foreground;
    private double offset;
    private Timer timer;

    public TickerPanel(String text, Color background, Color foreground) {
        this.text = text == null ? "" : text;
        this.foreground = foreground == null ? NeoBrutalTheme.INK : foreground;
        setOpaque(true);
        setBackground(background == null ? NeoBrutalTheme.BRAND_TEAL : background);
        setFont(NeoBrutalTheme.mono(12));
        setPreferredSize(new Dimension(10, 34));
        setMinimumSize(new Dimension(10, 34));
    }

    public void start() {
        if (timer == null) {
            timer = new Timer(30, e -> {
                offset += 1.2;
                repaint();
            });
        }
        if (!timer.isRunning()) {
            timer.start();
        }
    }

    public void stop() {
        if (timer != null) {
            timer.stop();
        }
    }

    public int getOffset() {
        return (int) offset;
    }

    @Override
    public void addNotify() {
        super.addNotify();
        start();
    }

    @Override
    public void removeNotify() {
        stop();
        super.removeNotify();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        int height = getHeight();
        g2.setColor(NeoBrutalTheme.INK);
        g2.fillRect(0, 0, getWidth(), 2);
        g2.fillRect(0, height - 2, getWidth(), 2);

        FontMetrics fm = g2.getFontMetrics(getFont());
        int unit = Math.max(1, fm.stringWidth(text) + 60);
        double shift = offset % unit;
        g2.setColor(foreground);
        g2.clipRect(0, 2, getWidth(), Math.max(0, height - 4));
        int baseline = (height - fm.getHeight()) / 2 + fm.getAscent();
        for (double x = -shift; x < getWidth(); x += unit) {
            g2.drawString(text, (int) Math.round(x), baseline);
        }
        g2.dispose();
    }
}
