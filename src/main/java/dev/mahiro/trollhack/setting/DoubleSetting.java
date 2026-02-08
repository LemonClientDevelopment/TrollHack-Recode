package dev.mahiro.trollhack.setting;

import com.google.gson.JsonElement;

import java.util.function.Supplier;

public final class DoubleSetting extends NumberSetting<Double> {
    public DoubleSetting(String name, double defaultValue, double min, double max, double step) {
        this(name, defaultValue, min, max, step, step, null, "", false);
    }

    public DoubleSetting(
            String name,
            double defaultValue,
            double min,
            double max,
            double step,
            double fineStep,
            Supplier<Boolean> visibility,
            String description,
            boolean isTransient
    ) {
        super(name, defaultValue, min, max, step, fineStep, visibility, description, isTransient);
    }

    public double get() {
        return getValue();
    }

    public void set(double value) {
        setValue(value);
    }

    @Override
    public void setFromDouble(double value) {
        setValue(normalizeToRange(value));
    }

    @Override
    protected Double fromDouble(double value) {
        return value;
    }

    @Override
    public void read(JsonElement element) {
        if (element != null && element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()) {
            setFromDouble(element.getAsDouble());
        }
    }

    @Override
    public Class<Double> getValueClass() {
        return Double.class;
    }
}

