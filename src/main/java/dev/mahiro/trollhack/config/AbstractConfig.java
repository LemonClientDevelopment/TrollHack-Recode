package dev.mahiro.trollhack.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.mahiro.trollhack.TrollHack;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public abstract class AbstractConfig implements IConfig {
    protected static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final String name;
    protected final Path directory;

    protected AbstractConfig(String name, Path directory) {
        this.name = name;
        this.directory = directory;
    }

    @Override
    public final String getName() {
        return name;
    }

    protected void ensureDirectory() throws IOException {
        Files.createDirectories(directory);
    }

    protected abstract JsonObject write();

    protected abstract void read(JsonObject root);

    @Override
    public void save() {
        try {
            ensureDirectory();

            Path file = getFile();
            Path backup = getBackup();
            ConfigUtils.fixEmptyJson(file);
            ConfigUtils.fixEmptyJson(backup);
            if (Files.exists(file)) {
                Files.copy(file, backup, StandardCopyOption.REPLACE_EXISTING);
            }

            JsonObject root = write();
            Files.writeString(file, GSON.toJson(root), StandardCharsets.UTF_8);
        } catch (Exception e) {
            TrollHack.LOGGER.error("Failed to save {} config", getName(), e);
        }
    }

    @Override
    public void load() {
        try {
            ensureDirectory();

            Path file = getFile();
            Path backup = getBackup();
            if (!Files.exists(file) && !Files.exists(backup)) return;

            try {
                ConfigUtils.fixEmptyJson(file);
                JsonObject root = JsonParser.parseString(Files.readString(file, StandardCharsets.UTF_8)).getAsJsonObject();
                read(root);
            } catch (Exception e) {
                TrollHack.LOGGER.warn("Failed to load latest {}, loading backup.", getName());
                ConfigUtils.fixEmptyJson(backup);
                JsonObject root = JsonParser.parseString(Files.readString(backup, StandardCharsets.UTF_8)).getAsJsonObject();
                read(root);
            }
        } catch (Exception e) {
            TrollHack.LOGGER.error("Failed to load {} config", getName(), e);
        }
    }
}
