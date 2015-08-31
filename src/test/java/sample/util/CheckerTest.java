package sample.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class CheckerTest {

	@Test
	public void 正規表現チェック() {
		assertTrue(Checker.match(Regex.rAlnum, "19azAZ"));
		assertFalse(Checker.match(Regex.rAlnum, "19azAZ-"));
		assertTrue(Checker.match(Regex.rKanji, "漢字"));
		assertFalse(Checker.match(Regex.rAlnum, "漢字ひらがな"));
	}

	@Test
	public void 桁数チェック() {
		assertTrue(Checker.len("テスト文字列", 6));
		assertFalse(Checker.len("テスト文字列超", 6));
		
		// サロゲートペアチェック
		assertTrue("テスト文字𩸽".length() == 7);
		assertTrue(Checker.len("テスト文字𩸽", 6));
	}
	
}
