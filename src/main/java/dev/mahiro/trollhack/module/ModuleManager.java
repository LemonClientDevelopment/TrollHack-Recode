package dev.mahiro.trollhack.module;

import dev.mahiro.trollhack.event.EventHandler;
import dev.mahiro.trollhack.event.events.input.KeyPressEvent;
import dev.mahiro.trollhack.gui.clickgui.ClickGuiScreen;
import dev.mahiro.trollhack.setting.Setting;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.Field;
import java.util.*;

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
            collectSettings(module);
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

    private static void collectSettings(Module module) {
        Class<?> clazz = module.getClass();
        while (clazz != null && clazz != Module.class) {
            for (Field field : clazz.getDeclaredFields()) {
                if (!Setting.class.isAssignableFrom(field.getType())) continue;
                try {
                    field.setAccessible(true);
                    Setting<?> setting = (Setting<?>) field.get(module);
                    if (setting != null) {
                        module.addSetting(setting);
                    }
                } catch (Throwable ignored) {
                }
            }
            clazz = clazz.getSuperclass();
        }
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
