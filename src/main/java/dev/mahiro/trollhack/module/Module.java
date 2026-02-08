package dev.mahiro.trollhack.module;

import dev.mahiro.trollhack.TrollHack;
import dev.mahiro.trollhack.event.IEventBus;
import dev.mahiro.trollhack.event.events.ModuleToggleEvent;

import java.util.Objects;

public abstract class Module {
    private final String name;
    private final String description;
    private final Category category;
    private final boolean enabledByDefault;

    private boolean enabled;
    private boolean visible = true;

    private int bindKey = -1;
    private BindMode bindMode = BindMode.TOGGLE;

    protected Module(String name, String description, Category category, boolean enabledByDefault) {
        this.name = Objects.requireNonNull(name, "name");
        this.description = Objects.requireNonNull(description, "description");
        this.category = Objects.requireNonNull(category, "category");
        this.enabledByDefault = enabledByDefault;
    }

    public final String getName() {
        return name;
    }

    public final String getDescription() {
        return description;
    }

    public final Category getCategory() {
        return category;
    }

    public final boolean isEnabledByDefault() {
        return enabledByDefault;
    }

    public final boolean isEnabled() {
        return enabled;
    }

    public final boolean isVisible() {
        return visible;
    }

    public final void setVisible(boolean visible) {
        this.visible = visible;
    }

    public final int getBindKey() {
        return bindKey;
    }

    public final void setBindKey(int bindKey) {
        this.bindKey = bindKey;
    }

    public final BindMode getBindMode() {
        return bindMode;
    }

    public final void setBindMode(BindMode bindMode) {
        this.bindMode = Objects.requireNonNull(bindMode, "bindMode");
    }

    public final void toggle() {
        setEnabled(!enabled);
    }

    public final void setEnabled(boolean enabled) {
        if (this.enabled == enabled) return;
        this.enabled = enabled;

        if (enabled) {
            TrollHack.EVENT_BUS.subscribe(this);
            onEnable();
        } else {
            TrollHack.EVENT_BUS.unsubscribe(this);
            onDisable();
        }

        TrollHack.EVENT_BUS.post(new ModuleToggleEvent(this, enabled));
    }

    protected void onEnable() {
    }

    protected void onDisable() {
    }
}

