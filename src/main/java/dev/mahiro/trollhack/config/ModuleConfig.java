package dev.mahiro.trollhack.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.mahiro.trollhack.TrollHack;
import dev.mahiro.trollhack.module.BindMode;
import dev.mahiro.trollhack.module.Module;
import dev.mahiro.trollhack.setting.Setting;

import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;

public final class ModuleConfig extends AbstractConfig {
    public static final ModuleConfig INSTANCE = new ModuleConfig();

    private ModuleConfig() {
        super("modules", GenericConfig.INSTANCE.directory.resolve("modules"));
    }

    @Override
    public Path getFile() {
        return directory.resolve(GenericConfig.INSTANCE.getModulePreset() + ".json");
    }

    @Override
    public Path getBackup() {
        return directory.resolve(GenericConfig.INSTANCE.getModulePreset() + ".bak");
    }

    @Override
    protected JsonObject write() {
        JsonObject root = new JsonObject();
        for (Module module : TrollHack.MODULE_MANAGER.getModules()) {
            JsonObject moduleObj = new JsonObject();
            moduleObj.addProperty("enabled", module.isEnabled());
            moduleObj.addProperty("visible", module.isVisible());
            moduleObj.addProperty("bindKey", module.getBindKey());
            moduleObj.addProperty("bindMode", module.getBindMode().name());

            JsonObject settingsObj = new JsonObject();
            for (Setting<?> setting : module.getSettings()) {
                if (setting.isTransient()) continue;
                settingsObj.add(setting.getName(), setting.write());
            }
            moduleObj.add("settings", settingsObj);

            root.add(module.getName(), moduleObj);
        }
        return root;
    }

    @Override
    protected void read(JsonObject root) {
        if (root == null) return;

        for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
            if (!(entry.getValue() instanceof JsonObject moduleObj)) continue;
            String moduleName = entry.getKey();
            TrollHack.MODULE_MANAGER.getModule(moduleName).ifPresent(module -> applyModule(module, moduleObj));
        }
    }

    private void applyModule(Module module, JsonObject moduleObj) {
        if (moduleObj.has("visible")) {
            module.setVisible(moduleObj.get("visible").getAsBoolean());
        }
        if (moduleObj.has("bindKey")) {
            module.setBindKey(moduleObj.get("bindKey").getAsInt());
        }
        if (moduleObj.has("bindMode")) {
            try {
                String raw = moduleObj.get("bindMode").getAsString();
                if (raw != null && !raw.isBlank()) {
                    module.setBindMode(BindMode.valueOf(raw.trim().toUpperCase(Locale.ROOT)));
                }
            } catch (Throwable ignored) {
            }
        }

        if (moduleObj.has("settings") && moduleObj.get("settings") instanceof JsonObject settingsObj) {
            for (Setting<?> setting : module.getSettings()) {
                if (!settingsObj.has(setting.getName())) continue;
                try {
                    setting.read(settingsObj.get(setting.getName()));
                } catch (Throwable ignored) {
                }
            }
        }

        if (moduleObj.has("enabled")) {
            module.setEnabled(moduleObj.get("enabled").getAsBoolean());
        }
    }
}
