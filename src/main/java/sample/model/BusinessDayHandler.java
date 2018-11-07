package sample.model;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import lombok.Setter;
import sample.context.Timestamper;
import sample.context.orm.DefaultRepository;
import sample.model.master.Holiday;
import sample.model.master.Holiday.RegHoliday;
import sample.util.DateUtils;

/**
 * ドメインに依存する営業日関連のユーティリティハンドラ。
 */
@Setter
public class BusinessDayHandler {

    @Autowired
    private Timestamper time;
    @Autowired
    private HolidayAccessor holidayAccessor;

    /** 営業日を返します。 */
    public LocalDate day() {
        return time.day();
    }

    /** 営業日を返します。 */
    public LocalDate day(int daysToAdd) {
        LocalDate day = day();
        if (0 < daysToAdd) {
            for (int i = 0; i < daysToAdd; i++)
                day = dayNext(day);
        } else if (daysToAdd < 0) {
            for (int i = 0; i < (-daysToAdd); i++)
                day = dayPrevious(day);
        }
        return day;
    }

    private LocalDate dayNext(LocalDate baseDay) {
        LocalDate day = baseDay.plusDays(1);
        while (isHolidayOrWeeekDay(day))
            day = day.plusDays(1);
        return day;
    }

    private LocalDate dayPrevious(LocalDate baseDay) {
        LocalDate day = baseDay.minusDays(1);
        while (isHolidayOrWeeekDay(day))
            day = day.minusDays(1);
        return day;
    }

    /** 祝日もしくは週末時はtrue。 */
    private boolean isHolidayOrWeeekDay(LocalDate day) {
        return (DateUtils.isWeekend(day) || isHoliday(day));
    }

    private boolean isHoliday(LocalDate day) {
        return holidayAccessor == null ? false : holidayAccessor.getHoliday(day).isPresent();
    }

    /** 祝日マスタを検索/登録するアクセサ。 */
    @Setter
    public static class HolidayAccessor {
        @Autowired
        private DefaultRepository rep;

        @Transactional(DefaultRepository.BeanNameTx)
        @Cacheable(cacheNames = "HolidayAccessor.getHoliday")
        public Optional<Holiday> getHoliday(LocalDate day) {
            return Holiday.get(rep, day);
        }

        @Transactional(DefaultRepository.BeanNameTx)
        @CacheEvict(cacheNames = "HolidayAccessor.getHoliday", allEntries = true)
        public void register(final DefaultRepository rep, final RegHoliday p) {
            Holiday.register(rep, p);
        }

    }

}
