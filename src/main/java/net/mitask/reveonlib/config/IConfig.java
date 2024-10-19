package net.mitask.reveonlib.config;

public interface IConfig<T> {
    T load();
    T reload();
    void save(T data);
    T getDefault();
    void resetToDefault();
}
