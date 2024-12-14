package net.mitask.reveonlib.config;

import com.google.common.io.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractConfig<T> implements IConfig<T> {
    protected final File configFile;
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final List<ConfigListener<T>> listeners = new ArrayList<>();
    protected T configData;

    public AbstractConfig(File file) {
        try {
            Files.createParentDirs(file);
        } catch (IOException e) {
            throw new RuntimeException("Could not create parent directories for config!", e);
        }
        this.configFile = file;
    }

    @SuppressWarnings("unused")
    public void addListener(ConfigListener<T> listener) {
        listeners.add(listener);
    }

    protected void notifyListeners(T newConfig) {
        for (ConfigListener<T> listener : listeners) {
            listener.onConfigChanged(newConfig);
        }
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

