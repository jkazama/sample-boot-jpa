package sample.util;

import java.math.BigDecimal;
import java.util.Optional;

import com.ibm.icu.text.Transliterator;

/** 各種型/文字列変換をサポートします。(ICU4Jライブラリに依存しています) */
public abstract class ConvertUtils {
    private static Transliterator zenkakuToHan = Transliterator.getInstance("Fullwidth-Halfwidth");
    private static Transliterator hankakuToZen = Transliterator.getInstance("Halfwidth-Fullwidth");
    private static Transliterator katakanaToHira = Transliterator.getInstance("Katakana-Hiragana");
    private static Transliterator hiraganaToKana = Transliterator.getInstance("Hiragana-Katakana");

    /** 例外無しにLongへ変換します。(変換できない時はnull) */
    public static Long quietlyLong(Object value) {
        try {
            return Optional.ofNullable(value).map(v -> Long.parseLong(v.toString())).orElse(null);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** 例外無しにIntegerへ変換します。(変換できない時はnull) */
    public static Integer quietlyInt(Object value) {
        try {
            return Optional.ofNullable(value).map(v -> Integer.parseInt(v.toString())).orElse(null);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** 例外無しにBigDecimalへ変換します。(変換できない時はnull) */
    public static BigDecimal quietlyDecimal(Object value) {
        try {
            return Optional.ofNullable(value).map((v) -> new BigDecimal(v.toString())).orElse(null);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** 例外無しBooleanへ変換します。(変換できない時はfalse) */
    public static Boolean quietlyBool(Object value) {
        return Optional.ofNullable(value).map((v) -> Boolean.parseBoolean(v.toString())).orElse(false);
    }

    /** 全角文字を半角にします。 */
    public static String zenkakuToHan(String text) {
        return Optional.ofNullable(text).map((v) -> zenkakuToHan.transliterate(v)).orElse(null);
    }

    /** 半角文字を全角にします。 */
    public static String hankakuToZen(String text) {
        return Optional.ofNullable(text).map((v) -> hankakuToZen.transliterate(v)).orElse(null);
    }

    /** カタカナをひらがなにします。 */
    public static String katakanaToHira(String text) {
        return Optional.ofNullable(text).map((v) -> katakanaToHira.transliterate(v)).orElse(null);
    }

    /**
     * ひらがな/半角カタカナを全角カタカナにします。
     * <p>low: 実際の挙動は厳密ではないので単体検証(ConvertUtilsTest)などで事前に確認して下さい。
     */
    public static String hiraganaToZenKana(String text) {
        return Optional.ofNullable(text).map((v) -> hiraganaToKana.transliterate(v)).orElse(null);
    }

    /**
     * ひらがな/全角カタカナを半角カタカナにします。
     * <p>low: 実際の挙動は厳密ではないので単体検証(ConvertUtilsTest)などで事前に確認して下さい。
     */
    public static String hiraganaToHanKana(String text) {
        return zenkakuToHan(hiraganaToZenKana(text));
    }

    /** 指定した文字列を抽出します。(サロゲートペア対応) */
    public static String substring(String text, int start, int end) {
        if (text == null)
            return null;
        int spos = text.offsetByCodePoints(0, start);
        int epos = text.length() < end ? text.length() : end;
        return text.substring(spos, text.offsetByCodePoints(spos, epos - start));
    }

    /** 文字列を左から指定の文字数で取得します。(サロゲートペア対応) */
    public static String left(String text, int len) {
        return substring(text, 0, len);
    }

    /** 文字列を左から指定のバイト数で取得します。 */
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
