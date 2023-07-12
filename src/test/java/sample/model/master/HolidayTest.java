package sample.model.master;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sample.model.DataFixtures;
import sample.model.DomainTester;
import sample.model.DomainTester.DomainTesterBuilder;
import sample.model.master.Holiday.FindHoliday;
import sample.model.master.Holiday.RegHoliday;
import sample.model.master.Holiday.RegHolidayItem;
import sample.util.DateUtils;

public class HolidayTest {
    private DomainTester tester;

    @BeforeEach
    public void before() {
        tester = DomainTesterBuilder.from(Holiday.class).build();
        tester.txInitializeData(rep -> {
            Stream.of("2015-09-21", "2015-09-22", "2015-09-23", "2016-09-21")
                    .map(DataFixtures::holiday)
                    .forEach(m -> rep.save(m));
        });
    }

    @AfterEach
    public void after() {
        tester.close();
    }

    @Test
    public void get() {
        tester.tx(rep -> {
            Optional<Holiday> day = Holiday.get(rep, LocalDate.of(2015, 9, 22));
            assertTrue(day.isPresent());
            assertEquals(LocalDate.of(2015, 9, 22), day.get().getDay());
        });
    }

    @Test
    public void find() {
        tester.tx(rep -> {
            assertEquals(3, Holiday.find(rep,
                    FindHoliday.builder().year(2015).build()).size());
            assertEquals(1, Holiday.find(rep,
                    FindHoliday.builder().year(2016).build()).size());
        });
    }

    @Test
    public void register() {
        List<RegHolidayItem> items = Stream.of("2016-09-21", "2016-09-22", "2016-09-23")
                .map(s -> RegHolidayItem.builder()
                        .holiday(DateUtils.day(s))
                        .name("休日")
                        .build())
                .toList();
        tester.tx(rep -> {
            Holiday.register(rep, RegHoliday
                    .builder()
                    .year(2016)
                    .list(items)
                    .build());
            assertEquals(3, Holiday.find(rep,
                    FindHoliday.builder().year(2016).build()).size());
        });
    }
}
