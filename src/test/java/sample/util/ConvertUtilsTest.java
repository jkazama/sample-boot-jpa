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
        assertEquals("A𠮷B", ConvertUtils.substring("A𠮷B", 0, 3));
        assertEquals("𠮷", ConvertUtils.substring("A𠮷B", 1, 2));
        assertEquals("𠮷B", ConvertUtils.substring("A𠮷B", 1, 3));
        assertEquals("B", ConvertUtils.substring("A𠮷B", 2, 3));
        assertEquals("A𠮷", ConvertUtils.left("A𠮷B", 2));
        assertEquals("A𠮷", ConvertUtils.leftStrict("A𠮷B", 6, "UTF-8"));
    }
}
