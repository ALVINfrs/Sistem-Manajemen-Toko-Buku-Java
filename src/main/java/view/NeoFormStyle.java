package view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.LayoutManager;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import util.NeoBrutalTheme;
import util.NeoShadowBorder;

/**
 * Helper styling form internal agar field, card, label, dan tombol konsisten.
 */
public final class NeoFormStyle {

    private NeoFormStyle() {
    }

    public static JTextField field(String name) {
        JTextField field = new JTextField(16);
        field.setName(name);
        field.setFont(NeoBrutalTheme.body(Font.PLAIN, 13));
        field.setBackground(NeoBrutalTheme.SURFACE);
        field.setForeground(NeoBrutalTheme.INK);
        field.setCaretColor(NeoBrutalTheme.INK);
        field.setSelectionColor(NeoBrutalTheme.WARNING);
        field.setSelectedTextColor(NeoBrutalTheme.INK);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(NeoBrutalTheme.INK, 2),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)));
        return field;
    }

    public static <T> JComboBox<T> combo() {
        JComboBox<T> combo = new JComboBox<>();
        styleCombo(combo);
        return combo;
    }

    public static void styleCombo(JComboBox<?> combo) {
        combo.setFont(NeoBrutalTheme.body(Font.PLAIN, 12));
        combo.setBackground(NeoBrutalTheme.SURFACE);
        combo.setForeground(NeoBrutalTheme.INK);
        combo.setBorder(BorderFactory.createLineBorder(NeoBrutalTheme.INK, 2));
        combo.setPreferredSize(new Dimension(160, 34));
    }

    public static void styleTextArea(JTextArea area) {
        area.setFont(NeoBrutalTheme.body(Font.PLAIN, 12));
        area.setBackground(NeoBrutalTheme.SURFACE);
        area.setForeground(NeoBrutalTheme.INK);
        area.setCaretColor(NeoBrutalTheme.INK);
        area.setSelectionColor(NeoBrutalTheme.WARNING);
        area.setSelectedTextColor(NeoBrutalTheme.INK);
        area.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(NeoBrutalTheme.INK, 2),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)));
    }

    public static NeoButton button(String text, Icon icon, Color fill) {
        NeoButton button = new NeoButton(text, icon, fill);
        button.setFont(NeoBrutalTheme.display(13));
        return button;
    }

    public static JPanel card(LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setBackground(NeoBrutalTheme.SURFACE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new NeoShadowBorder(),
                BorderFactory.createEmptyBorder(14, 14, 14, 14)));
        return panel;
    }

    public static JLabel section(String text) {
        JLabel label = new JLabel(text == null ? "" : text.toUpperCase(Locale.ROOT));
        label.setFont(NeoBrutalTheme.mono(10));
        label.setForeground(NeoBrutalTheme.TEXT_MUTED);
        return label;
    }

    public static JLabel label(String text) {
        JLabel label = new JLabel(text == null ? "" : text);
        label.setFont(NeoBrutalTheme.body(Font.BOLD, 12));
        label.setForeground(NeoBrutalTheme.INK);
        return label;
    }
}
