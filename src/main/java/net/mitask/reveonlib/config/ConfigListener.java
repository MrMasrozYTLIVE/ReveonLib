package net.mitask.reveonlib.config;

public interface ConfigListener<T> {
    void onConfigChanged(T newConfig);
}