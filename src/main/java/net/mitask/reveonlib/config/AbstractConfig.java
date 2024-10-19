package net.mitask.reveonlib.config;

import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractConfig<T> implements IConfig<T> {
    protected final File configFile;
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final List<ConfigListener<T>> listeners = new ArrayList<>();
    private T configData;

    @Setter
    private boolean autoSave = false;

    public AbstractConfig(File file) {
        this.configFile = file;
    }

    public void addListener(ConfigListener<T> listener) {
        listeners.add(listener);
    }

    protected void notifyListeners(T newConfig) {
        for (ConfigListener<T> listener : listeners) {
            listener.onConfigChanged(newConfig);
        }
    }

    @SuppressWarnings("unchecked")
    public <E> E get(String fieldName) {
        try {
            Field field = configData.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return (E) field.get(configData);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            logger.error("Error while getting field: {}", fieldName, e);
            return null;
        }
    }

    public <E> void set(String fieldName, E value) {
        try {
            Field field = configData.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(configData, value);

            if (autoSave) save(configData);
            else notifyListeners(configData);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            logger.error("Error while setting field: {}", fieldName, e);
        }
    }

    public <E> E getOrDefault(String fieldName, E defaultValue) {
        E value = get(fieldName);
        return value != null ? value : defaultValue;
    }

    @Override
    public T load() {
        configData = loadConfig();
//        notifyListeners(configData);
        return configData;
    }

    @Override
    public T reload() {
        configData = loadConfig();
        notifyListeners(configData);
        return configData;
    }

    protected abstract T loadConfig();

    @Override
    public void save(T data) {
        throw new UnsupportedOperationException("[AbstractConfig save] Not implemented!");
    }

    @Override
    public T getDefault() {
        throw new UnsupportedOperationException("[AbstractConfig getDefault] Not implemented!");
    }

    @Override
    public void resetToDefault() {
        save(getDefault());
    }
}

