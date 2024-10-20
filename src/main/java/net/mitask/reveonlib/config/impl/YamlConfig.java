package net.mitask.reveonlib.config.impl;

import net.mitask.reveonlib.config.AbstractConfig;
import org.yaml.snakeyaml.Yaml;

import java.io.*;

public class YamlConfig<T> extends AbstractConfig<T> {
    private final Yaml yaml = new Yaml();
    private final Class<T> clazz;

    public YamlConfig(File file, Class<T> clazz) {
        super(file);
        this.clazz = clazz;
    }

    @Override
    public T loadConfig() {
        try (FileInputStream fis = new FileInputStream(configFile)) {
            return yaml.loadAs(fis, clazz);
        } catch (IOException e) {
            logger.error("Error while loading YamlConfig!", e);
            return getDefault();
        }
    }

    @Override
    public void save(T data) {
        try (FileWriter writer = new FileWriter(configFile)) {
            yaml.dump(data, writer);
            notifyListeners(data);
        } catch (IOException e) {
            logger.error("Error while saving YamlConfig!", e);
        }
    }

    @Override
    public T getDefault() {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            logger.error("Error while getting default YamlConfig!", e);
            return null;
        }
    }
}
