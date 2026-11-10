package util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.BasicStroke;
import java.awt.Rectangle;
import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.junit.jupiter.api.Test;

class NeoChartStyleTest {

    @Test
    void stylesBarChartWithoutChangingData() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(10, "Omzet", "Sen");
        dataset.addValue(20, "Omzet", "Sel");
        JFreeChart chart = ChartFactory.createBarChart(
                "Tes", "Hari", "Rp", dataset,
                PlotOrientation.VERTICAL, false, true, false);

        NeoChartStyle.apply(chart);

        CategoryPlot plot = chart.getCategoryPlot();
        assertEquals(NeoBrutalTheme.BG, plot.getBackgroundPaint());
        assertEquals(NeoBrutalTheme.INK, plot.getOutlinePaint());
        assertEquals(NeoBrutalTheme.INK, plot.getRangeAxis().getAxisLinePaint());
        assertEquals(NeoBrutalTheme.INK, plot.getDomainAxis().getAxisLinePaint());

        BarRenderer renderer = assertInstanceOf(BarRenderer.class, plot.getRenderer());
        assertTrue(renderer.getShadowsVisible());
        assertEquals(NeoBrutalTheme.INK, renderer.getShadowPaint());
        assertEquals(3.0, renderer.getShadowXOffset(), 0.001);
        assertEquals(3.0, renderer.getShadowYOffset(), 0.001);
        assertEquals(10.0, dataset.getValue(0, 0).doubleValue(), 0.001);
        assertEquals(20.0, dataset.getValue(0, 1).doubleValue(), 0.001);
        assertEquals(NeoBrutalTheme.BRAND_TEAL, NeoChartStyle.COLORS[0]);
    }

    @Test
    void stylesLineChartWithThickStrokeAndSquareMarkers() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(10, "Omzet", "Sen");
        dataset.addValue(20, "Omzet", "Sel");
        JFreeChart chart = ChartFactory.createLineChart(
                "Tes", "Hari", "Rp", dataset,
                PlotOrientation.VERTICAL, false, true, false);

        NeoChartStyle.apply(chart);

        CategoryPlot plot = chart.getCategoryPlot();
        LineAndShapeRenderer renderer = assertInstanceOf(
                LineAndShapeRenderer.class, plot.getRenderer());
        BasicStroke stroke = assertInstanceOf(
                BasicStroke.class, renderer.getSeriesStroke(0));
        assertTrue(stroke.getLineWidth() >= 3.0f);
        assertTrue(renderer.getSeriesShapesVisible(0));
        assertInstanceOf(Rectangle.class, renderer.getSeriesShape(0));
        assertEquals(NeoBrutalTheme.INK, plot.getRangeAxis().getAxisLinePaint());
    }

    @Test
    void panelAndCardUseNeoSurface() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(10, "Omzet", "Sen");
        JFreeChart chart = ChartFactory.createBarChart(
                "Tes", "Hari", "Rp", dataset,
                PlotOrientation.VERTICAL, false, true, false);
        NeoChartStyle.apply(chart);

        ChartPanel panel = NeoChartStyle.panel(chart);
        assertFalse(panel.isMouseWheelEnabled());
        assertEquals(NeoBrutalTheme.SURFACE, panel.getBackground());

        JPanel card = NeoChartStyle.card(chart);
        assertInstanceOf(CompoundBorder.class, card.getBorder());
        assertEquals(NeoBrutalTheme.SURFACE, card.getBackground());
    }
}
