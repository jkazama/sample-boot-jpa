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
        assertTrue(Checker.len("Test String", 11));
        assertFalse(Checker.len("Test String Extra", 11));

        // surrogate pair check
        assertTrue("Test String𩸽".length() == 13);
        assertTrue(Checker.len("Test String��", 12));
    }

}
