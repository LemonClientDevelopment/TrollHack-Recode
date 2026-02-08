package dev.mahiro.trollhack.config;

import com.google.gson.JsonObject;

import java.nio.file.Path;

public final class GuiConfig extends AbstractConfig {
    public static final GuiConfig INSTANCE = new GuiConfig();

    private GuiConfig() {
        super("gui", GenericConfig.INSTANCE.directory.resolve("gui"));
    }

    @Override
    public Path getFile() {
        return directory.resolve(GenericConfig.INSTANCE.getGuiPreset() + ".json");
    }

    @Override
    public Path getBackup() {
        return directory.resolve(GenericConfig.INSTANCE.getGuiPreset() + ".bak");
    }

    @Override
    protected JsonObject write() {
        return new JsonObject();
    }

    @Override
    protected void read(JsonObject root) {
    }
}
