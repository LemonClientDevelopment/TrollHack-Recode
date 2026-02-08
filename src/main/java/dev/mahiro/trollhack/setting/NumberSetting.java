package dev.mahiro.trollhack.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import java.util.Objects;
import java.util.function.Supplier;

public abstract class NumberSetting<T extends Number> extends Setting<T> {
    private final double min;
    private final double max;
    private final double step;
    private final double fineStep;

    protected NumberSetting(
            String name,
            T defaultValue,
            double min,
            double max,
            double step,
            double fineStep,
            Supplier<Boolean> visibility,
            String description,
            boolean isTransient
    ) {
        super(name, defaultValue, visibility, description, isTransient);
        if (max < min) throw new IllegalArgumentException("max < min");
        this.min = min;
        this.max = max;
        this.step = step;
        this.fineStep = fineStep;
    }

    public final double getMin() {
        return min;
    }

    public final double getMax() {
        return max;
    }

    public final double getStep() {
        return step;
    }

    public final double getFineStep() {
        return fineStep;
    }

    public abstract void setFromDouble(double value);

    @Override
    public void setValueFromString(String value) {
        if (value == null) return;
        try {
            setFromDouble(Double.parseDouble(value.trim()));
        } catch (NumberFormatException ignored) {
        }
    }

    public final double normalizeToRange(double value) {
        double clamped = Math.max(min, Math.min(max, value));
        if (step > 0.0) {
            double snapped = Math.round((clamped - min) / step) * step + min;
            return Math.max(min, Math.min(max, snapped));
        }
        return clamped;
    }

    @Override
    protected T sanitize(T prev, T input) {
        Objects.requireNonNull(input, "input");
        double normalized = normalizeToRange(input.doubleValue());
        return fromDouble(normalized);
    }

    protected abstract T fromDouble(double value);

    @Override
    public JsonElement write() {
        return new JsonPrimitive(getValue());
    }
}

