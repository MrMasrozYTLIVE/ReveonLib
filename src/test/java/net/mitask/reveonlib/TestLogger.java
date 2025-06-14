package net.mitask.reveonlib;

import net.mitask.reveonlib.logger.Logger;

public class TestLogger {
    public static void main(String... args) {
        testLogger(Logger.getLogger(TestLogger.class));
        testLogger(Logger.getLogger("TestLogger"));
    }

    private static void testLogger(Logger logger) {
        for (Logger.LogLevel value : Logger.LogLevel.values()) {
            logger.log(value, "Testing Logger.log(Level, Text)");
        }
        System.out.println();

        logger.info("Testing Logger.info(Text)");
        logger.warn("Testing Logger.warn(Text)");
        logger.error("Testing Logger.error(Text)");
        logger.debug("Testing Logger.debug(Text)");
        System.out.println();

        logger.info("Testing Logger.info({}, {})", "Text", "Args");
        logger.warn("Testing Logger.warn({}, {})", "Text", "Args");
        logger.error("Testing Logger.error({}, {})", "Text", "Args");
        logger.debug("Testing Logger.debug({}, {})", "Text", "Args");
        System.out.println();

        logger.error("Testing Logger.error(Text, Exception)", new RuntimeException("This is a test exception"));
        System.out.println();

        logger.error("Testing Logger.error({}, {}) with Exception passed at start of the args", new RuntimeException("This is a test exception"), "Text", "Args");
        System.out.println();

        logger.error("Testing Logger.error({}, {}) with Exceptions passed at random parts of the args", new RuntimeException("This is a test exception 1"), "Text", new RuntimeException("This is a test exception 2"), "Args");
    }
}
