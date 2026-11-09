package view;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.Polygon;
import java.awt.RenderingHints;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.Timer;
import util.NeoBrutalTheme;

/**
 * Panel brand splash: latar maroon dengan bentuk geometris yang mengambang.
 */
public class SplashBrandPanel extends JPanel {

    private static final int STAR = 0;
    private static final int PLUS = 1;
    private static final int CIRCLE = 2;

    private static final int[] TYPES = {
        STAR, PLUS, CIRCLE, STAR, PLUS, CIRCLE, STAR, PLUS, CIRCLE, STAR
    };
    private static final double[] X = {
        0.12, 0.82, 0.28, 0.74, 0.14, 0.62, 0.88, 0.36, 0.68, 0.20
    };
    private static final double[] Y = {
        0.10, 0.16, 0.28, 0.38, 0.52, 0.58, 0.70, 0.78, 0.86, 0.92
    };
    private static final int[] SIZES = {
        18, 22, 14, 16, 24, 18, 20, 14, 22, 16
    };
    private static final Color[] COLORS = {
        NeoBrutalTheme.WARNING, NeoBrutalTheme.PRIMARY, NeoBrutalTheme.SECONDARY,
        NeoBrutalTheme.BRAND_TEAL, NeoBrutalTheme.WARNING, NeoBrutalTheme.PRIMARY,
        NeoBrutalTheme.SECONDARY, NeoBrutalTheme.BRAND_TEAL, NeoBrutalTheme.WARNING,
        NeoBrutalTheme.PRIMARY
    };

    private double phase;
    private Timer timer;

    public SplashBrandPanel(JComponent content) {
        super(new GridBagLayout());
        setBackground(NeoBrutalTheme.BRAND_MAROON);
        setOpaque(true);
        if (content != null) {
            add(content);
        }
    }

    public void start() {
        if (timer == null) {
            timer = new Timer(33, e -> {
                phase += 0.05;
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
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int width = getWidth();
        int height = getHeight();
        for (int i = 0; i < TYPES.length; i++) {
            int cx = (int) Math.round(X[i] * width);
            int cy = (int) Math.round(Y[i] * height + Math.sin(phase + i) * 6);
            int size = SIZES[i];
            g2.setColor(COLORS[i]);
            switch (TYPES[i]) {
                case STAR:
                    g2.fillPolygon(star(cx, cy, size));
                    break;
                case PLUS:
                    g2.fillRect(cx - size / 2, cy - size / 6, size, Math.max(3, size / 3));
                    g2.fillRect(cx - size / 6, cy - size / 2, Math.max(3, size / 3), size);
                    break;
                case CIRCLE:
                default:
                    g2.fillOval(cx - size / 2, cy - size / 2, size, size);
                    break;
            }
        }
        g2.dispose();
    }

    private static Polygon star(int cx, int cy, int size) {
        Polygon polygon = new Polygon();
        double outer = size / 2.0;
        double inner = outer * 0.45;
        for (int i = 0; i < 10; i++) {
            double radius = i % 2 == 0 ? outer : inner;
            double angle = -Math.PI / 2 + i * Math.PI / 5;
            polygon.addPoint(
                    (int) Math.round(cx + Math.cos(angle) * radius),
                    (int) Math.round(cy + Math.sin(angle) * radius));
        }
        return polygon;
    }
}
