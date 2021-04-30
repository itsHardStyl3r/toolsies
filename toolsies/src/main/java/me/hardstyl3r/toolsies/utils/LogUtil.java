package me.hardstyl3r.toolsies.utils;

import me.hardstyl3r.toolsies.Toolsies;

import java.util.logging.Level;

public class LogUtil {

    public static void info(String message) {
        Toolsies.logger.log(Level.INFO, message);
    }

    public static void warn(String message) {
        Toolsies.logger.log(Level.WARNING, message);
    }

    public static void error(String message) {
        Toolsies.logger.log(Level.SEVERE, message);
    }
}
