package view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.awt.Component;
import java.awt.Container;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.junit.jupiter.api.Test;
import util.NeoBrutalTheme;

class NeoPageHeaderTest {

    @Test
    void rendersKickerTitleAndSubtitle() {
        NeoPageHeader header = new NeoPageHeader(
                "MASTER DATA", "Data Buku", "Kelola katalog.", new JPanel());

        JLabel kicker = find(header, "pageKicker");
        JLabel title = find(header, "pageTitle");
        JLabel subtitle = find(header, "pageSubtitle");

        assertNotNull(kicker);
        assertNotNull(title);
        assertNotNull(subtitle);
        assertEquals("MASTER DATA", kicker.getText());
        assertEquals("Data Buku", title.getText());
        assertEquals("Kelola katalog.", subtitle.getText());
        assertEquals(NeoBrutalTheme.INK, title.getForeground());
    }

    private static JLabel find(Container root, String name) {
        for (Component c : root.getComponents()) {
            if (c instanceof JLabel && name.equals(c.getName())) {
                return (JLabel) c;
            }
            if (c instanceof Container) {
                JLabel found = find((Container) c, name);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }
}
