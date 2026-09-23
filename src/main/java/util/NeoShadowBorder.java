package util;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import javax.swing.border.AbstractBorder;

public class NeoShadowBorder extends AbstractBorder {
    private final int offset;
    private final Color shadowColor;
    private final Color lineColor;

    public NeoShadowBorder() {
        this(4, Color.BLACK, Color.BLACK);
    }

    public NeoShadowBorder(int offset, Color shadowColor, Color lineColor) {
        this.offset = offset;
        this.shadowColor = shadowColor;
        this.lineColor = lineColor;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
        Graphics2D g2 = (Graphics2D) g.create();
        if (w > offset + 2 && h > offset + 2) {
            // outline mengelilingi konten (tepi kanan/bawah berhenti sebelum zona shadow)
            g2.setColor(lineColor);
            g2.setStroke(new BasicStroke(2));
            g2.drawRect(x, y, w - offset - 1, h - offset - 1);
            // shadow solid HANYA di strip kanan + bawah (zona inset), menyentuh outline
            g2.setColor(shadowColor);
            g2.fillRect(x + w - offset - 1, y + offset, offset, h - offset);
            g2.fillRect(x + offset, y + h - offset - 1, w - offset, offset);
        } else {
            g2.setColor(lineColor);
            g2.setStroke(new BasicStroke(2));
            g2.drawRect(x, y, w - 1, h - 1);
        }
        g2.dispose();
    }

    @Override
    public Insets getBorderInsets(Component c) {
        return new Insets(4, 4, offset + 4, offset + 4);
    }
}
