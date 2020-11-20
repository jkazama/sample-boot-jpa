package sample.util;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

public class ConvertUtilsTest {

    @Test
    public void 例外無視変換() {
        assertEquals(Long.valueOf(8), ConvertUtils.quietlyLong("8"));
        assertNull(ConvertUtils.quietlyLong("a"));
        assertEquals(Integer.valueOf(8), ConvertUtils.quietlyInt("8"));
        assertNull(ConvertUtils.quietlyInt("a"));
        assertEquals(new BigDecimal("8.3"), ConvertUtils.quietlyDecimal("8.3"));
        assertNull(ConvertUtils.quietlyDecimal("a"));
        assertTrue(ConvertUtils.quietlyBool("true"));
        assertFalse(ConvertUtils.quietlyBool("a"));
    }

    @Test
    public void 文字列変換() {
        assertEquals("aA19aA19あｱｱ", ConvertUtils.zenkakuToHan("aA19ａＡ１９あアｱ"));
        assertEquals("ａＡ１９ａＡ１９あアア", ConvertUtils.hankakuToZen("aA19ａＡ１９あアｱ"));
        assertEquals("aA19ａＡ１９あああ", ConvertUtils.katakanaToHira("aA19ａＡ１９あアｱ"));
        assertEquals("aA19ａＡ１９アアア", ConvertUtils.hiraganaToZenKana("aA19ａＡ１９あアｱ"));
        assertEquals("aA19aA19ｱｱｱ", ConvertUtils.hiraganaToHanKana("aA19ａＡ１９あアｱ"));
    }

    @Test
    public void 桁数操作及びサロゲートペア対応() {
        assertEquals("あ𠮷い", ConvertUtils.substring("あ𠮷い", 0, 3));
        assertEquals("𠮷", ConvertUtils.substring("あ𠮷い", 1, 2));
        assertEquals("𠮷い", ConvertUtils.substring("あ𠮷い", 1, 3));
        assertEquals("い", ConvertUtils.substring("あ𠮷い", 2, 3));
        assertEquals("あ𠮷", ConvertUtils.left("あ𠮷い", 2));
        assertEquals("あ𠮷", ConvertUtils.leftStrict("あ𠮷い", 6, "UTF-8"));
    }
}
