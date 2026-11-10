package view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import org.junit.jupiter.api.Test;
import util.NeoBrutalTheme;

class NeoFormStyleTest {

    @Test
    void fieldUsesNeoThemeAndKeepsName() {
        JTextField field = NeoFormStyle.field("judul");

        assertEquals("judul", field.getName());
        assertEquals(NeoBrutalTheme.SURFACE, field.getBackground());
        assertTrue(field.getBorder() instanceof CompoundBorder
                || field.getBorder() instanceof LineBorder);
    }

    @Test
    void buttonUsesNeoButtonAndDisplayFont() {
        NeoButton button = NeoFormStyle.button("Simpan", null, NeoBrutalTheme.PRIMARY);

        assertEquals(NeoBrutalTheme.PRIMARY, button.getFillColor());
        assertEquals(NeoBrutalTheme.display(13).getFamily(), button.getFont().getFamily());
    }
}
