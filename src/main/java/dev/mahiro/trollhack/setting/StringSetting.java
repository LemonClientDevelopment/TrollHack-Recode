package dev.mahiro.trollhack.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import java.util.function.Supplier;

public final class StringSetting extends Setting<String> {
    private final boolean onlyNumber;

    public StringSetting(String name, String defaultValue) {
        this(name, defaultValue, false, null, "", false);
    }

    public StringSetting(String name, String defaultValue, boolean onlyNumber, Supplier<Boolean> visibility, String description, boolean isTransient) {
        super(name, defaultValue == null ? "" : defaultValue, visibility, description, isTransient);
        this.onlyNumber = onlyNumber;
    }

    public boolean isOnlyNumber() {
        return onlyNumber;
    }

    public String get() {
        return getValue();
    }

    public void set(String value) {
        setValue(value == null ? "" : value);
    }

    @Override
    public JsonElement write() {
        return new JsonPrimitive(getValue());
    }

    @Override
    public void read(JsonElement element) {
        if (element != null && element.isJsonPrimitive()) {
            set(element.getAsString());
        }
    }

    @Override
    public Class<String> getValueClass() {
        return String.class;
    }
}

