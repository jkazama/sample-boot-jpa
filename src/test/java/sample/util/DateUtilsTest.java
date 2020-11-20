package sample.util;

import static org.junit.jupiter.api.Assertions.*;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import org.junit.jupiter.api.Test;

public class DateUtilsTest {
    private final LocalDateTime targetDate = LocalDateTime.of(2015, 8, 29, 1, 23, 31);
    private final LocalDate targetDay = LocalDate.of(2015, 8, 29);

    @Test
    public void 初期化検証() {
        assertEquals(targetDay, DateUtils.day("2015-08-29"));
        assertEquals(
                targetDate,
                DateUtils.date("2015-08-29T01:23:31", DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        assertEquals(
                Optional.of(targetDate),
                DateUtils.dateOpt("2015-08-29T01:23:31", DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        assertEquals(Optional.empty(), DateUtils.dateOpt(null, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        assertEquals(targetDate, DateUtils.date("20150829012331", "yyyyMMddHHmmss"));

        assertEquals(LocalDateTime.of(2015, 8, 29, 0, 0, 0), DateUtils.dateByDay(targetDay));
        assertEquals(LocalDateTime.of(2015, 8, 29, 23, 59, 59), DateUtils.dateTo(targetDay));
    }

    @Test
    public void フォーマット検証() {
        assertEquals(
                "01:23:31",
                DateUtils.dateFormat(targetDate, DateTimeFormatter.ISO_LOCAL_TIME));
        assertEquals("08/29 01:23", DateUtils.dateFormat(targetDate, "MM/dd HH:mm"));
    }

    @Test
    public void サポートユーティリティ検証() {
        LocalDate startDay = LocalDate.of(2015, 8, 1);
        LocalDate endDay = LocalDate.of(2015, 8, 31);
        assertTrue(DateUtils.between(startDay, endDay).isPresent());
        assertFalse(DateUtils.between(startDay, null).isPresent());
        assertFalse(DateUtils.between(null, endDay).isPresent());
        assertEquals(30, DateUtils.between(startDay, endDay).get().getDays()); // 31でない点に注意

        LocalDateTime startDate = LocalDateTime.of(2015, 8, 1, 01, 23, 31);
        LocalDateTime endDate = LocalDateTime.of(2015, 8, 31, 00, 23, 31);
        assertTrue(DateUtils.between(startDate, endDate).isPresent());
        assertFalse(DateUtils.between(startDate, null).isPresent());
        assertFalse(DateUtils.between(null, endDate).isPresent());
        assertEquals(29, DateUtils.between(startDate, endDate).get().toDays()); // 30でない点に注意

        assertTrue(DateUtils.isWeekend(LocalDate.of(2015, 8, 29)));
        assertFalse(DateUtils.isWeekend(LocalDate.of(2015, 8, 28)));
    }

}
