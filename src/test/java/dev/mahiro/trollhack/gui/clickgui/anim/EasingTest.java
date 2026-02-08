package dev.mahiro.trollhack.gui.clickgui.anim;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EasingTest {
    @Test
    void outCubic_hasExpectedEndpoints() {
        assertEquals(0.0f, Easing.OUT_CUBIC.apply(0.0f), 1.0e-6f);
        assertEquals(1.0f, Easing.OUT_CUBIC.apply(1.0f), 1.0e-6f);
    }

    @Test
    void clamp01_clampsToRange() {
        assertEquals(0.0f, Easing.clamp01(-1.0f), 0.0f);
        assertEquals(0.0f, Easing.clamp01(0.0f), 0.0f);
        assertEquals(0.5f, Easing.clamp01(0.5f), 0.0f);
        assertEquals(1.0f, Easing.clamp01(1.0f), 0.0f);
        assertEquals(1.0f, Easing.clamp01(2.0f), 0.0f);
    }

    @Test
    void outCubic_isMonotonic() {
        float prev = -1.0f;
        for (int i = 0; i <= 100; i++) {
            float t = i / 100.0f;
            float v = Easing.OUT_CUBIC.apply(t);
            assertTrue(v >= prev, "Expected monotonic increasing at t=" + t + ", prev=" + prev + ", got=" + v);
            prev = v;
        }
    }
}
