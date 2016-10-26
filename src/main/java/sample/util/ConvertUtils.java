package sample.util;

import java.math.BigDecimal;
import java.util.Optional;

import com.ibm.icu.text.Transliterator;

/** Support various object / string conversion. which depends on the ICU4J library. */
public abstract class ConvertUtils {
    private static Transliterator ZenkakuToHan = Transliterator.getInstance("Fullwidth-Halfwidth");
    private static Transliterator HankakuToZen = Transliterator.getInstance("Halfwidth-Fullwidth");
    private static Transliterator KatakanaToHira = Transliterator.getInstance("Katakana-Hiragana");
    private static Transliterator HiraganaToKana = Transliterator.getInstance("Hiragana-Katakana");

    public static Long quietlyLong(Object value) {
        try {
            return Optional.ofNullable(value).map(v -> Long.parseLong(v.toString())).orElse(null);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static Integer quietlyInt(Object value) {
        try {
            return Optional.ofNullable(value).map(v -> Integer.parseInt(v.toString())).orElse(null);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static BigDecimal quietlyDecimal(Object value) {
        try {
            return Optional.ofNullable(value).map((v) -> new BigDecimal(v.toString())).orElse(null);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static Boolean quietlyBool(Object value) {
        return Optional.ofNullable(value).map((v) -> Boolean.parseBoolean(v.toString())).orElse(false);
    }

    public static String zenkakuToHan(String text) {
        return Optional.ofNullable(text).map((v) -> ZenkakuToHan.transliterate(v)).orElse(null);
    }

    public static String hankakuToZen(String text) {
        return Optional.ofNullable(text).map((v) -> HankakuToZen.transliterate(v)).orElse(null);
    }

    public static String katakanaToHira(String text) {
        return Optional.ofNullable(text).map((v) -> KatakanaToHira.transliterate(v)).orElse(null);
    }

    public static String hiraganaToZenKana(String text) {
        return Optional.ofNullable(text).map((v) -> HiraganaToKana.transliterate(v)).orElse(null);
    }

    public static String hiraganaToHanKana(String text) {
        return zenkakuToHan(hiraganaToZenKana(text));
    }

    /** Support surrogate pair. */
    public static String substring(String text, int start, int end) {
        if (text == null)
            return null;
        int spos = text.offsetByCodePoints(0, start);
        int epos = text.length() < end ? text.length() : end;
        return text.substring(spos, text.offsetByCodePoints(spos, epos - start));
    }

    /** Support surrogate pair. */
    public static String left(String text, int len) {
        return substring(text, 0, len);
    }

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
