package sample.context;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import sample.context.spring.ObjectProviderAccessor;
import sample.context.support.AppSettingHandler;
import sample.util.DateUtils;
import sample.util.TimePoint;

/**
 * Date and time utility component.
 */
public interface Timestamper {
    public static final String KeyDay = "system.businessDay.day";

    /** Returns the current business day */
    LocalDate day();

    /** Returns the current LocalDateTime */
    LocalDateTime date();

    /** Returns the current TimePoint */
    default TimePoint tp() {
        return TimePoint.of(day(), date());
    }

    /** Proceed the business day to the target date */
    Timestamper forwardDay(LocalDate day);

    @Component
    @RequiredArgsConstructor(staticName = "of")
    public static class TimestamperImpl implements Timestamper {
        private final ObjectProvider<AppSettingHandler> settingHandler;
        private final ObjectProviderAccessor providerAccessor;
        private final Clock clock = Clock.systemDefaultZone();

        /** {@inheritDoc} */
        @Override
        public LocalDate day() {
            var setting = settingHandler().setting(KeyDay);
            if (setting.getValue() == null) {
                LocalDate day = date().toLocalDate();
                settingHandler().change(KeyDay, DateUtils.dayFormat(day));
                return day;
            } else {
                return DateUtils.day(settingHandler().setting(KeyDay).str());
            }
        }

        private AppSettingHandler settingHandler() {
            return this.providerAccessor.bean(settingHandler, AppSettingHandler.class);
        }

        /** {@inheritDoc} */
        @Override
        public LocalDateTime date() {
            return LocalDateTime.now(clock);
        }

        /** {@inheritDoc} */
        @Override
        public Timestamper forwardDay(LocalDate day) {
            this.settingHandler().change(KeyDay, DateUtils.dayFormat(day));
            return this;
        }

    }

}
