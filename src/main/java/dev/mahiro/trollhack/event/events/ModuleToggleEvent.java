package dev.mahiro.trollhack.event.events;

import dev.mahiro.trollhack.module.Module;

import java.util.Objects;

public final class ModuleToggleEvent {
    private final Module module;
    private final boolean enabled;

    public ModuleToggleEvent(Module module, boolean enabled) {
        this.module = Objects.requireNonNull(module, "module");
        this.enabled = enabled;
    }

    public Module getModule() {
        return module;
    }

    public boolean isEnabled() {
        return enabled;
    }
}

