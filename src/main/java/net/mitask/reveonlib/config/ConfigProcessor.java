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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;

@AutoService(Processor.class)
@SupportedAnnotationTypes("net.mitask.mctr.config.annotations.Config")
@SupportedSourceVersion(javax.lang.model.SourceVersion.RELEASE_8)
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
            Files.createDirectories(Paths.get("src/main/generated/"));

            String fullClassName = "src/main/generated/" + packageName.replace('.', '/') + "/" + className + ".java";
            JavaFileObject configFile = processingEnv.getFiler().createSourceFile(fullClassName);
            String configClass = config.format() == Config.FormatType.JSON ? "JsonConfig" : "YamlConfig";

            var autoSaveAnnotation = element.getAnnotation(AutoSave.class);
            boolean autoSave = autoSaveAnnotation != null && autoSaveAnnotation.value();

            try (Writer writer = configFile.openWriter()) {
                writer.write("package " + packageName + ";\n\n");
                writer.write("import java.io.*;\n");
                writer.write("import net.mitask.mctr.config.impl." + configClass + ";\n");
                writer.write("import net.mitask.mctr.config.AbstractConfig;\n\n");

                writer.write("public class " + className + " {\n");

                writer.write("    private transient AbstractConfig<" + className + "> configHandler;\n\n");

                writer.write("    public " + className + "() {\n");
                writer.write("        File configFile = new File(\"" + path + "\", \"" + configFileName + "\");\n");
                writer.write("        this.configHandler = new " + configClass +"<>(configFile, " + className + ".class);\n");
                writer.write("        " + className + " loadedConfig = configHandler.load();\n");
                writer.write("        copyFrom(loadedConfig);\n");
                writer.write("    }\n\n");

                for (Element field : element.getEnclosedElements()) {
                    if (field.getKind().isField()) {
                        String fieldType = field.asType().toString();
                        String fieldName = field.getSimpleName().toString();

                        writer.write("    private " + fieldType + " " + fieldName + ";\n");

                        writer.write("    public " + fieldType + " get" + capitalize(fieldName) + "() {\n");
                        writer.write("        return " + fieldName + ";\n");
                        writer.write("    }\n");

                        writer.write("    public void set" + capitalize(fieldName) + "(" + fieldType + " value) {\n");
                        writer.write("        this." + fieldName + " = value;\n");
                        if (autoSave) writer.write("        save();\n");
                        writer.write("    }\n\n");
                    }
                }

                writer.write("    private void copyFrom(" + className + " loadedConfig) {\n");
                for (Element field : element.getEnclosedElements()) {
                    if (field.getKind().isField()) {
                        String fieldName = field.getSimpleName().toString();
                        writer.write("        this." + fieldName + " = loadedConfig." + fieldName + ";\n");
                    }
                }
                writer.write("    }\n\n");

                writer.write("    public void save() {\n");
                writer.write("        configHandler.save(this);\n");
                writer.write("    }\n");

                writer.write("}\n");
            }
        } catch (Exception e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Failed to generate config class: " + e.getMessage());
        }
    }

    private String capitalize(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
