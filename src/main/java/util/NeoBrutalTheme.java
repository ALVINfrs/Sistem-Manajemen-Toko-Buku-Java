package util;

import com.formdev.flatlaf.FlatLightLaf;
import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import javax.swing.UIManager;

public class NeoBrutalTheme {
    public static final Color BG        = Color.decode("#FFFBEA");
    public static final Color SURFACE   = Color.WHITE;
    public static final Color INK       = Color.BLACK;
    public static final Color PRIMARY   = Color.decode("#FF5A5F");
    public static final Color SECONDARY = Color.decode("#4D9DE0");
    public static final Color SUCCESS   = Color.decode("#2EC4B6");
    public static final Color WARNING   = Color.decode("#FFB703");
    public static final Color DANGER    = Color.decode("#E63946");
    public static final Color BRAND_MAROON = Color.decode("#7A0B2E");
    public static final Color BRAND_TEAL   = Color.decode("#00A7C4");

    private static String displayFamily;
    private static String monoFamily;

    private static String firstAvailable(String fallback, String... names) {
        String[] available = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getAvailableFontFamilyNames();
        for (String name : names) {
            for (String candidate : available) {
                if (candidate.equalsIgnoreCase(name)) {
                    return candidate;
                }
            }
        }
        return fallback;
    }

    /** Font judul tebal; Arial Black/Bahnschrift/Segoe UI Black bila tersedia. */
    public static Font display(int size) {
        if (displayFamily == null) {
            displayFamily = firstAvailable(Font.SANS_SERIF,
                    "Arial Black", "Bahnschrift", "Segoe UI Black");
        }
        return new Font(displayFamily, Font.BOLD, size);
    }

    /** Font aksen monospace; Consolas/Cascadia Mono/Courier New bila tersedia. */
    public static Font mono(int size) {
        if (monoFamily == null) {
            monoFamily = firstAvailable(Font.MONOSPACED,
                    "Consolas", "Cascadia Mono", "Courier New");
        }
        return new Font(monoFamily, Font.BOLD, size);
    }

    public static void apply() {
        FlatLightLaf.setup();

        // Sudut tegas, tanpa rounded corner
        UIManager.put("Button.arc", 0);
        UIManager.put("Component.arc", 0);
        UIManager.put("ProgressBar.arc", 0);
        UIManager.put("TextComponent.arc", 0);
        UIManager.put("CheckBox.arc", 0);
        UIManager.put("ScrollBar.thumbArc", 0);

        // Border tebal, tanpa efek focus glow bawaan
        UIManager.put("Component.focusWidth", 0);
        UIManager.put("Component.innerFocusWidth", 0);
        UIManager.put("Component.borderWidth", 2);
        UIManager.put("Button.borderWidth", 2);
        UIManager.put("Component.borderColor", INK);
        UIManager.put("Button.borderColor", INK);
        UIManager.put("Button.default.borderColor", INK);

        // Warna dasar
        UIManager.put("Panel.background", BG);
        UIManager.put("Button.background", SURFACE);
        UIManager.put("Button.foreground", INK);
        UIManager.put("Button.default.background", PRIMARY);
        UIManager.put("Button.default.foreground", INK);
        UIManager.put("TextField.background", SURFACE);
        UIManager.put("ComboBox.background", SURFACE);

        // Tabel
        UIManager.put("Table.gridColor", INK);
        UIManager.put("Table.showHorizontalLines", true);
        UIManager.put("Table.showVerticalLines", true);
        UIManager.put("TableHeader.background", INK);
        UIManager.put("TableHeader.foreground", Color.WHITE);
        UIManager.put("TableHeader.font", new Font("Segoe UI", Font.BOLD, 13));

        // Font default
        UIManager.put("defaultFont", new Font("Segoe UI Semibold", Font.PLAIN, 13));
    }
}
