package net.mitask.reveonlib.logger;

import com.diogonunes.jcolor.Attribute;

import java.util.HashMap;
import java.util.Map;

import static com.diogonunes.jcolor.Ansi.colorize;

@SuppressWarnings("unused")
public class Logger {
    private static final Map<String, Logger> loggers = new HashMap<>();
    private final String name;

    private Logger(String loggerName) {
        name = loggerName;
    }

    public void log(LogLevel level, Object message) {
        String text = colorize(String.format(" %s ", name), Attribute.CYAN_TEXT(), Attribute.BOLD(), Attribute.FRAMED()) + " ";

        switch (level) {
            case INFO:
                text += colorize(String.format("INFO: %s", message), Attribute.GREEN_TEXT(), Attribute.BOLD());
                break;
            case WARN:
                text += colorize(String.format("WARN: %s", message), Attribute.YELLOW_TEXT(), Attribute.BOLD());
                break;
            case ERROR:
                text += colorize(String.format("ERROR: %s", message), Attribute.RED_TEXT(), Attribute.BOLD());
                break;
            case DEBUG:
                text += colorize(String.format("DEBUG: %s", message), Attribute.MAGENTA_TEXT(), Attribute.BOLD());
                break;
        }

        System.out.println(text);
    }

    public void info(Object text) {
        log(LogLevel.INFO, text);
    }
    public void info(Object text, Object... args) {
        String out = text.toString();
        for (Object arg : args) out = out.replaceFirst("\\{}", "" + arg);
        info(out);
    }

    public void warn(Object text) {
        log(LogLevel.WARN, text);
    }
    public void warn(Object text, Object... args) {
        String out = text.toString();
        for (Object arg : args) out = out.replaceFirst("\\{}", "" + arg);
        warn(out);
    }

    public void error(Object text) {
        log(LogLevel.ERROR, text);
    }
    public void error(Object text, Object... args) {
        String out = text.toString();
        StringBuilder exception = new StringBuilder();
        for (Object arg : args) {
            if(arg instanceof Exception e) {
                exception.append("\n").append("- ").append(e.getMessage());
                for (StackTraceElement stackTraceElement : e.getStackTrace())
                    exception.append("\n   ").append(stackTraceElement);

                continue;
            }
            out = out.replaceFirst("\\{}", "" + arg);
        }
        error(out + exception);
    }

    public void debug(Object text) {
        log(LogLevel.DEBUG, text);
    }
    public void debug(Object text, Object... args) {
        String out = text.toString();
        for (Object arg : args) out = out.replaceFirst("\\{}", "" + arg);
        debug(out);
    }


    public static Logger getLogger(String loggerName) {
        if(loggers.containsKey(loggerName)) {
            return loggers.get(loggerName);
        }

        Logger logger = new Logger(loggerName);
        loggers.put(loggerName, logger);
        return logger;
    }

    public static Logger getLogger(Class<?> logger) {
        return getLogger(logger.getName());
    }

    public enum LogLevel {
        INFO,
        WARN,
        ERROR,
        DEBUG
    }
}
