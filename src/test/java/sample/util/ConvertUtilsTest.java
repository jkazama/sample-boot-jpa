package sample.util;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.math.BigDecimal;

import org.junit.Test;

public class ConvertUtilsTest {

    @Test
    public void quietly() {
        assertThat(ConvertUtils.quietlyLong("8"), is(8L));
        assertNull(ConvertUtils.quietlyLong("a"));
        assertThat(ConvertUtils.quietlyInt("8"), is(8));
        assertNull(ConvertUtils.quietlyInt("a"));
        assertThat(ConvertUtils.quietlyDecimal("8.3"), is(new BigDecimal("8.3")));
        assertNull(ConvertUtils.quietlyDecimal("a"));
        assertTrue(ConvertUtils.quietlyBool("true"));
        assertFalse(ConvertUtils.quietlyBool("a"));
    }

}
