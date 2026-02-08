package dev.mahiro.trollhack.config;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;

import java.nio.file.Path;

public final class GamePaths {
    private GamePaths() {
    }

    public static Path getGameDir() {
        try {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client != null && client.runDirectory != null) {
                return client.runDirectory.toPath().toAbsolutePath();
            }
        } catch (Throwable ignored) {
        }

        try {
            return FabricLoader.getInstance().getGameDir().toAbsolutePath();
        } catch (Throwable ignored) {
        }

        return Path.of(".").toAbsolutePath();
    }
}
