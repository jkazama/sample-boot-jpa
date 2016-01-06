package sample.util;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import org.junit.Test;

public class DateUtilsTest {
    private final LocalDateTime targetDate = LocalDateTime.of(2015, 8, 29, 1, 23, 31);
    private final LocalDate targetDay = LocalDate.of(2015, 8, 29);

    @Test
    public void 初期化検証() {
        assertThat(DateUtils.day("2015-08-29"), is(targetDay));
        assertThat(
                DateUtils.date("2015-08-29T01:23:31", DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                is(targetDate));
        assertThat(
                DateUtils.dateOpt("2015-08-29T01:23:31", DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                is(Optional.of(targetDate)));
        assertThat(DateUtils.dateOpt(null, DateTimeFormatter.ISO_LOCAL_DATE_TIME), is(Optional.empty()));
        assertThat(DateUtils.date("20150829012331", "yyyyMMddHHmmss"), is(targetDate));

        assertThat(DateUtils.dateByDay(targetDay), is(LocalDateTime.of(2015, 8, 29, 0, 0, 0)));
        assertThat(DateUtils.dateTo(targetDay), is(LocalDateTime.of(2015, 8, 29, 23, 59, 59)));
    }

    @Test
    public void フォーマット検証() {
        assertThat(
                DateUtils.dateFormat(targetDate, DateTimeFormatter.ISO_LOCAL_TIME), is("01:23:31"));
        assertThat(DateUtils.dateFormat(targetDate, "MM/dd HH:mm"), is("08/29 01:23"));
    }

    @Test
    public void サポートユーティリティ検証() {
        LocalDate startDay = LocalDate.of(2015, 8, 1);
        LocalDate endDay = LocalDate.of(2015, 8, 31);
        assertTrue(DateUtils.between(startDay, endDay).isPresent());
        assertFalse(DateUtils.between(startDay, null).isPresent());
        assertFalse(DateUtils.between(null, endDay).isPresent());
        assertThat(DateUtils.between(startDay, endDay).get().getDays(), is(30)); // 31でない点に注意

        LocalDateTime startDate = LocalDateTime.of(2015, 8, 1, 01, 23, 31);
        LocalDateTime endDate = LocalDateTime.of(2015, 8, 31, 00, 23, 31);
        assertTrue(DateUtils.between(startDate, endDate).isPresent());
        assertFalse(DateUtils.between(startDate, null).isPresent());
        assertFalse(DateUtils.between(null, endDate).isPresent());
        assertThat(DateUtils.between(startDate, endDate).get().toDays(), is(29L)); // 30でない点に注意

        assertTrue(DateUtils.isWeekend(LocalDate.of(2015, 8, 29)));
        assertFalse(DateUtils.isWeekend(LocalDate.of(2015, 8, 28)));
    }

}
