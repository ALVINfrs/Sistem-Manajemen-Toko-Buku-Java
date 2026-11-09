package util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;

public class MotionTest {

    @Test
    void easingEndpointsAndMonotonic() {
        assertEquals(0.0, Motion.EASE_OUT_CUBIC.apply(0), 1e-9);
        assertEquals(1.0, Motion.EASE_OUT_CUBIC.apply(1), 1e-9);
        double a = Motion.EASE_OUT_CUBIC.apply(0.25);
        double b = Motion.EASE_OUT_CUBIC.apply(0.50);
        double c = Motion.EASE_OUT_CUBIC.apply(0.75);
        assertTrue(a < b && b < c && c < 1.0);
    }

    @Test
    void easeOutBackOvershootsThenEndsAtOne() {
        assertTrue(Motion.EASE_OUT_BACK.apply(0.7) > 1.0);
        assertEquals(1.0, Motion.EASE_OUT_BACK.apply(1.0), 1e-6);
    }

    @Test
    void animateEndsAtOneOnEdt() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Double> last = new AtomicReference<>(0.0);
        AtomicBoolean edt = new AtomicBoolean(false);
        Motion.animate(120, Motion.LINEAR,
                p -> last.set(p),
                () -> {
                    edt.set(SwingUtilities.isEventDispatchThread());
                    latch.countDown();
                });
        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertEquals(1.0, last.get(), 1e-9);
        assertTrue(edt.get());
    }
}
