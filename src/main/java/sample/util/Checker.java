package sample.util;

/**
 * 簡易的な入力チェッカーを表現します。
 */
public abstract class Checker {

    /**
     * 正規表現に文字列がマッチするか。(nullは許容)
     * <p>引数のregexにはRegex定数を利用する事を推奨します。
     */
    public static boolean match(String regex, Object v) {
        return v != null ? v.toString().matches(regex) : true;
    }

    /** 文字桁数チェック、max以下の時はtrue。(サロゲートペア対応) */
    public static boolean len(String v, int max) {
        return wordSize(v) <= max;
    }

    private static int wordSize(String v) {
        return v.codePointCount(0, v.length());
    }
}
