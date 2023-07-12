package sample.util;

/**
 * Represents a simple input checker.
 */
public abstract class Checker {

    /**
     * Does the string match the regular expression. (null is acceptable)
     * <p>
     * It is recommended to use the Regex constant for the regex argument.
     */
    public static boolean match(String regex, Object v) {
        return v != null ? v.toString().matches(regex) : true;
    }

    /** Character digit check, true if max or less (surrogate pair support) */
    public static boolean len(String v, int max) {
        return wordSize(v) <= max;
    }

    private static int wordSize(String v) {
        return v.codePointCount(0, v.length());
    }
}
