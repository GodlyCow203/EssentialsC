package net.godlycow.org.essc.util;

import java.util.logging.Level;
import java.util.logging.Logger;

public final class EssLog {

    private static final String PREFIX   = "[EssentialsC] ";
    private static final String D_PREFIX = "[EssentialsC] [DEBUG] ";
    private static final String W_PREFIX = "[EssentialsC] [WARN] ";
    private static final String E_PREFIX = "[EssentialsC] [ERROR] ";

    private static Logger logger;
    private static boolean debug = false;

    private EssLog() {}

    public static void init(Logger pluginLogger, boolean debugEnabled) {
        logger = pluginLogger;
        debug  = debugEnabled;
    }

    public static void setDebug(boolean enabled) {
        debug = enabled;
    }

    public static boolean isDebug() {
        return debug;
    }


    public static void info(String message) {
        log(Level.INFO, PREFIX + message);
    }

    public static void info(String format, Object... args) {
        info(String.format(format, args));
    }

    public static void debug(String message) {
        if (!debug) return;
        log(Level.INFO, D_PREFIX + message);
    }

    public static void debug(String format, Object... args) {
        if (!debug) return;
        debug(String.format(format, args));
    }


    public static void warn(String message) {
        log(Level.WARNING, W_PREFIX + message);
    }

    public static void warn(String format, Object... args) {
        warn(String.format(format, args));
    }


    public static void error(String message) {
        log(Level.SEVERE, E_PREFIX + message);
    }

    public static void error(String message, Throwable throwable) {
        log(Level.SEVERE, E_PREFIX + message);
        if (logger != null) logger.log(Level.SEVERE, "", throwable);
    }

    public static void error(String format, Object... args) {
        error(String.format(format, args));
    }

    private static void log(Level level, String message) {
        if (logger == null) {
            System.out.println(message);
            return;
        }
        logger.log(level, message);
    }
}