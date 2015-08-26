package sample.util;

/**
 * 正規表現定数インターフェース。
 * <p>Checker.matchと組み合わせて利用してください。
 */
public interface Regex {
	/** Ascii */
	String rAscii = "^\\p{ASCII}*$";
	/** 英字 */
	String rAlpha = "^[a-zA-Z]*$";
	/** 英字大文字 */
	String rAlphaUpper = "^[A-Z]*$";
	/** 英字小文字 */
	String rAlphaLower = "^[a-z]*$";
	/** 英数 */
	String rAlnum = "^[0-9a-zA-Z]*$";
	/** シンボル */
	String rSymbol = "^\\p{Punct}*$";
	/** 英数記号 */
	String rAlnumSymbol = "^[0-9a-zA-Z\\p{Punct}]*$";
	/** 数字 */
	String rNumber = "^[-]?[0-9]*$";
	/** 整数 */
	String rNumberNatural = "^[0-9]*$";
	/** 倍精度浮動小数点 */
	String rDecimal = "^[-]?(\\d+)(\\.\\d+)?$";
	// see UnicodeBlock
	/** ひらがな */
	String rHiragana = "^\\p{InHiragana}*$";
	/** カタカナ */
	String rKatakana = "^\\p{InKatakana}*$";
	/** 半角カタカナ */
	String rHankata = "^[｡-ﾟ]*$";
	/** 半角文字列 */
	String rHankaku = "^[\\p{InBasicLatin}｡-ﾟ]*$"; // ラテン文字 + 半角カタカナ
	/** 全角文字列 */
	String rZenkaku = "^[^\\p{InBasicLatin}｡-ﾟ]*$"; // 全角の定義を半角以外で割り切り
	/** 漢字 */
	String rKanji = "^[\\p{InCJKUnifiedIdeographs}々\\p{InCJKCompatibilityIdeographs}]*$";
	/** 文字 */
	String rWord = "^(?s).*$";
	/** コード */
	String rCode = "^[0-9a-zA-Z_-]*$"; // 英数 + アンダーバー + ハイフン
}
