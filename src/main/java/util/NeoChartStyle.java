package util;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Paint;
import java.awt.Rectangle;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.Axis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;

/**
 * Styling chart neobrutalism: plot cream, axis hitam tebal, bar flat + hard shadow.
 */
public final class NeoChartStyle {

    public static final Color[] COLORS = {
        NeoBrutalTheme.BRAND_TEAL,
        NeoBrutalTheme.WARNING,
        NeoBrutalTheme.PRIMARY,
        NeoBrutalTheme.SECONDARY,
        NeoBrutalTheme.BRAND_MAROON
    };

    private static final Color GRID = new Color(0, 0, 0, 35);
    private static final BasicStroke AXIS_STROKE = new BasicStroke(2f);
    private static final BasicStroke BAR_OUTLINE = new BasicStroke(2f);

    private NeoChartStyle() {
    }

    public static void apply(JFreeChart chart) {
        chart.setBackgroundPaint(NeoBrutalTheme.SURFACE);
        chart.setBorderVisible(false);
        if (chart.getTitle() != null) {
            chart.getTitle().setFont(NeoBrutalTheme.display(14));
            chart.getTitle().setPaint(NeoBrutalTheme.INK);
            chart.getTitle().setBorder(new org.jfree.chart.block.BlockBorder(
                    0, 0, 0, 6));
        }

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(NeoBrutalTheme.BG);
        plot.setOutlinePaint(NeoBrutalTheme.INK);
        plot.setOutlineStroke(AXIS_STROKE);
        plot.setRangeGridlinePaint(GRID);
        plot.setRangeGridlineStroke(new BasicStroke(1f));
        plot.setDomainGridlinePaint(new Color(0, 0, 0, 0));
        plot.setDomainGridlinesVisible(false);

        styleAxis(plot.getRangeAxis());
        styleAxis(plot.getDomainAxis());

        if (plot.getRenderer() instanceof BarRenderer) {
            CyclingBarRenderer renderer = new CyclingBarRenderer();
            renderer.setBarPainter(new StandardBarPainter());
            renderer.setShadowVisible(true);
            renderer.setShadowPaint(NeoBrutalTheme.INK);
            renderer.setShadowXOffset(3);
            renderer.setShadowYOffset(3);
            renderer.setDrawBarOutline(true);
            renderer.setSeriesOutlinePaint(0, NeoBrutalTheme.INK);
            renderer.setSeriesOutlineStroke(0, BAR_OUTLINE);
            renderer.setItemMargin(0.10);
            plot.setRenderer(renderer);
        } else if (plot.getRenderer() instanceof LineAndShapeRenderer) {
            LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
            renderer.setSeriesPaint(0, NeoBrutalTheme.BRAND_TEAL);
            renderer.setSeriesStroke(0, new BasicStroke(
                    3.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            renderer.setSeriesShapesVisible(0, true);
            renderer.setSeriesShape(0, new Rectangle(-4, -4, 8, 8));
            renderer.setSeriesOutlinePaint(0, NeoBrutalTheme.INK);
            renderer.setSeriesOutlineStroke(0, BAR_OUTLINE);
        }
    }

    public static ChartPanel panel(JFreeChart chart) {
        ChartPanel panel = new ChartPanel(chart);
        panel.setBackground(NeoBrutalTheme.SURFACE);
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        panel.setPreferredSize(new Dimension(500, 300));
        panel.setMinimumSize(new Dimension(0, 0));
        panel.setMouseWheelEnabled(false);
        panel.setDomainZoomable(false);
        panel.setRangeZoomable(false);
        return panel;
    }

    public static JPanel card(JFreeChart chart) {
        JPanel card = new JPanel(new java.awt.BorderLayout());
        card.setBackground(NeoBrutalTheme.SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new NeoShadowBorder(),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        card.add(panel(chart), java.awt.BorderLayout.CENTER);
        return card;
    }

    /**
     * Memusatkan card chart dan membatasi lebarnya agar plot tidak melar
     * saat window dimaksimalkan.
     */
    public static JPanel centerCard(JPanel card, int maxWidth) {
        return new CenteredCard(card, maxWidth);
    }

    private static final class CenteredCard extends JPanel {

        private final JPanel card;
        private final int maxWidth;

        CenteredCard(JPanel card, int maxWidth) {
            super(null);
            this.card = card;
            this.maxWidth = Math.max(1, maxWidth);
            setOpaque(false);
            add(card);
        }

        @Override
        public void doLayout() {
            int lebar = Math.min(getWidth(), maxWidth);
            int x = Math.max(0, (getWidth() - lebar) / 2);
            card.setBounds(x, 0, lebar, getHeight());
        }

        @Override
        public Dimension getPreferredSize() {
            return card.getPreferredSize();
        }
    }

    private static void styleAxis(Axis axis) {
        if (axis == null) {
            return;
        }
        axis.setAxisLinePaint(NeoBrutalTheme.INK);
        axis.setAxisLineStroke(AXIS_STROKE);
        axis.setTickLabelFont(NeoBrutalTheme.mono(10));
        axis.setTickLabelPaint(NeoBrutalTheme.INK);
        axis.setLabelFont(NeoBrutalTheme.mono(10));
        axis.setLabelPaint(NeoBrutalTheme.INK);
        axis.setTickMarksVisible(false);
    }

    /** Renderer bar dengan warna bergilir per kategori. */
    private static final class CyclingBarRenderer extends BarRenderer {

        @Override
        public Paint getItemPaint(int row, int column) {
            return COLORS[Math.floorMod(column, COLORS.length)];
        }
    }
}
