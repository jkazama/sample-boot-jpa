package sample.model.master;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import org.junit.Test;

import sample.EntityTestSupport;
import sample.model.master.Holiday.*;
import sample.util.DateUtils;

public class HolidayTest extends EntityTestSupport {

    @Override
    protected void setupPreset() {
        targetEntities(Holiday.class);
    }

    @Override
    protected void before() {
        tx(() -> {
            Arrays.asList("2015-09-21", "2015-09-22", "2015-09-23", "2016-09-21")
                    .stream()
                    .map(fixtures::holiday)
                    .forEach((m) -> m.save(rep));
        });
    }

    @Test
    public void 休日を取得する() {
        tx(() -> {
            Optional<Holiday> day = Holiday.get(rep, LocalDate.of(2015, 9, 22));
            assertTrue(day.isPresent());
            assertThat(day.get().getDay(), is(LocalDate.of(2015, 9, 22)));
        });
    }

    @Test
    public void 休日を検索する() {
        tx(() -> {
            assertThat(Holiday.find(rep, 2015), hasSize(3));
            assertThat(Holiday.find(rep, 2016), hasSize(1));
        });
    }

    @Test
    public void 休日を登録する() {
        List<RegHolidayItem> items = Arrays.asList("2016-09-21", "2016-09-22", "2016-09-23")
                .stream()
                .map((s) -> new RegHolidayItem(DateUtils.day(s), "休日"))
                .collect(Collectors.toList());
        tx(() -> {
            Holiday.register(rep, new RegHoliday(2016, items));
            assertThat(Holiday.find(rep, 2016), hasSize(3));
        });
    }
}
