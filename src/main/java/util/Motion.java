package util;

import java.awt.Component;
import java.awt.Point;
import javax.swing.Timer;

/**
 * Animasi ringan berbasis {@link Timer} Swing. Semua callback berjalan di EDT.
 * Tidak ada dependency eksternal; easing dihitung manual.
 */
public final class Motion {

    private Motion() {
    }

    public interface Easing {
        double apply(double t);
    }

    public interface Frame {
        void at(double progress);
    }

    public static final Easing LINEAR = t -> t;

    public static final Easing EASE_OUT_CUBIC = t -> 1 - Math.pow(1 - t, 3);

    public static final Easing EASE_IN_OUT_CUBIC = t ->
            t < 0.5 ? 4 * t * t * t : 1 - Math.pow(-2 * t + 2, 3) / 2;

    public static final Easing EASE_OUT_BACK = t -> {
        double c1 = 1.70158;
        double c3 = c1 + 1;
        return 1 + c3 * Math.pow(t - 1, 3) + c1 * Math.pow(t - 1, 2);
    };

    /**
     * Menjalankan animasi selama {@code durationMs} dan memanggil {@code frame}
     * dengan progress yang sudah di-ease (0..1). Frame terakhir selalu 1.0,
     * lalu {@code onDone} dipanggil bila tidak null.
     */
    public static Timer animate(int durationMs, Easing easing, Frame frame, Runnable onDone) {
        final int duration = Math.max(1, durationMs);
        final long start = System.nanoTime();
        final Timer timer = new Timer(16, null);
        timer.addActionListener(e -> {
            long elapsed = System.nanoTime() - start;
            double raw = Math.min(1.0, elapsed / (duration * 1_000_000.0));
            frame.at(easing.apply(raw));
            if (raw >= 1.0) {
                timer.stop();
                frame.at(easing.apply(1.0));
                if (onDone != null) {
                    onDone.run();
                }
            }
        });
        timer.start();
        return timer;
    }

    /** Menjalankan aksi satu kali setelah delay, di EDT. */
    public static Timer delay(int delayMs, Runnable action) {
        final Timer timer = new Timer(Math.max(0, delayMs), e -> action.run());
        timer.setRepeats(false);
        timer.start();
        return timer;
    }

    /**
     * Menggeser komponen kiri-kanan dengan amplitudo yang mengecil, lalu
     * mengembalikannya ke lokasi awal.
     */
    public static Timer shake(Component component, int amplitude, int cycles, Runnable onDone) {
        final Point origin = component.getLocation();
        final int duration = 300;
        final long start = System.nanoTime();
        final Timer timer = new Timer(16, null);
        timer.addActionListener(e -> {
            long elapsed = System.nanoTime() - start;
            double raw = Math.min(1.0, elapsed / (duration * 1_000_000.0));
            double dx = Math.sin(raw * Math.PI * Math.max(1, cycles)) * amplitude * (1 - raw);
            component.setLocation(origin.x + (int) Math.round(dx), origin.y);
            if (raw >= 1.0) {
                timer.stop();
                component.setLocation(origin);
                if (onDone != null) {
                    onDone.run();
                }
            }
        });
        timer.start();
        return timer;
    }
}
