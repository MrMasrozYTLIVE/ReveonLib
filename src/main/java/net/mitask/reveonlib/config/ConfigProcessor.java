package net.mitask.reveonlib.config;

import com.google.auto.service.AutoService;
import net.mitask.reveonlib.config.annotations.AutoSave;
import net.mitask.reveonlib.config.annotations.Config;

import javax.annotation.processing.*;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.Writer;
import java.util.Set;

@AutoService(Processor.class)
@SupportedAnnotationTypes("net.mitask.reveonlib.config.annotations.Config")
@SupportedSourceVersion(javax.lang.model.SourceVersion.RELEASE_17)
@SuppressWarnings("unused")
public class ConfigProcessor extends AbstractProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(Config.class)) {
            Config config = element.getAnnotation(Config.class);
            String className = config.wrapperClassName().isEmpty() ? element.getSimpleName() + "Wrapper" : config.wrapperClassName();
            String packageName = processingEnv.getElementUtils().getPackageOf(element).toString();
            generateConfigClass((TypeElement) element, className, packageName, config);
        }
        return true;
    }

    private void generateConfigClass(TypeElement element, String className, String packageName, Config config) {
        String configFileName = config.name() + "." + config.format().name().toLowerCase();
        String path = config.path();

        try {
            JavaFileObject configFile = processingEnv.getFiler().createSourceFile(packageName + "." + className);
            String configClass = config.format() == Config.FormatType.JSON ? "JsonConfig" : "YamlConfig";

            var autoSaveAnnotation = element.getAnnotation(AutoSave.class);
            boolean autoSave = autoSaveAnnotation != null && autoSaveAnnotation.value();

            String simpleName = element.getSimpleName().toString();
            try (Writer writer = configFile.openWriter()) {
                writer.write("package " + packageName + ";\n\n");

                writer.write("import java.io.*;\n");
                writer.write("import net.mitask.reveonlib.config.impl." + configClass + ";\n");
                writer.write("import net.mitask.reveonlib.config.AbstractConfig;\n");
                writer.write("import net.mitask.reveonlib.config.ConfigListener;\n");
                writer.write("import " + packageName + "." + simpleName + ";\n\n");

                writer.write("public class " + className + " {\n");

                writer.write("    private transient AbstractConfig<" + simpleName + "> configHandler;\n\n");
                writer.write("    private transient " + simpleName + " loadedConfig;\n\n");

                writer.write("    public " + className + "() {\n");
                writer.write("        File configFile = new File(\"" + path + "\", \"" + configFileName + "\");\n");
                writer.write("        this.configHandler = new " + configClass +"<>(configFile, " + simpleName + ".class);\n");
                writer.write("        loadedConfig = configHandler.load();\n");
                writer.write("        save();\n");
                writer.write("    }\n\n");

                for (Element field : element.getEnclosedElements()) {
                    if (field.getKind().isField()) {
                        String fieldType = field.asType().toString();
                        String fieldName = field.getSimpleName().toString();

                        writer.write("    public " + fieldType + " get" + capitalize(fieldName) + "() {\n");
                        writer.write("        return loadedConfig." + fieldName + ";\n");
                        writer.write("    }\n");

                        writer.write("    public void set" + capitalize(fieldName) + "(" + fieldType + " value) {\n");
                        writer.write("        loadedConfig." + fieldName + " = value;\n");
                        if (autoSave) writer.write("        save();\n");
                        writer.write("    }\n\n");
                    }
                }

                writer.write("    public void save() {\n");
                writer.write("        configHandler.save(loadedConfig);\n");
                writer.write("    }\n\n");

                writer.write("    public void reload() {\n");
                writer.write("        loadedConfig = configHandler.reload();\n");
                writer.write("    }\n\n");

                writer.write("    public void addListener(ConfigListener<" + simpleName + "> listener) {\n");
                writer.write("        configHandler.addListener(listener);\n");
                writer.write("    }\n");

                writer.write("}\n");
            }

            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Config class was generated!");
        } catch (Exception e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Failed to generate config class: " + e.getMessage());
        }
    }

    private String capitalize(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
