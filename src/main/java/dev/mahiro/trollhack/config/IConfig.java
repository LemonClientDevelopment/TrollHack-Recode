package dev.mahiro.trollhack.config;

import java.nio.file.Path;

public interface IConfig {
    String getName();

    Path getFile();

    Path getBackup();

    void save();

    void load();
}
