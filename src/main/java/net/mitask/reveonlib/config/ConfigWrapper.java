package net.mitask.reveonlib.config;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class ConfigWrapper<T> {
    @Getter
    private T config;
    private AbstractConfig<T> configHandler;

    @SuppressWarnings("unused")
    public void save() {
        configHandler.save(config);
    }

    @SuppressWarnings("unused")
    public void addListener(ConfigListener<T> listener) {
        configHandler.addListener(listener);
    }
}
