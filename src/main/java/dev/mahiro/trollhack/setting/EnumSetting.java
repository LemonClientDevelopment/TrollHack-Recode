package dev.mahiro.trollhack.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import java.util.Locale;
import java.util.Objects;
import java.util.function.Supplier;

public final class EnumSetting<E extends Enum<E>> extends Setting<E> {
    private final Class<E> enumClass;
    private final E[] values;

    public EnumSetting(String name, E defaultValue) {
        this(name, defaultValue, null, "", false);
    }

    public EnumSetting(String name, E defaultValue, Supplier<Boolean> visibility, String description, boolean isTransient) {
        super(name, Objects.requireNonNull(defaultValue, "defaultValue"), visibility, description, isTransient);
        //noinspection unchecked
        this.enumClass = (Class<E>) defaultValue.getDeclaringClass();
        this.values = enumClass.getEnumConstants();
    }

    public E get() {
        return getValue();
    }

    public void set(E value) {
        setValue(value);
    }

    public void nextValue() {
        E current = getValue();
        int index = current.ordinal() + 1;
        if (index >= values.length) index = 0;
        setValue(values[index]);
    }

    public void previousValue() {
        E current = getValue();
        int index = current.ordinal() - 1;
        if (index < 0) index = values.length - 1;
        setValue(values[index]);
    }

    @Override
    public JsonElement write() {
        return new JsonPrimitive(getValue().name());
    }

    @Override
    public void read(JsonElement element) {
        if (element == null || !element.isJsonPrimitive()) return;
        String s = element.getAsString();
        if (s == null) return;
        String normalized = s.trim().toUpperCase(Locale.ROOT).replace(' ', '_');
        for (E v : values) {
            if (v.name().equalsIgnoreCase(normalized)) {
                setValue(v);
                return;
            }
        }
    }

    @Override
    public Class<E> getValueClass() {
        return enumClass;
    }
}

