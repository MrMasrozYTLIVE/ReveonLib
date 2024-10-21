package net.mitask.reveonlib.config;

import net.mitask.reveonlib.config.annotations.AutoSave;
import net.mitask.reveonlib.config.annotations.Config;
import net.mitask.reveonlib.config.impl.JsonConfig;
import net.mitask.reveonlib.config.impl.YamlConfig;

import java.io.File;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

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
        return new ConfigWrapper<>(createAutoSaveProxy(configInstance, configHandler, autoSave), configHandler);
    }

    @SuppressWarnings("unchecked")
    public static <T> T createAutoSaveProxy(T configInstance, AbstractConfig<T> configHandler, boolean autoSave) {
        return (T) Proxy.newProxyInstance(
                configInstance.getClass().getClassLoader(),
                configInstance.getClass().getInterfaces(),
                new AutoSaveHandler<>(configInstance, configHandler, autoSave)
        );
    }

    private record AutoSaveHandler<T>(T originalConfig, AbstractConfig<T> configHandler, boolean autoSave) implements InvocationHandler {
        @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                Object result = method.invoke(originalConfig, args);

                // If it's a setter method, trigger a save
                if(autoSave && method.getName().startsWith("set")) configHandler.save(originalConfig);

                return result;
            }
        }
}

