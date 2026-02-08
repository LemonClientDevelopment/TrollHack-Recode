package dev.mahiro.trollhack.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public final class MultiBoolSetting extends Setting<List<BoolSetting>> {
    private final List<BoolSetting> options;
    private boolean expanded;

    public MultiBoolSetting(String name, List<BoolSetting> options) {
        this(name, options, null, "", false);
    }

    public MultiBoolSetting(String name, List<BoolSetting> options, Supplier<Boolean> visibility, String description, boolean isTransient) {
        super(name, List.of(), visibility, description, isTransient);
        this.options = new ArrayList<>(options == null ? List.of() : options);
    }

    public List<BoolSetting> getOptions() {
        return Collections.unmodifiableList(options);
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    @Override
    public List<BoolSetting> getValue() {
        return getOptions();
    }

    @Override
    public void setValue(List<BoolSetting> value) {
    }

    @Override
    public void resetValue() {
        for (BoolSetting option : options) {
            option.resetValue();
        }
    }

    @Override
    public JsonElement write() {
        JsonObject obj = new JsonObject();
        for (BoolSetting option : options) {
            obj.addProperty(option.getName(), option.get());
        }
        obj.addProperty("_expanded", expanded);
        return obj;
    }

    @Override
    public void read(JsonElement element) {
        if (element == null || !element.isJsonObject()) return;
        JsonObject obj = element.getAsJsonObject();
        for (BoolSetting option : options) {
            if (obj.has(option.getName())) {
                option.read(obj.get(option.getName()));
            }
        }
        if (obj.has("_expanded")) {
            expanded = obj.get("_expanded").getAsBoolean();
        }
    }

    @Override
    public Class<List<BoolSetting>> getValueClass() {
        //noinspection unchecked
        return (Class<List<BoolSetting>>) (Class<?>) List.class;
    }
}

