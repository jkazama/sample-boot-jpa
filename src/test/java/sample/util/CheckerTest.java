package sample.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class CheckerTest {

    @Test
    public void match() {
        assertTrue(Checker.match(Regex.rAlnum, "19azAZ"));
        assertFalse(Checker.match(Regex.rAlnum, "19azAZ-"));
    }

}
