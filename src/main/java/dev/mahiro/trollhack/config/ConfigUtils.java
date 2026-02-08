package dev.mahiro.trollhack.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ConfigUtils {
    private ConfigUtils() {
    }

    public static void fixEmptyJson(Path file) throws IOException {
        if (Files.exists(file)) {
            if (Files.size(file) > 0L) return;
        } else {
            Path parent = file.getParent();
            if (parent != null) Files.createDirectories(parent);
            Files.createFile(file);
        }

        Files.writeString(file, "{}", StandardCharsets.UTF_8);
    }
}
