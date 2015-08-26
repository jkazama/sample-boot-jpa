package sample.util;

import java.math.BigDecimal;

import org.apache.commons.lang3.StringUtils;

import com.ibm.icu.text.Transliterator;

/** 各種型/文字列変換をサポートします。 */
public abstract class ConvertUtils {
	private static Transliterator zenkakuToHan = Transliterator.getInstance("Fullwidth-Halfwidth");
	private static Transliterator hankakuToZen = Transliterator.getInstance("Halfwidth-Fullwidth");
	private static Transliterator katakanaToHira = Transliterator.getInstance("Katakana-Hiragana");
	private static Transliterator hiraganaToKana = Transliterator.getInstance("Hiragana-Katakana");

	/** 強制的にLongへ変換します。(変換できない時はnull) */
	public static Long parseLong(Object v) {
		if (v == null) return null;
		try {
			return Long.parseLong(v.toString());
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/** 強制的にIntegerへ変換します。(変換できない時はnull) */
	public static Integer parseInt(Object v) {
		if (v == null) return null;
		try {
			return Integer.parseInt(v.toString());
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/** 強制的にBigDecimalへ変換します。(変換できない時はnull) */
	public static BigDecimal parseDecimal(Object v) {
		if (v == null) return null;
		try {
			return new BigDecimal(v.toString());
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/** ObjectをBooleanへ変換します。(変換できない時はEmpty) */
	public static Boolean parseBool(Object v) {
		if (v == null) return null;
		return Boolean.parseBoolean(v.toString());
	}

	/** 全角文字を半角にします。 */
	public static String zenkakuToHan(String text) {
		if (text == null) return null;
		return zenkakuToHan.transliterate(text);
	}

	/** 半角文字を全角にします。 */
	public static String hankakuToZen(String text) {
		if (text == null) return null;
		return hankakuToZen.transliterate(text);
	}

	/** カタカナをひらがなにします。 */
	public static String katakanaToHira(String text) {
		if (text == null) return null;
		return katakanaToHira.transliterate(text);
	}

	/** ひらがな/半角カタカナを全角カタカナにします。 */
	public static String hiraganaToZenKana(String text) {
		if (text == null) return null;
		return hiraganaToKana.transliterate(text);
	}

	/** ひらがな/全角カタカナを半角カタカナにします。 */
	public static String hiraganaToHanKana(String text) {
		return zenkakuToHan(hiraganaToZenKana(text));
	}

	/** 文字列を左から指定の文字数で取得します。 */
	public static String left(String text, int len) {
		return StringUtils.left(text, len);
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
