package dev.mahiro.trollhack.setting;

import com.google.gson.JsonElement;

import java.util.function.Supplier;

public final class FloatSetting extends NumberSetting<Float> {
    public FloatSetting(String name, float defaultValue, float min, float max, float step) {
        this(name, defaultValue, min, max, step, step, null, "", false);
    }

    public FloatSetting(
            String name,
            float defaultValue,
            float min,
            float max,
            float step,
            float fineStep,
            Supplier<Boolean> visibility,
            String description,
            boolean isTransient
    ) {
        super(name, defaultValue, min, max, step, fineStep, visibility, description, isTransient);
    }

    public float get() {
        return getValue();
    }

    public void set(float value) {
        setValue(value);
    }

    @Override
    public void setFromDouble(double value) {
        setValue((float) normalizeToRange(value));
    }

    @Override
    protected Float fromDouble(double value) {
        return (float) value;
    }

    @Override
    public void read(JsonElement element) {
        if (element != null && element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()) {
            setFromDouble(element.getAsDouble());
        }
    }

    @Override
    public Class<Float> getValueClass() {
        return Float.class;
    }
}

