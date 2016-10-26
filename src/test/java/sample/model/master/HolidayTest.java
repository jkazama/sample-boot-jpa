package sample.model.master;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.time.LocalDate;
import java.util.*;

import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.factory.Lists;
import org.junit.Test;

import sample.EntityTestSupport;
import sample.model.master.Holiday.*;
import sample.util.DateUtils;

// Eclipse Collections examples.
public class HolidayTest extends EntityTestSupport {

    @Override
    protected void setupPreset() {
        targetEntities(Holiday.class);
    }

    @Override
    protected void before() {
        tx(() -> {
            Lists.immutable.of("2015-09-21", "2015-09-22", "2015-09-23", "2016-09-21")
                .collect(fixtures::holiday)
                .each((m) -> m.save(rep));
        });
    }

    @Test
    public void get() {
        tx(() -> {
            Optional<Holiday> day = Holiday.get(rep, LocalDate.of(2015, 9, 22));
            assertTrue(day.isPresent());
            assertThat(day.get().getDay(), is(LocalDate.of(2015, 9, 22)));
        });
    }

    @Test
    public void find() {
        tx(() -> {
            assertThat(Holiday.find(rep, 2015), hasSize(3));
            assertThat(Holiday.find(rep, 2016), hasSize(1));
        });
    }

    @Test
    public void register() {
        MutableList<RegHolidayItem> items = Lists.mutable
                .of("2016-09-21", "2016-09-22", "2016-09-23")
                .collect((s) -> new RegHolidayItem(DateUtils.day(s), "Holiday"));
        tx(() -> {
            Holiday.register(rep, new RegHoliday(2016, items));
            assertThat(Holiday.find(rep, 2016), hasSize(3));
        });
    }
}
