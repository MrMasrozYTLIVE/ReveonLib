package net.mitask.reveonlib.config.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.mitask.reveonlib.config.AbstractConfig;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Paths;

@SuppressWarnings("unused")
public class JsonConfig<T> extends AbstractConfig<T> {
    private final Class<T> clazz;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public JsonConfig(File file, Class<T> clazz) {
        super(file);
        this.clazz = clazz;
    }

    @Override
    public T loadConfig() {
        try (FileReader reader = new FileReader(configFile)) {
            return gson.fromJson(reader, clazz);
        } catch (IOException e) {
            logger.error("Error while loading JsonConfig!", e);
            configData = getDefault();
            save(configData);
            return configData;
        }
    }

    @Override
    public void save(T data) {
        try {
            Files.createFile(Paths.get(configFile.getCanonicalPath()));
        } catch (FileAlreadyExistsException ignored) {} catch (IOException e) {
            logger.error("Error while creating config file on saving!", e);
        }

        try (FileWriter writer = new FileWriter(configFile)) {
            gson.toJson(data, writer);
            notifyListeners(data);
        } catch (IOException e) {
            logger.error("Error while saving JsonConfig!", e);
        }
    }

    @Override
    public T getDefault() {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            logger.error("Error while getting default JsonConfig!", e);
            return null;
        }
    }
}
