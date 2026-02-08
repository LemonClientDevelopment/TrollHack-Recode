package dev.mahiro.trollhack.config;

import com.google.gson.JsonObject;

import java.nio.file.Path;

public final class GenericConfig extends AbstractConfig {
    public static final GenericConfig INSTANCE = new GenericConfig();

    private String guiPreset = "default";
    private String modulePreset = "default";

    private GenericConfig() {
        super("generic", getDirectoryPath());
    }

    private static Path getDirectoryPath() {
        return GamePaths.getGameDir().resolve("trollhack").resolve("config");
    }

    @Override
    public Path getFile() {
        return directory.resolve("generic.json");
    }

    @Override
    public Path getBackup() {
        return directory.resolve("generic.bak");
    }

    public String getGuiPreset() {
        return guiPreset;
    }

    public void setGuiPreset(String guiPreset) {
        this.guiPreset = guiPreset == null || guiPreset.isBlank() ? "default" : guiPreset;
    }

    public String getModulePreset() {
        return modulePreset;
    }

    public void setModulePreset(String modulePreset) {
        this.modulePreset = modulePreset == null || modulePreset.isBlank() ? "default" : modulePreset;
    }

    @Override
    protected JsonObject write() {
        JsonObject root = new JsonObject();
        root.addProperty("guiPreset", guiPreset);
        root.addProperty("modulePreset", modulePreset);
        return root;
    }

    @Override
    protected void read(JsonObject root) {
        if (root == null) return;

        if (root.has("guiPreset")) {
            setGuiPreset(root.get("guiPreset").getAsString());
        }
        if (root.has("modulePreset")) {
            setModulePreset(root.get("modulePreset").getAsString());
        }
    }
}
