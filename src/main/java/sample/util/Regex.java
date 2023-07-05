package sample.util;

/**
 * Regular expression constant interface.
 * <p>
 * Use in combination with Checker.match.
 */
public interface Regex {
    /** ascii */
    String rAscii = "^\\p{ASCII}*$";
    /** alphabetic character */
    String rAlpha = "^[a-zA-Z]*$";
    /** upper-case alphabetics */
    String rAlphaUpper = "^[A-Z]*$";
    /** lower-case alphabetics */
    String rAlphaLower = "^[a-z]*$";
    /** alphabetic/numeric character */
    String rAlnum = "^[0-9a-zA-Z]*$";
    /** symbol */
    String rSymbol = "^\\p{Punct}*$";
    /** alphabetic/numeric/symbol */
    String rAlnumSymbol = "^[0-9a-zA-Z\\p{Punct}]*$";
    /** numeric */
    String rNumber = "^[-]?[0-9]*$";
    /** numeric natural */
    String rNumberNatural = "^[0-9]*$";
    /** decimal */
    String rDecimal = "^[-]?(\\d+)(\\.\\d+)?$";
    // see UnicodeBlock
    /** hiragana */
    String rHiragana = "^\\p{InHiragana}*$";
    /** katakana */
    String rKatakana = "^\\p{InKatakana}*$";
    /** half-width katakana */
    String rHankata = "^[｡-ﾟ]*$";
    /** half-width character string */
    String rHankaku = "^[\\p{InBasicLatin}｡-ﾟ]*$";
    /** full-width character string */
    String rZenkaku = "^[^\\p{InBasicLatin}｡-ﾟ]*$";
    /** kanji */
    String rKanji = "^[\\p{InCJKUnifiedIdeographs}々\\p{InCJKCompatibilityIdeographs}]*$";
    /** string */
    String rWord = "^(?s).*$";
    /** code */
    String rCode = "^[0-9a-zA-Z_-]*$";
}
