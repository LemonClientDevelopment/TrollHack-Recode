package dev.mahiro.trollhack.setting;

import com.google.gson.JsonElement;

import java.util.function.Supplier;

public final class IntSetting extends NumberSetting<Integer> {
    public IntSetting(String name, int defaultValue, int min, int max, int step) {
        this(name, defaultValue, min, max, step, step, null, "", false);
    }

    public IntSetting(
            String name,
            int defaultValue,
            int min,
            int max,
            int step,
            int fineStep,
            Supplier<Boolean> visibility,
            String description,
            boolean isTransient
    ) {
        super(name, defaultValue, min, max, step, fineStep, visibility, description, isTransient);
    }

    public int get() {
        return getValue();
    }

    public void set(int value) {
        setValue(value);
    }

    @Override
    public void setFromDouble(double value) {
        setValue((int) Math.round(normalizeToRange(value)));
    }

    @Override
    protected Integer fromDouble(double value) {
        return (int) Math.round(value);
    }

    @Override
    public void read(JsonElement element) {
        if (element != null && element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()) {
            setFromDouble(element.getAsDouble());
        }
    }

    @Override
    public Class<Integer> getValueClass() {
        return Integer.class;
    }
}

