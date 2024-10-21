package net.mitask.reveonlib.config;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;
import net.mitask.reveonlib.config.annotations.AutoSave;
import net.mitask.reveonlib.config.annotations.Config;
import net.mitask.reveonlib.config.impl.JsonConfig;
import net.mitask.reveonlib.config.impl.YamlConfig;

import java.io.File;

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
        File configFile = new File(path, name + "." + format.name().toLowerCase());

        AutoSave autoSaveAnnotation = configClass.getAnnotation(AutoSave.class);
        boolean autoSave = autoSaveAnnotation != null && autoSaveAnnotation.value();

        AbstractConfig<T> configHandler;
        if (format == Config.FormatType.JSON) {
            configHandler = new JsonConfig<>(configFile, configClass);
        } else {
            configHandler = new YamlConfig<>(configFile, configClass);
        }

        // Automatically load configuration data
        T configInstance = configHandler.load();

        // Create a proxy to intercept field modifications for auto-saving
        try {
            return new ConfigWrapper<>(createAutoSaveProxy(configInstance, configHandler, autoSave), configHandler);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Failed to create Config!", e);
        }
    }

    public static <T> T createAutoSaveProxy(T configInstance, AbstractConfig<T> configHandler, boolean autoSave) throws InstantiationException, IllegalAccessException {
        var configClass = configInstance.getClass();
        return (T) new ByteBuddy()
                .subclass(configClass)
                .method(ElementMatchers.nameStartsWith("set"))
                .intercept(MethodDelegation.to(new Interceptor<>(configInstance, configHandler, autoSave)))
                .make()
                .load(configClass.getClassLoader())
                .getLoaded()
                .newInstance();
    }

    private record Interceptor<T>(T configInstance, AbstractConfig<T> configHandler, boolean autoSave) {
        public void intercept() throws Throwable {
            if(autoSave) configHandler.save(configInstance);
        }
    }
}

