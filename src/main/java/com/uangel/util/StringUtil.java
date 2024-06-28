package com.uangel.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * String Util
 *
 * @file StringUtil.java
 * @author dajin kim
 */
public class StringUtil {
    private static final String STR_FAIL = "FAIL";
    private static final String STR_SUCCESS = "SUCCESS";
    private static final String TRUE = "TRUE";

    private StringUtil() {
        // Do Nothing
    }

    public static String getSucFail(boolean result) {
        return (result ?  STR_SUCCESS : STR_FAIL);
    }

    public static boolean checkTrue(String str) {
        return TRUE.equalsIgnoreCase(str);
    }

    public static String blankIfNull(String str) {
        return str == null ? "" : str;
    }

    public static String removeLine(String str) {
        return str.replaceAll("(\r\n|\r|\n|\n\r)", "").trim();
    }

    public static boolean isNull(String str) {
        return str == null || str.isEmpty();
    }

    public static boolean notNull(String str) {
        return str != null && !str.isEmpty();
    }

    public static boolean isNumeric(String strNum) {
        if (isNull(strNum)) return false;
        try {
            Double.parseDouble(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    public static List<String> splitToList(String str, String separator) {
        String[] arr = str.split("\\s*" + separator + "\\s*");
        return new ArrayList<>(Arrays.asList(arr));
    }

    public static String camelToSnake(String str) {
        if (isNull(str)) return "";
        StringBuilder result = new StringBuilder();
        char c = str.charAt(0);
        result.append(Character.toLowerCase(c));
        for (int i = 1; i < str.length(); i++) {

            char ch = str.charAt(i);
            if (Character.isUpperCase(ch)) {
                result.append('_');
                result.append(Character.toLowerCase(ch));
            } else {
                result.append(ch);
            }
        }
        return result.toString();
    }

    public static String snakeToCamel(String str) {
        if (isNull(str)) return "";
        str = str.substring(0, 1).toUpperCase() + str.substring(1);
        StringBuilder builder = new StringBuilder(str);

        for (int i = 0; i < builder.length(); i++) {
            if (builder.charAt(i) == '_') {
                builder.deleteCharAt(i);
                builder.replace(i, i + 1, String.valueOf(Character.toUpperCase(builder.charAt(i))));
            }
        }

        return builder.toString();
    }
}
