package sample.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

public class TimePointTest {

    @Test
    public void checkInit() {
        LocalDate targetDay = LocalDate.of(2015, 8, 28);
        LocalDateTime targetDate = LocalDateTime.of(2015, 8, 29, 1, 23, 31);

        TimePoint tp = TimePoint.of(targetDay, targetDate);
        assertEquals(targetDay, tp.day());
        assertEquals(targetDate, tp.date());

        TimePoint tpDay = TimePoint.of(targetDay);
        assertEquals(targetDay, tpDay.day());
        assertEquals(targetDay.atStartOfDay(), tpDay.date());

        TimePoint now = TimePoint.now();
        assertNotNull(now.day());
        assertNotNull(now.date());
    }

    @Test
    public void checkCompare() {
        LocalDate targetDay = LocalDate.of(2015, 8, 28);
        LocalDateTime targetDate = LocalDateTime.of(2015, 8, 29, 1, 23, 31);

        TimePoint tp = TimePoint.of(targetDay, targetDate);
        assertTrue(tp.equalsDay(LocalDate.of(2015, 8, 28)));
        assertFalse(tp.equalsDay(LocalDate.of(2015, 8, 27)));
        assertFalse(tp.equalsDay(LocalDate.of(2015, 8, 29)));

        assertTrue(tp.beforeDay(LocalDate.of(2015, 8, 29)));
        assertFalse(tp.beforeDay(LocalDate.of(2015, 8, 28)));
        assertFalse(tp.beforeDay(LocalDate.of(2015, 8, 27)));

        assertTrue(tp.afterDay(LocalDate.of(2015, 8, 27)));
        assertFalse(tp.afterDay(LocalDate.of(2015, 8, 28)));
        assertFalse(tp.afterDay(LocalDate.of(2015, 8, 29)));

        assertTrue(tp.beforeEqualsDay(LocalDate.of(2015, 8, 29)));
        assertTrue(tp.beforeEqualsDay(LocalDate.of(2015, 8, 28)));
        assertFalse(tp.beforeEqualsDay(LocalDate.of(2015, 8, 27)));

        assertTrue(tp.afterEqualsDay(LocalDate.of(2015, 8, 27)));
        assertTrue(tp.afterEqualsDay(LocalDate.of(2015, 8, 28)));
        assertFalse(tp.afterEqualsDay(LocalDate.of(2015, 8, 29)));
    }

}
