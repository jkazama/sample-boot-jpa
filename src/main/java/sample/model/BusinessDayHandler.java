package sample.model;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import lombok.RequiredArgsConstructor;
import sample.context.Timestamper;
import sample.context.orm.OrmRepository;
import sample.context.orm.TxTemplate;
import sample.model.master.Holiday;
import sample.model.master.Holiday.RegHoliday;
import sample.util.DateUtils;

/**
 * Domain-dependent business day-related utility handlers.
 */
@Component
@RequiredArgsConstructor(staticName = "of")
public class BusinessDayHandler {
    private final Timestamper time;
    private final HolidayAccessor holidayAccessor;

    /** Return business days. */
    public LocalDate day() {
        return time.day();
    }

    /** Return business days. */
    public LocalDate day(int daysToAdd) {
        LocalDate day = day();
        if (0 < daysToAdd) {
            for (int i = 0; i < daysToAdd; i++) {
                day = dayNext(day);
            }
        } else if (daysToAdd < 0) {
            for (int i = 0; i < (-daysToAdd); i++) {
                day = dayPrevious(day);
            }
        }
        return day;
    }

    private LocalDate dayNext(LocalDate baseDay) {
        LocalDate day = baseDay.plusDays(1);
        while (isHolidayOrWeeekDay(day)) {
            day = day.plusDays(1);
        }
        return day;
    }

    private LocalDate dayPrevious(LocalDate baseDay) {
        LocalDate day = baseDay.minusDays(1);
        while (isHolidayOrWeeekDay(day)) {
            day = day.minusDays(1);
        }
        return day;
    }

    private boolean isHolidayOrWeeekDay(LocalDate day) {
        return (DateUtils.isWeekend(day) || isHoliday(day));
    }

    private boolean isHoliday(LocalDate day) {
        return holidayAccessor.get(day).isPresent();
    }

    /** Accessor to search/register holiday master */
    public static interface HolidayAccessor {

        Optional<Holiday> get(LocalDate holiday);

        void register(final OrmRepository rep, final RegHoliday param);
    }

    @Component
    public static class HolidayAccessorImpl implements HolidayAccessor {
        private final PlatformTransactionManager txm;
        private final OrmRepository rep;

        public HolidayAccessorImpl(PlatformTransactionManager txm, OrmRepository rep) {
            this.txm = txm;
            this.rep = rep;
        }

        @Cacheable(cacheNames = "HolidayAccessor.getHoliday")
        public Optional<Holiday> get(LocalDate day) {
            return TxTemplate.of(txm).readOnly().tx(() -> Holiday.get(rep, day));
        }

        @CacheEvict(cacheNames = "HolidayAccessor.getHoliday", allEntries = true)
        public void register(final OrmRepository rep, final RegHoliday param) {
            TxTemplate.of(txm).tx(() -> Holiday.register(rep, param));
        }

    }

}
