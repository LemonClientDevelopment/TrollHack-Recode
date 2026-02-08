package dev.mahiro.trollhack.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.awt.*;
import java.util.Objects;
import java.util.function.Supplier;

public final class ColorSetting extends Setting<Color> {
    private float hue;
    private float saturation;
    private float brightness;
    private int alpha;

    private boolean rainbow;
    private boolean expanded;
    private final boolean allowAlpha;

    public ColorSetting(String name, Color defaultValue) {
        this(name, defaultValue, true, null, "", false);
    }

    public ColorSetting(String name, Color defaultValue, boolean allowAlpha, Supplier<Boolean> visibility, String description, boolean isTransient) {
        super(name, Objects.requireNonNull(defaultValue, "defaultValue"), visibility, description, isTransient);
        this.allowAlpha = allowAlpha;
        setColorInternal(defaultValue);
    }

    public boolean isAllowAlpha() {
        return allowAlpha;
    }

    public boolean isRainbow() {
        return rainbow;
    }

    public void setRainbow(boolean rainbow) {
        this.rainbow = rainbow;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    public float getHue() {
        return hue;
    }

    public float getSaturation() {
        return saturation;
    }

    public float getBrightness() {
        return brightness;
    }

    public int getAlpha() {
        return alpha;
    }

    public void setHsb(float hue, float saturation, float brightness) {
        this.hue = clamp01(hue);
        this.saturation = clamp01(saturation);
        this.brightness = clamp01(brightness);
        setValue(getColor());
    }

    public void setAlpha(int alpha) {
        this.alpha = Math.max(0, Math.min(255, alpha));
        setValue(getColor());
    }

    public Color getColor() {
        int rgb = Color.HSBtoRGB(hue, saturation, brightness);
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        int a = allowAlpha ? alpha : 255;
        return new Color(r, g, b, a);
    }

    @Override
    public Color getValue() {
        return getColor();
    }

    @Override
    public void setValue(Color value) {
        if (value == null) return;
        setColorInternal(value);
        super.setValue(getColor());
    }

    private void setColorInternal(Color value) {
        float[] hsb = Color.RGBtoHSB(value.getRed(), value.getGreen(), value.getBlue(), null);
        this.hue = hsb[0];
        this.saturation = hsb[1];
        this.brightness = hsb[2];
        this.alpha = value.getAlpha();
    }

    @Override
    public void resetValue() {
        super.resetValue();
        this.rainbow = false;
        setColorInternal(super.getValue());
    }

    @Override
    public JsonElement write() {
        JsonObject obj = new JsonObject();
        obj.addProperty("hue", hue);
        obj.addProperty("saturation", saturation);
        obj.addProperty("brightness", brightness);
        obj.addProperty("alpha", alpha);
        obj.addProperty("rainbow", rainbow);
        obj.addProperty("expanded", expanded);
        obj.addProperty("allowAlpha", allowAlpha);
        return obj;
    }

    @Override
    public void read(JsonElement element) {
        if (element == null || !element.isJsonObject()) return;
        JsonObject obj = element.getAsJsonObject();
        if (obj.has("hue")) hue = clamp01(obj.get("hue").getAsFloat());
        if (obj.has("saturation")) saturation = clamp01(obj.get("saturation").getAsFloat());
        if (obj.has("brightness")) brightness = clamp01(obj.get("brightness").getAsFloat());
        if (obj.has("alpha")) alpha = Math.max(0, Math.min(255, obj.get("alpha").getAsInt()));
        if (obj.has("rainbow")) rainbow = obj.get("rainbow").getAsBoolean();
        if (obj.has("expanded")) expanded = obj.get("expanded").getAsBoolean();
        super.setValue(getColor());
    }

    @Override
    public Class<Color> getValueClass() {
        return Color.class;
    }

    private static float clamp01(float v) {
        if (v < 0.0f) return 0.0f;
        if (v > 1.0f) return 1.0f;
        return v;
    }
}

