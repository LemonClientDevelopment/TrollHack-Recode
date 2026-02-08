package dev.mahiro.trollhack.module;

import dev.mahiro.trollhack.event.EventHandler;
import dev.mahiro.trollhack.event.IEventBus;
import dev.mahiro.trollhack.event.events.input.KeyPressEvent;
import dev.mahiro.trollhack.gui.clickgui.ClickGuiScreen;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class ModuleManager {
    private final List<Module> modules = new ArrayList<>();
    private final Map<String, Module> moduleByName = new LinkedHashMap<>();

    private boolean loaded;

    public ModuleManager() {
    }

    public boolean isLoaded() {
        return loaded;
    }

    public void register(Module module) {
        Objects.requireNonNull(module, "module");
        if (loaded) throw new IllegalStateException("ModuleManager already loaded");

        String normalizedName = normalize(module.getName());
        if (moduleByName.containsKey(normalizedName)) {
            throw new IllegalArgumentException("Duplicate module name: " + module.getName());
        }

        modules.add(module);
        moduleByName.put(normalizedName, module);
    }

    public void load() {
        if (loaded) return;
        loaded = true;

        modules.sort(Comparator.comparing(Module::getName, String.CASE_INSENSITIVE_ORDER));

        for (Module module : modules) {
            if (module.isEnabledByDefault()) {
                module.setEnabled(true);
            }
        }
    }

    public List<Module> getModules() {
        return Collections.unmodifiableList(modules);
    }

    public List<Module> getModules(Category category) {
        Objects.requireNonNull(category, "category");

        List<Module> result = new ArrayList<>();
        for (Module module : modules) {
            if (module.getCategory() == category) result.add(module);
        }

        result.sort(Comparator.comparing(Module::getName, String.CASE_INSENSITIVE_ORDER));
        return result;
    }

    public List<Module> getEnabledModules() {
        List<Module> result = new ArrayList<>();
        for (Module module : modules) {
            if (module.isEnabled()) result.add(module);
        }

        result.sort(Comparator.comparing(Module::getName, String.CASE_INSENSITIVE_ORDER));
        return result;
    }

    public Optional<Module> getModule(String name) {
        if (name == null || name.isBlank()) return Optional.empty();
        return Optional.ofNullable(moduleByName.get(normalize(name)));
    }

    public void disableAll() {
        for (Module module : modules) {
            if (module.isEnabled()) module.setEnabled(false);
        }
    }

    private static String normalize(String name) {
        return name.trim().toLowerCase(Locale.ROOT);
    }

    @EventHandler
    private void onKeyPress(KeyPressEvent event) {
        if (event.getKeyCode() != GLFW.GLFW_KEY_RIGHT_SHIFT) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.currentScreen instanceof ClickGuiScreen) {
            client.setScreen(null);
        } else {
            client.setScreen(new ClickGuiScreen());
        }
    }
}
