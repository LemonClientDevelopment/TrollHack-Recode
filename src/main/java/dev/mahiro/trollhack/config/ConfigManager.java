package dev.mahiro.trollhack.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.mahiro.trollhack.TrollHack;
import dev.mahiro.trollhack.module.BindMode;
import dev.mahiro.trollhack.module.Module;
import dev.mahiro.trollhack.setting.Setting;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public final class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "trollhack.json";

    private ConfigManager() {
    }

    public static void loadAndApply() {
        Path path = getConfigPath();
        if (!Files.exists(path)) return;

        try {
            String json = Files.readString(path, StandardCharsets.UTF_8);
            ClientConfig config = GSON.fromJson(json, ClientConfig.class);
            if (config == null) return;
            apply(config);
        } catch (Exception e) {
            TrollHack.LOGGER.error("Failed to load config", e);
        }
    }

    public static void saveFromRuntime() {
        try {
            Path path = getConfigPath();
            Files.createDirectories(path.getParent());
            ClientConfig config = capture();
            String json = GSON.toJson(config);
            Files.writeString(path, json, StandardCharsets.UTF_8);
        } catch (Exception e) {
            TrollHack.LOGGER.error("Failed to save config", e);
        }
    }

    private static ClientConfig capture() {
        ClientConfig config = new ClientConfig();
        for (Module module : TrollHack.MODULE_MANAGER.getModules()) {
            ModuleConfig moduleConfig = new ModuleConfig();
            moduleConfig.enabled = module.isEnabled();
            moduleConfig.visible = module.isVisible();
            moduleConfig.bindKey = module.getBindKey();
            moduleConfig.bindMode = module.getBindMode().name();

            JsonObject settings = new JsonObject();
            for (Setting<?> setting : module.getSettings()) {
                if (setting.isTransient()) continue;
                settings.add(setting.getName(), setting.write());
            }
            moduleConfig.settings = settings;

            config.modules.put(module.getName(), moduleConfig);
        }
        return config;
    }

    private static void apply(ClientConfig config) {
        for (Map.Entry<String, ModuleConfig> entry : config.modules.entrySet()) {
            String moduleName = entry.getKey();
            ModuleConfig moduleConfig = entry.getValue();
            TrollHack.MODULE_MANAGER.getModule(moduleName).ifPresent(module -> applyModule(module, moduleConfig));
        }
    }

    private static void applyModule(Module module, ModuleConfig moduleConfig) {
        if (moduleConfig == null) return;

        module.setVisible(moduleConfig.visible);
        module.setBindKey(moduleConfig.bindKey);

        try {
            if (moduleConfig.bindMode != null && !moduleConfig.bindMode.isBlank()) {
                module.setBindMode(BindMode.valueOf(moduleConfig.bindMode.trim().toUpperCase(Locale.ROOT)));
            }
        } catch (IllegalArgumentException ignored) {
        }

        if (moduleConfig.settings != null) {
            for (Setting<?> setting : module.getSettings()) {
                JsonElement element = moduleConfig.settings.get(setting.getName());
                if (element != null) {
                    try {
                        setting.read(element);
                    } catch (Exception ignored) {
                    }
                }
            }
        }

        module.setEnabled(moduleConfig.enabled);
    }

    private static Path getConfigPath() {
        return FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
    }

    public static final class ClientConfig {
        public Map<String, ModuleConfig> modules = new LinkedHashMap<>();
    }

    public static final class ModuleConfig {
        public boolean enabled;
        public boolean visible = true;
        public int bindKey = -1;
        public String bindMode = BindMode.TOGGLE.name();
        public JsonObject settings = new JsonObject();
    }
}
