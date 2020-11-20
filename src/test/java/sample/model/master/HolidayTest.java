package sample.model.master;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

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
            assertEquals(LocalDate.of(2015, 9, 22), day.get().getDay());
        });
    }

    @Test
    public void 休日を検索する() {
        tx(() -> {
            assertEquals(3, Holiday.find(rep, 2015).size());
            assertEquals(1, Holiday.find(rep, 2016).size());
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
            assertEquals(3, Holiday.find(rep, 2016).size());
        });
    }
}
