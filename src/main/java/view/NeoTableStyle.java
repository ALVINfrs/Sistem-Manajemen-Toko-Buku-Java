package view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import util.NeoBrutalTheme;

/**
 * Styling tabel internal: header hitam, baris cream/putih, seleksi teal.
 */
public final class NeoTableStyle {

    private NeoTableStyle() {
    }

    public static void apply(JTable table) {
        table.setRowHeight(30);
        table.setFont(NeoBrutalTheme.body(Font.PLAIN, 12));
        table.setBackground(NeoBrutalTheme.SURFACE);
        table.setForeground(NeoBrutalTheme.INK);
        table.setGridColor(new Color(0, 0, 0, 46));
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setSelectionBackground(NeoBrutalTheme.BRAND_TEAL);
        table.setSelectionForeground(NeoBrutalTheme.INK);
        table.setFillsViewportHeight(true);
        table.setDefaultRenderer(Object.class, new AlternatingRenderer());

        JTableHeader header = table.getTableHeader();
        header.setOpaque(true);
        header.setBackground(NeoBrutalTheme.INK);
        header.setForeground(NeoBrutalTheme.BG);
        header.setFont(NeoBrutalTheme.mono(10));
        header.setDefaultRenderer(new HeaderRenderer());
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 34));
    }

    public static JScrollPane wrap(JTable table) {
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(NeoBrutalTheme.INK, 2));
        scroll.setBackground(NeoBrutalTheme.SURFACE);
        scroll.getViewport().setBackground(NeoBrutalTheme.SURFACE);
        table.setFillsViewportHeight(true);
        return scroll;
    }

    /** Renderer baris selang-seling yang tetap menghormati state seleksi. */
    public static class AlternatingRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);
            setOpaque(true);
            setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
            if (isSelected) {
                setBackground(NeoBrutalTheme.BRAND_TEAL);
                setForeground(NeoBrutalTheme.INK);
            } else {
                setBackground(row % 2 == 0
                        ? NeoBrutalTheme.SURFACE : NeoBrutalTheme.ROW_ALT);
                setForeground(NeoBrutalTheme.INK);
            }
            return c;
        }
    }

    /** Header tabel hitam dengan border bawah tebal. */
    public static class HeaderRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(
                    table, value, false, false, row, column);
            setOpaque(true);
            setBackground(NeoBrutalTheme.INK);
            setForeground(NeoBrutalTheme.BG);
            setFont(NeoBrutalTheme.mono(10));
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 2, 1, NeoBrutalTheme.INK),
                    BorderFactory.createEmptyBorder(0, 8, 0, 8)));
            return this;
        }
    }
}
