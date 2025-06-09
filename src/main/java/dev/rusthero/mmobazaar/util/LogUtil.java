package dev.rusthero.mmobazaar.util;

import java.util.logging.Logger;

public final class LogUtil {

    private LogUtil() {}

    public static void logException(Logger logger, String context, Throwable t) {
        logger.severe(context + ": " + t.getMessage());
        for (StackTraceElement element : t.getStackTrace()) {
            logger.severe("    at " + element);
        }
    }
}
