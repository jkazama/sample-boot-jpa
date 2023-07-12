package sample.util;

import java.math.BigDecimal;
import java.util.Optional;

/** Supports various type/string conversions. */
public abstract class ConvertUtils {

    /** Converts to Long without exception. (null if conversion is not possible) */
    public static Long quietlyLong(Object value) {
        try {
            return Optional.ofNullable(value).map(v -> Long.parseLong(v.toString())).orElse(null);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Converts to Integer without exception. (null if conversion is not possible)
     */
    public static Integer quietlyInt(Object value) {
        try {
            return Optional.ofNullable(value).map(v -> Integer.parseInt(v.toString())).orElse(null);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Convert to BigDecimal without exception. (null if conversion is not possible)
     */
    public static BigDecimal quietlyDecimal(Object value) {
        try {
            return Optional.ofNullable(value).map((v) -> new BigDecimal(v.toString())).orElse(null);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Converts to Boolean without exception. (false if conversion is not possible)
     */
    public static Boolean quietlyBool(Object value) {
        return Optional.ofNullable(value).map((v) -> Boolean.parseBoolean(v.toString())).orElse(false);
    }

    /** Extracts the specified string. (Surrogate pairs supported) */
    public static String substring(String text, int start, int end) {
        if (text == null)
            return null;
        int spos = text.offsetByCodePoints(0, start);
        int epos = text.length() < end ? text.length() : end;
        return text.substring(spos, text.offsetByCodePoints(spos, epos - start));
    }

    /**
     * Obtains a string with a specified number of characters from the left.
     * (Surrogate pairs supported)
     */
    public static String left(String text, int len) {
        return substring(text, 0, len);
    }

    /** Obtains a string with the specified number of bytes from the left. */
    public static String leftStrict(String text, int lenByte, String charset) {
        StringBuilder sb = new StringBuilder();
        try {
            int cnt = 0;
            for (int i = 0; i < text.length(); i++) {
                String v = text.substring(i, i + 1);
                byte[] b = v.getBytes(charset);
                if (lenByte < cnt + b.length) {
                    break;
                } else {
                    sb.append(v);
                    cnt += b.length;
                }
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
        return sb.toString();
    }

}
