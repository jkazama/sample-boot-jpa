package sample.context;

import java.time.*;

import org.springframework.beans.factory.annotation.Autowired;

import lombok.Setter;
import sample.util.*;

/**
 * Date and time utility component.
 */
@Setter
public class Timestamper {
    public static final String KeyDay = "system.businessDay.day";

    @Autowired(required = false)
    private AppSettingHandler setting;

    private final Clock clock;

    public Timestamper() {
        clock = Clock.systemDefaultZone();
    }

    public Timestamper(final Clock clock) {
        this.clock = clock;
    }

    public LocalDate day() {
        return setting == null ? LocalDate.now(clock) : DateUtils.day(setting.setting(KeyDay).str());
    }

    public LocalDateTime date() {
        return LocalDateTime.now(clock);
    }

    public TimePoint tp() {
        return TimePoint.of(day(), date());
    }

    /**
     * Forward a business day to a target day.
     * <p>It is effective only at the time of setting in AppSettingHandler.
     */
    public Timestamper proceedDay(LocalDate day) {
        if (setting != null)
            setting.update(KeyDay, DateUtils.dayFormat(day));
        return this;
    }

}
