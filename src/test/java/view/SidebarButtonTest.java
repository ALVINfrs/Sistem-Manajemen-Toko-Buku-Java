package view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import org.junit.jupiter.api.Test;
import org.kordamp.ikonli.materialdesign2.MaterialDesignV;
import org.kordamp.ikonli.swing.FontIcon;
import util.NeoBrutalTheme;

class SidebarButtonTest {

    @Test
    void activeStateUsesTealFillAndInkForeground() {
        SidebarButton button = new SidebarButton("Dashboard",
                FontIcon.of(MaterialDesignV.VIEW_DASHBOARD, 18, Color.BLACK));

        button.setActive(true);

        assertTrue(button.isActive());
        assertEquals(NeoBrutalTheme.BRAND_TEAL, button.getActiveFill());
        assertEquals(NeoBrutalTheme.INK, button.getForeground());
    }

    @Test
    void inactiveStateUsesCreamForeground() {
        SidebarButton button = new SidebarButton("Dashboard",
                FontIcon.of(MaterialDesignV.VIEW_DASHBOARD, 18, Color.BLACK));

        button.setActive(false);

        assertEquals(NeoBrutalTheme.BG, button.getForeground());
    }
}
