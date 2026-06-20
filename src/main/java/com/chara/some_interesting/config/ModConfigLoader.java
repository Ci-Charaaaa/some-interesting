package com.chara.some_interesting.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ModConfigLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger("some-interesting");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "some-interesting.json";

    public static void load() {
        Path path = getConfigPath();

        if (!Files.exists(path)) {
            ModConfig.set(new ModConfig());
            save();
            LOGGER.info("[Config] Generated default config at {}", path);
            return;
        }

        try {
            String json = Files.readString(path);
            ModConfig config = GSON.fromJson(json, ModConfig.class);
            if (config == null) config = new ModConfig();
            ModConfig.set(config);
            LOGGER.info("[Config] Loaded config from {}", path);
        } catch (Exception e) {
            LOGGER.error("[Config] Failed to load config, using defaults", e);
            ModConfig.set(new ModConfig());
        }
    }

    public static void save() {
        Path path = getConfigPath();
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, GSON.toJson(ModConfig.get()));
        } catch (IOException e) {
            LOGGER.error("[Config] Failed to save config", e);
        }
    }

    public static void reload() {
        load();
        LOGGER.info("[Config] Config reloaded");
    }

    private static Path getConfigPath() {
        return FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
    }
}
