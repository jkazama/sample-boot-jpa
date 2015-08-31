package sample.util;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.math.BigDecimal;

import org.junit.Test;

public class ConvertUtilsTest {

	@Test
	public void 例外無視変換() {
		assertThat(ConvertUtils.quietlyLong("8"), is(8L));
		assertNull(ConvertUtils.quietlyLong("a"));
		assertThat(ConvertUtils.quietlyInt("8"), is(8));
		assertNull(ConvertUtils.quietlyInt("a"));
		assertThat(ConvertUtils.quietlyDecimal("8.3"), is(new BigDecimal("8.3")));
		assertNull(ConvertUtils.quietlyDecimal("a"));
		assertTrue(ConvertUtils.quietlyBool("true"));
		assertFalse(ConvertUtils.quietlyBool("a"));
	}
	
	@Test
	public void 文字列変換() {
		assertThat(ConvertUtils.zenkakuToHan("aA19ａＡ１９あアｱ"), is("aA19aA19あｱｱ"));
		assertThat(ConvertUtils.hankakuToZen("aA19ａＡ１９あアｱ"), is("ａＡ１９ａＡ１９あアア"));
		assertThat(ConvertUtils.katakanaToHira("aA19ａＡ１９あアｱ"), is("aA19ａＡ１９あああ"));
		assertThat(ConvertUtils.hiraganaToZenKana("aA19ａＡ１９あアｱ"), is("aA19ａＡ１９アアア"));
		assertThat(ConvertUtils.hiraganaToHanKana("aA19ａＡ１９あアｱ"), is("aA19aA19ｱｱｱ"));
	}
	
}
