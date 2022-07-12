package me.hardstyl3r.toolsies.utils;

public class StringUtils {

    /**
     * A function to determine whether a given string is a number.
     * This is pretty much inspired by Apache Commons Lang 2.6.
     * @param s A string to be checked.
     * @return True if numeric, false if not.
     */
    public static boolean isNumeric(String s) {
        if (s == null) return false;
        for (int i = 0; i < s.length(); ++i)
            if (!Character.isDigit(s.charAt(i))) return false;
        return true;
    }
}
