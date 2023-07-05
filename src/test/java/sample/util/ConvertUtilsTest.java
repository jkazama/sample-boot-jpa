package sample.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

public class ConvertUtilsTest {

    @Test
    public void checkQuietly() {
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
    public void checkSubstring() {
        assertEquals("あ𠮷い", ConvertUtils.substring("あ𠮷い", 0, 3));
        assertEquals("𠮷", ConvertUtils.substring("あ𠮷い", 1, 2));
        assertEquals("𠮷い", ConvertUtils.substring("あ𠮷い", 1, 3));
        assertEquals("い", ConvertUtils.substring("あ𠮷い", 2, 3));
        assertEquals("あ𠮷", ConvertUtils.left("あ𠮷い", 2));
        assertEquals("あ𠮷", ConvertUtils.leftStrict("あ𠮷い", 6, "UTF-8"));
    }
}
