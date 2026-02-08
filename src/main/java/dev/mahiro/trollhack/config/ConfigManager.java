package dev.mahiro.trollhack.config;

import dev.mahiro.trollhack.TrollHack;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashSet;
import java.util.Set;

public final class ConfigManager {
    private static final Set<IConfig> CONFIGS = new LinkedHashSet<>();
    private static final String DEFAULT_PRESET = "default";

    private ConfigManager() {
    }

    static {
        register(GenericConfig.INSTANCE);
        register(GuiConfig.INSTANCE);
        register(ModuleConfig.INSTANCE);
    }

    public static void register(IConfig config) {
        if (config == null) return;
        CONFIGS.add(config);
    }

    public static void unregister(IConfig config) {
        if (config == null) return;
        CONFIGS.remove(config);
    }

    public static boolean loadAll() {
        migrateFromFabricConfigDir();
        boolean success = load(GenericConfig.INSTANCE);
        for (IConfig config : CONFIGS) {
            if (config == GenericConfig.INSTANCE) continue;
            success = load(config) || success;
        }
        return success;
    }

    public static boolean load(IConfig config) {
        try {
            config.load();
            TrollHack.LOGGER.info("{} config loaded", config.getName());
            return true;
        } catch (Exception e) {
            TrollHack.LOGGER.error("Failed to load {} config", config.getName(), e);
            return false;
        }
    }

    public static boolean saveAll() {
        boolean success = save(GenericConfig.INSTANCE);
        for (IConfig config : CONFIGS) {
            if (config == GenericConfig.INSTANCE) continue;
            success = save(config) || success;
        }
        return success;
    }

    public static boolean save(IConfig config) {
        try {
            config.save();
            TrollHack.LOGGER.info("{} config saved", config.getName());
            return true;
        } catch (Exception e) {
            TrollHack.LOGGER.error("Failed to save {} config", config.getName(), e);
            return false;
        }
    }

    public enum ConfigType {
        GUI(GuiConfig.INSTANCE, GenericConfig.INSTANCE::getGuiPreset, GenericConfig.INSTANCE::setGuiPreset),
        MODULES(ModuleConfig.INSTANCE, GenericConfig.INSTANCE::getModulePreset, GenericConfig.INSTANCE::setModulePreset);

        private final IConfig config;
        private final PresetGetter getter;
        private final PresetSetter setter;

        ConfigType(IConfig config, PresetGetter getter, PresetSetter setter) {
            this.config = config;
            this.getter = getter;
            this.setter = setter;
        }

        public String getPreset() {
            return getter.get();
        }

        public void setPreset(String preset) {
            if (!isValidPresetName(preset)) return;
            save(GenericConfig.INSTANCE);
            setter.set(preset);
            save(GenericConfig.INSTANCE);
            load(config);
        }

        public void copyPreset(String name) {
            if (!isValidPresetName(name)) return;
            if (name.equals(getPreset())) return;

            save(config);

            try {
                Path from = config.getFile();
                Path to = from.getParent().resolve(name + ".json");
                Files.createDirectories(to.getParent());
                Files.copy(from, to, StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception e) {
                TrollHack.LOGGER.error("Failed to copy preset for {}", config.getName(), e);
            }
        }

        public void deletePreset(String name) {
            if (!isValidPresetName(name)) return;
            if (!getAllPresets().contains(name)) return;

            try {
                Path dir = config.getFile().getParent();
                Files.deleteIfExists(dir.resolve(name + ".json"));
                Files.deleteIfExists(dir.resolve(name + ".bak"));
            } catch (Exception e) {
                TrollHack.LOGGER.error("Failed to delete preset for {}", config.getName(), e);
            }

            if (name.equals(getPreset())) {
                setter.set(DEFAULT_PRESET);
                save(GenericConfig.INSTANCE);
                load(config);
            }
        }

        public Set<String> getAllPresets() {
            File dir = config.getFile().getParent().toFile();
            if (!dir.exists() || !dir.isDirectory()) return Set.of();
            File[] files = dir.listFiles();
            if (files == null) return Set.of();

            LinkedHashSet<String> result = new LinkedHashSet<>();
            for (File f : files) {
                if (!f.isFile()) continue;
                if (!f.getName().endsWith(".json")) continue;
                if (f.length() <= 8L) continue;
                String name = f.getName();
                result.add(name.substring(0, name.length() - 5));
            }
            return result;
        }
    }

    private static boolean isValidPresetName(String input) {
        if (input == null) return false;
        String name = input.trim();
        if (name.isEmpty()) return false;
        if (name.endsWith(".json")) name = name.substring(0, name.length() - 5);
        if (name.isEmpty()) return false;

        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            boolean ok = (c >= 'a' && c <= 'z')
                    || (c >= 'A' && c <= 'Z')
                    || (c >= '0' && c <= '9')
                    || c == '-' || c == '_' || c == '.';
            if (!ok) return false;
        }
        return true;
    }

    private static void migrateFromFabricConfigDir() {
        try {
            Path oldBase = net.fabricmc.loader.api.FabricLoader.getInstance().getConfigDir().resolve("trollhack").resolve("config");
            Path newBase = GamePaths.getGameDir().resolve("trollhack").resolve("config");

            if (!Files.exists(oldBase)) return;
            if (Files.exists(newBase)) return;

            Files.createDirectories(newBase);
            try (var stream = Files.walk(oldBase)) {
                stream.forEach(from -> {
                    try {
                        Path relative = oldBase.relativize(from);
                        Path to = newBase.resolve(relative);
                        if (Files.isDirectory(from)) {
                            Files.createDirectories(to);
                        } else {
                            Files.createDirectories(to.getParent());
                            Files.copy(from, to, StandardCopyOption.REPLACE_EXISTING);
                        }
                    } catch (Exception ignored) {
                    }
                });
            }
        } catch (Exception ignored) {
        }
    }

    @FunctionalInterface
    private interface PresetGetter {
        String get();
    }

    @FunctionalInterface
    private interface PresetSetter {
        void set(String value);
    }
}
