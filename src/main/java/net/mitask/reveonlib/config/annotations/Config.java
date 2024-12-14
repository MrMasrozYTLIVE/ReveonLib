package net.mitask.reveonlib.config.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Config {
    String name();
    String path() default "./config/";
    String wrapperClassName() default "";
    FormatType format() default FormatType.JSON;

    enum FormatType {
        JSON,
        @Deprecated
        YAML
    }
}