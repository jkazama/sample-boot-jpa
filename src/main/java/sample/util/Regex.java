package sample.util;

/**
 * The regular expression constants.
 * <p>Use it in combination with Checker.match.
 */
public interface Regex {
    String rAscii = "^\\p{ASCII}*$";
    String rAlpha = "^[a-zA-Z]*$";
    String rAlphaUpper = "^[A-Z]*$";
    String rAlphaLower = "^[a-z]*$";
    String rAlnum = "^[0-9a-zA-Z]*$";
    String rSymbol = "^\\p{Punct}*$";
    String rAlnumSymbol = "^[0-9a-zA-Z\\p{Punct}]*$";
    String rNumber = "^[-]?[0-9]*$";
    String rNumberNatural = "^[0-9]*$";
    String rDecimal = "^[-]?(\\d+)(\\.\\d+)?$";
    String rHiragana = "^\\p{InHiragana}*$";
    String rKatakana = "^\\p{InKatakana}*$";
    String rHankata = "^[｡-ﾟ]*$";
    String rHankaku = "^[\\p{InBasicLatin}｡-ﾟ]*$";
    String rZenkaku = "^[^\\p{InBasicLatin}｡-ﾟ]*$";
    String rKanji = "^[\\p{InCJKUnifiedIdeographs}々\\p{InCJKCompatibilityIdeographs}]*$";
    String rWord = "^(?s).*$";
    String rCode = "^[0-9a-zA-Z_-]*$";
}
