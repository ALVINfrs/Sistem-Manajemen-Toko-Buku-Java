package view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JComponent;
import util.NeoBrutalTheme;

/**
 * Label kotak miring untuk aksen neobrutalism.
 */
public class StickerLabel extends JComponent {

    private final String text;
    private final Color background;
    private final Color foreground;
    private final double degrees;

    public StickerLabel(String text, Color background, Color foreground, double degrees) {
        this.text = text == null ? "" : text.toUpperCase();
        this.background = background == null ? NeoBrutalTheme.WARNING : background;
        this.foreground = foreground == null ? NeoBrutalTheme.INK : foreground;
        this.degrees = degrees;
        setFont(NeoBrutalTheme.mono(11));
        setOpaque(false);
    }

    @Override
    public Dimension getPreferredSize() {
        FontMetrics fm = getFontMetrics(getFont());
        return new Dimension(fm.stringWidth(text) + 32, fm.getHeight() + 26);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int pad = 10;
        int width = getWidth() - pad * 2;
        int height = getHeight() - pad * 2;
        if (width <= 0 || height <= 0) {
            g2.dispose();
            return;
        }
        g2.rotate(Math.toRadians(degrees), getWidth() / 2.0, getHeight() / 2.0);
        g2.setColor(background);
        g2.fillRect(pad, pad, width, height);
        g2.setColor(NeoBrutalTheme.INK);
        g2.setStroke(new BasicStroke(2f));
        g2.drawRect(pad + 1, pad + 1, width - 2, height - 2);

        FontMetrics fm = g2.getFontMetrics(getFont());
        int textX = pad + (width - fm.stringWidth(text)) / 2;
        int textY = pad + (height - fm.getHeight()) / 2 + fm.getAscent();
        g2.setColor(foreground);
        g2.drawString(text, textX, textY);
        g2.dispose();
    }
}
