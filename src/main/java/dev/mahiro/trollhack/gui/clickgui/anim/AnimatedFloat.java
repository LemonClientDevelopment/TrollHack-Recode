package dev.mahiro.trollhack.gui.clickgui.anim;

public final class AnimatedFloat {
    private final Easing easing;
    private final float durationMs;

    private float from;
    private float to;
    private long startMs;

    public AnimatedFloat(Easing easing, float durationMs, float initial) {
        this.easing = easing;
        this.durationMs = durationMs;
        this.from = initial;
        this.to = initial;
        this.startMs = System.currentTimeMillis();
    }

    public float get() {
        float t = (System.currentTimeMillis() - startMs) / durationMs;
        if (t <= 0.0f) return from;
        if (t >= 1.0f) return to;
        float eased = easing.apply(t);
        return from + (to - from) * eased;
    }

    public void update(float value) {
        float current = get();
        this.from = current;
        this.to = value;
        this.startMs = System.currentTimeMillis();
    }

    public void forceUpdate(float value) {
        this.from = value;
        this.to = value;
        this.startMs = System.currentTimeMillis();
    }

    public long getTime() {
        return startMs;
    }
}

