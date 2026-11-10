package view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import org.junit.jupiter.api.Test;
import util.NeoBrutalTheme;

class NeoTableStyleTest {

    @Test
    void appliesHeaderRowAndSelectionColors() {
        JTable table = new JTable(new DefaultTableModel(
                new Object[][]{{"A"}}, new Object[]{"Kolom"}));

        NeoTableStyle.apply(table);

        assertEquals(30, table.getRowHeight());
        assertEquals(NeoBrutalTheme.INK, table.getTableHeader().getBackground());
        assertEquals(NeoBrutalTheme.BG, table.getTableHeader().getForeground());
        assertEquals(NeoBrutalTheme.BRAND_TEAL, table.getSelectionBackground());
        assertTrue(table.getDefaultRenderer(Object.class)
                instanceof NeoTableStyle.AlternatingRenderer);
    }

    @Test
    void wrapAddsBlackBorderAndFillsViewport() {
        JTable table = new JTable(new DefaultTableModel(
                new Object[][]{{"A"}}, new Object[]{"Kolom"}));

        JScrollPane scroll = NeoTableStyle.wrap(table);

        assertTrue(scroll.getBorder() instanceof LineBorder);
        assertTrue(table.getFillsViewportHeight());
    }
}
