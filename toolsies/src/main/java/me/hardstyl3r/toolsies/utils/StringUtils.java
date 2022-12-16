package me.hardstyl3r.toolsies.utils;

import net.md_5.bungee.api.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {

    private static final Pattern UUID_PATTERN = Pattern.compile("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");
    private static final Pattern HEX_COLORS = Pattern.compile("#[a-fA-F0-9]{6}");

    /**
     * A function to determine whether a given string is a number.
     * This is pretty much inspired by Apache Commons Lang 2.6.
     *
     * @param s A string to be checked.
     * @return True if numeric, false if not.
     */
    public static boolean isNumeric(String s) {
        if (s == null) return false;
        for (int i = 0; i < s.length(); ++i)
            if (!Character.isDigit(s.charAt(i))) return false;
        return true;
    }

    /**
     * A function to determine whether a given string is a correct UUID.
     *
     * @param s A string to be checked.
     * @return True if is UUID, false if not.
     */
    public static boolean isUUID(String s) {
        if (s == null || s.isBlank()) return false;
        return UUID_PATTERN.matcher(s).matches();
    }

    /**
     * This method translates only legacy (&) and the new hex color codes.
     * Legacy: &{color}; Hex: #{color}
     */
    public static String translateBothColorCodes(String message) {
        Matcher matcher = HEX_COLORS.matcher(message);

        while (matcher.find()) {
            String color = message.substring(matcher.start(), matcher.end());
            message = message.replace(color, ChatColor.of(color) + "");
            matcher = HEX_COLORS.matcher(message);
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
