package sample.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class CheckerTest {

    @Test
    public void match() {
        assertTrue(Checker.match(Regex.rAlnum, "19azAZ"));
        assertFalse(Checker.match(Regex.rAlnum, "19azAZ-"));
        assertTrue(Checker.match(Regex.rKanji, "漢字"));
        assertFalse(Checker.match(Regex.rAlnum, "漢字ひらがな"));
    }

    @Test
    public void len() {
        assertTrue(Checker.len("テスト文字列", 6));
        assertFalse(Checker.len("テスト文字列超", 6));

        // surrogate pair check
        assertTrue("テスト文字𩸽".length() == 7);
        assertTrue(Checker.len("テスト文字𩸽", 6));
    }

}
