package dev.mahiro.trollhack.module;

import dev.mahiro.trollhack.TrollHack;
import dev.mahiro.trollhack.event.EventHandler;
import dev.mahiro.trollhack.event.EventType;
import dev.mahiro.trollhack.event.events.client.TickEvent;
import dev.mahiro.trollhack.event.events.input.KeyActionEvent;
import dev.mahiro.trollhack.gui.clickgui.ClickGuiScreen;
import dev.mahiro.trollhack.gui.clickgui.GuiTheme;
import dev.mahiro.trollhack.module.modules.client.ClickGui;
import dev.mahiro.trollhack.module.modules.client.ExampleModule;
import dev.mahiro.trollhack.module.modules.client.GuiSetting;
import dev.mahiro.trollhack.module.modules.combat.AutoCrystal;
import dev.mahiro.trollhack.module.modules.misc.FakePlayer;
import dev.mahiro.trollhack.module.modules.movement.AutoSprint;
import dev.mahiro.trollhack.setting.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;

import java.lang.reflect.Field;
import java.util.*;

public final class ModuleManager {
    private final List<Module> modules = new ArrayList<>();
    private final Map<String, Module> moduleByName = new LinkedHashMap<>();

    private boolean loaded;

    public ModuleManager() {
        TrollHack.EVENT_BUS.subscribe(this);

        // Client
        register(new ClickGui());
        register(new ExampleModule());
        register(new GuiSetting());

        // Combat
        register(new AutoCrystal());

        // Movement
        register(new AutoSprint());

        // Misc
        register(new FakePlayer());

        load();
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
                try {
                    field.setAccessible(true);
                    Object obj = field.get(module);
                    if (obj instanceof Setting<?> setting) {
                        module.addSetting(setting);
                    }
                } catch (IllegalAccessException ignored) {
                }
            }
            clazz = clazz.getSuperclass();
        }
    }

    @EventHandler
    private void onKeyAction(KeyActionEvent event) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.getWindow() == null) return;
        if (client.getOverlay() != null) return;
        if (InputUtil.isKeyPressed(client.getWindow(), InputUtil.GLFW_KEY_F3)) return;

        int keyCode = event.getKeyCode();
        if (keyCode == InputUtil.UNKNOWN_KEY.getCode()) return;

        if (keyCode == InputUtil.GLFW_KEY_RIGHT_SHIFT && event.isPressed()) {
            if (client.currentScreen instanceof ClickGuiScreen) {
                client.setScreen(null);
            } else if (client.currentScreen == null) {
                client.setScreen(new ClickGuiScreen());
            }
            return;
        }

        if (client.currentScreen != null) return;

        for (Module module : modules) {
            int bindKey = module.getBindKey();
            if (bindKey == -1) continue;
            if (bindKey != keyCode) continue;

            if (module.getBindMode() == BindMode.TOGGLE) {
                if (event.isPressed()) {
                    module.toggle();
                }
            } else if (module.getBindMode() == BindMode.HOLD) {
                module.setEnabled(event.isPressed());
            }
        }
    }

    @EventHandler
    private void onPreTick(TickEvent event) {
        if (event.getType() != EventType.Pre) return;

        GuiTheme.tick();
    }
}
