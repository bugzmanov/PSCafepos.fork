package org.pscafepos.util;

/**
 * @author bagmanov
 */
public class StringUtils {

    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }


    public static String repeat(String pattern, int repeatCount) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < repeatCount; i++) {
            builder.append(pattern);
        }
        return builder.toString();
    }
}
