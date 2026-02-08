package dev.mahiro.trollhack.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import java.util.function.Supplier;

public final class BoolSetting extends Setting<Boolean> {
    public BoolSetting(String name, boolean defaultValue) {
        this(name, defaultValue, null, "", false);
    }

    public BoolSetting(String name, boolean defaultValue, Supplier<Boolean> visibility, String description, boolean isTransient) {
        super(name, defaultValue, visibility, description, isTransient);
    }

    public boolean get() {
        return getValue();
    }

    public void set(boolean value) {
        setValue(value);
    }

    @Override
    public JsonElement write() {
        return new JsonPrimitive(getValue());
    }

    @Override
    public void read(JsonElement element) {
        if (element != null && element.isJsonPrimitive() && element.getAsJsonPrimitive().isBoolean()) {
            setValue(element.getAsBoolean());
        }
    }

    @Override
    public Class<Boolean> getValueClass() {
        return Boolean.class;
    }
}

