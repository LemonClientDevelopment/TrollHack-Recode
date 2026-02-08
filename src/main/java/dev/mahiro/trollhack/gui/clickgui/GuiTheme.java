package dev.mahiro.trollhack.gui.clickgui;

import java.awt.*;

public final class GuiTheme {
    public static int SCALE_PERCENT = 100;
    private static float prevScale = 1.0f;
    private static float scale = 1.0f;
    private static long lastScaleChangeMs = System.currentTimeMillis();
    private static long lastScaleTickMs = System.currentTimeMillis();

    public static Color PRIMARY = new Color(255, 140, 180, 220);
    public static Color BACKGROUND = new Color(40, 32, 36, 160);
    public static Color TEXT = new Color(255, 250, 253, 255);

    public static boolean PARTICLE = false;
    public static float BACKGROUND_BLUR = 0.0f;
    public static float DARKNESS = 0.25f;
    public static float FADE_IN_TIME_SEC = 0.4f;
    public static float FADE_OUT_TIME_SEC = 0.4f;

    public static float WINDOW_X_MARGIN = 4.0f;
    public static float WINDOW_Y_MARGIN = 1.0f;

    public static boolean WINDOW_OUTLINE = true;
    public static boolean TITLE_BAR = false;
    public static int WINDOW_BLUR_PASS = 2;

    public static int HOVER_ALPHA = 32;

    public static void setScalePercent(int scalePercent) {
        int clamped = clampInt(scalePercent, 50, 400);
        if (clamped == SCALE_PERCENT) {
            return;
        }
        SCALE_PERCENT = clamped;
        lastScaleChangeMs = System.currentTimeMillis();
    }

    public static void tick() {
        prevScale = scale;
        long now = System.currentTimeMillis();
        float dt = (now - lastScaleTickMs) / 1000.0f;
        lastScaleTickMs = now;
        if (dt <= 0.0f) {
            return;
        }
        if (now - lastScaleChangeMs < 500L) {
            return;
        }

        float target = getRoundedScale();
        float diff = target - scale;
        float maxDelta = 0.05f * dt;
        if (Math.abs(diff) <= maxDelta) {
            scale = target;
        } else {
            scale += Math.signum(diff) * maxDelta;
        }
    }

    public static float getScaleFactor(float tickDelta) {
        float interpolated = prevScale + (scale - prevScale) * clamp01(tickDelta);
        return interpolated * 2.0f;
    }

    public static Color getIdleOverlay() {
        float lightness = rgbToHslLightness(PRIMARY);
        return lightness < 0.9f ? new Color(255, 255, 255, 0) : new Color(0, 0, 0, 0);
    }

    public static Color getHoverOverlay() {
        return withAlpha(getIdleOverlay(), clampInt(HOVER_ALPHA, 0, 255));
    }

    public static Color getClickOverlay() {
        return withAlpha(getIdleOverlay(), clampInt(HOVER_ALPHA * 2, 0, 255));
    }

    private static float getRoundedScale() {
        float raw = SCALE_PERCENT / 100.0f;
        return Math.round(raw / 0.1f) * 0.1f;
    }

    private static float rgbToHslLightness(Color color) {
        float r = color.getRed() / 255.0f;
        float g = color.getGreen() / 255.0f;
        float b = color.getBlue() / 255.0f;
        float max = Math.max(r, Math.max(g, b));
        float min = Math.min(r, Math.min(g, b));
        return (max + min) * 0.5f;
    }

    private static Color withAlpha(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), clampInt(alpha, 0, 255));
    }

    private static int clampInt(int v, int min, int max) {
        if (v < min) return min;
        if (v > max) return max;
        return v;
    }

    private static float clamp01(float v) {
        if (v < 0.0f) return 0.0f;
        if (v > 1.0f) return 1.0f;
        return v;
    }

    private GuiTheme() {
    }
}
