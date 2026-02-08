package dev.mahiro.trollhack.gui.clickgui.anim;

public enum Easing {
    OUT_CUBIC,
    IN_CUBIC,
    OUT_QUART,
    OUT_EXPO,
    OUT_BACK;

    public float apply(float t) {
        t = clamp01(t);
        return switch (this) {
            case OUT_CUBIC -> 1.0f - pow3(1.0f - t);
            case IN_CUBIC -> pow3(t);
            case OUT_QUART -> 1.0f - pow4(1.0f - t);
            case OUT_EXPO -> t == 1.0f ? 1.0f : 1.0f - (float) Math.pow(2.0, -10.0 * t);
            case OUT_BACK -> {
                float c1 = 1.70158f;
                float c3 = c1 + 1.0f;
                float x = t - 1.0f;
                yield 1.0f + c3 * x * x * x + c1 * x * x;
            }
        };
    }

    public static float clamp01(float t) {
        if (t < 0.0f) return 0.0f;
        if (t > 1.0f) return 1.0f;
        return t;
    }

    private static float pow3(float v) {
        return v * v * v;
    }

    private static float pow4(float v) {
        float v2 = v * v;
        return v2 * v2;
    }
}

