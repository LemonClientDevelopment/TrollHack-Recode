package dev.mahiro.trollhack.setting;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public abstract class Setting<T> {
    protected static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final String name;
    private final String description;
    private final Supplier<Boolean> visibility;
    private final boolean isTransient;

    private final T defaultValue;
    private T value;

    private final List<Runnable> listeners = new ArrayList<>();
    private final List<BiConsumer<T, T>> valueListeners = new ArrayList<>();

    protected Setting(
        String name,
        T defaultValue,
        Supplier<Boolean> visibility,
        String description,
        boolean isTransient
    ) {
        this.name = Objects.requireNonNull(name, "name");
        this.defaultValue = Objects.requireNonNull(defaultValue, "defaultValue");
        this.value = defaultValue;
        this.visibility = visibility;
        this.description = description == null ? "" : description;
        this.isTransient = isTransient;
    }

    public final String getName() {
        return name;
    }

    public final String getDescription() {
        return description;
    }

    public final boolean isVisible() {
        return visibility == null || Boolean.TRUE.equals(visibility.get());
    }

    public final boolean isModified() {
        return !Objects.equals(value, defaultValue);
    }

    public final boolean isTransient() {
        return isTransient;
    }

    public final T getDefaultValue() {
        return defaultValue;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        if (Objects.equals(this.value, value)) return;
        T prev = this.value;
        T next = sanitize(prev, value);
        this.value = next;
        for (BiConsumer<T, T> listener : valueListeners) {
            listener.accept(prev, next);
        }
        for (Runnable listener : listeners) {
            listener.run();
        }
    }

    protected T sanitize(T prev, T input) {
        return input;
    }

    public void resetValue() {
        setValue(defaultValue);
    }

    public void addListener(Runnable listener) {
        listeners.add(Objects.requireNonNull(listener, "listener"));
    }

    public void addValueListener(BiConsumer<T, T> listener) {
        valueListeners.add(Objects.requireNonNull(listener, "listener"));
    }

    public void setValueFromString(String value) {
        read(JsonParser.parseString(value));
    }

    public JsonElement write() {
        return GSON.toJsonTree(value);
    }

    public void read(JsonElement element) {
        setValue(GSON.fromJson(element, getValueClass()));
    }

    public abstract Class<T> getValueClass();
}
