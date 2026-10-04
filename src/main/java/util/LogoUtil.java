package util;

import java.awt.Color;
import java.awt.Image;
import java.net.URL;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.kordamp.ikonli.materialdesign2.MaterialDesignB;
import org.kordamp.ikonli.swing.FontIcon;

/**
 * Logo toko terpusat. Memuat logo AJB dari classpath dan menskalakan
 * proporsional berdasarkan tinggi target. Bila file tidak ada (tidak
 * seharusnya terjadi), fallback ke ikon buku Ikonli agar UI tidak rusak.
 */
public class LogoUtil {

    private LogoUtil() {}

    public static Icon icon(int targetHeight) {
        try {
            URL url = LogoUtil.class.getResource("/images/logo_ajb_transparent.png");
            if (url != null) {
                ImageIcon raw = new ImageIcon(url);
                int w = raw.getIconWidth();
                int h = raw.getIconHeight();
                if (w > 0 && h > 0) {
                    int newH = targetHeight;
                    int newW = Math.max(1, (int) Math.round((double) w * newH / h));
                    Image scaled = raw.getImage().getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
                    return new ImageIcon(scaled);
                }
            }
        } catch (Exception ignored) {
            // fallback di bawah
        }
        return FontIcon.of(MaterialDesignB.BOOK_OPEN_PAGE_VARIANT, targetHeight, Color.BLACK);
    }
}
