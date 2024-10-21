package net.mitask.reveonlib.config;

import net.mitask.reveonlib.config.annotations.Config;
import net.mitask.reveonlib.config.impl.JsonConfig;
import net.mitask.reveonlib.config.impl.YamlConfig;

import java.io.File;
import java.nio.file.Paths;

@SuppressWarnings("unused")
public class ConfigLoader {
    public static <T> ConfigWrapper<T> createConfig(Class<T> configClass) {
        Config configMetadata = configClass.getAnnotation(Config.class);
        if (configMetadata == null) {
            throw new IllegalArgumentException("Class must be annotated with @Config.");
        }

        String name = configMetadata.name();
        String path = configMetadata.path();
        Config.FormatType format = configMetadata.format();
        File configFile = new File(Paths.get(".").toAbsolutePath() + path, name + "." + format.name().toLowerCase());

        AbstractConfig<T> configHandler;
        if (format == Config.FormatType.JSON) {
            configHandler = new JsonConfig<>(configFile, configClass);
        } else {
            configHandler = new YamlConfig<>(configFile, configClass);
        }

        return new ConfigWrapper<>(configHandler.load(), configHandler);
    }
}

