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

}
