package me.gamerduck.detoxify.api;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.loader.ConfigurationLoader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigManager {
    private final ConfigurationLoader<?> loader;
    private final Path configPath;
    private Config config;
    private final ClassLoader classLoader;

    public ConfigManager(Path configPath, ClassLoader classLoader) {
        this.configPath = configPath;
        this.classLoader = classLoader;
        this.loader = HoconConfigurationLoader.builder()
                .path(configPath)
                .defaultOptions(opts -> opts.shouldCopyDefaults(true))
                .build();
    }

    public void load() throws ConfigurateException {
        try {
            // ✅ Ensure directory exists
            Files.createDirectories(configPath.getParent());

            // ✅ Copy from JAR if missing
            if (Files.notExists(configPath)) {
                try (InputStream in = classLoader.getResourceAsStream("config.conf")) {
                    if (in != null) {
                        Files.copy(in, configPath);
                        System.out.println("[Detoxify] Default config copied from JAR.");
                    } else {
                        System.err.println("[Detoxify] Could not find config.conf in JAR resources!");
                    }
                }
            }
        } catch (IOException e) {
            throw new ConfigurateException("Failed to prepare config file", e);
        }

        // ✅ Load into object
        ConfigurationNode root = loader.load();
        config = root.get(Config.class);

        // ✅ Always re-save to ensure missing defaults are added
        root.set(Config.class, config);
        loader.save(root);
    }

    public void save() throws ConfigurateException {
        ConfigurationNode root = loader.createNode();
        root.set(Config.class, config);
        loader.save(root);
    }

    public void reload() throws ConfigurateException {
        ConfigurationNode root = loader.load();
        Config newConfig = root.get(Config.class);

        if (newConfig != null) {
            this.config = newConfig;
            System.out.println("[Detoxify] Configuration reloaded successfully!");
        } else {
            System.err.println("[Detoxify] Failed to reload configuration (null object). Keeping old config.");
        }
    }

    public Config config() {
        return config;
    }
}
