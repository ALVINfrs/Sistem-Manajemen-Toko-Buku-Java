package view;

import java.awt.AlphaComposite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import javax.swing.JPanel;

/**
 * Panel yang bisa di-fade dan digeser tanpa mengubah layout manager.
 * Dipakai untuk entrance animation halaman auth.
 */
public class MotionPanel extends JPanel {

    private float alpha = 1f;
    private int offsetX;
    private int offsetY;

    public MotionPanel() {
        this(null);
    }

    public MotionPanel(LayoutManager layout) {
        super(layout);
        setOpaque(false);
    }

    public void setAlpha(float alpha) {
        this.alpha = Math.max(0f, Math.min(1f, alpha));
        repaint();
    }

    public float getAlpha() {
        return alpha;
    }

    public void setOffset(int x, int y) {
        this.offsetX = x;
        this.offsetY = y;
        repaint();
    }

    public int getOffsetX() {
        return offsetX;
    }

    public int getOffsetY() {
        return offsetY;
    }

    @Override
    protected void paintChildren(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setComposite(AlphaComposite.SrcOver.derive(alpha));
        g2.translate(offsetX, offsetY);
        super.paintChildren(g2);
        g2.dispose();
    }
}
