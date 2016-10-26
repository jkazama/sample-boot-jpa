package sample.util;

import java.io.Serializable;
import java.time.*;

import lombok.Value;
import sample.model.constraints.*;

/**
 * Pair of a date and the date and time.
 * <p>A business day uses it with the case which is not 0:00.
 */
@Value
public class TimePoint implements Serializable {
    private static final long serialVersionUID = 1L;
    @ISODate
    private LocalDate day;
    @ISODateTime
    private LocalDateTime date;

    public LocalDate day() {
        return getDay();
    }

    public LocalDateTime date() {
        return getDate();
    }

    /** day == targetDay */
    public boolean equalsDay(LocalDate targetDay) {
        return day.compareTo(targetDay) == 0;
    }

    /** day &lt; targetDay */
    public boolean beforeDay(LocalDate targetDay) {
        return day.compareTo(targetDay) < 0;
    }

    /** day &lt;= targetDay */
    public boolean beforeEqualsDay(LocalDate targetDay) {
        return day.compareTo(targetDay) <= 0;
    }

    /** targetDay &lt; day */
    public boolean afterDay(LocalDate targetDay) {
        return 0 < day.compareTo(targetDay);
    }

    /** targetDay &lt;= day */
    public boolean afterEqualsDay(LocalDate targetDay) {
        return 0 <= day.compareTo(targetDay);
    }

    public static TimePoint of(LocalDate day, LocalDateTime date) {
        return new TimePoint(day, date);
    }

    public static TimePoint of(LocalDate day) {
        return of(day, day.atStartOfDay());
    }

    public static TimePoint now() {
        LocalDateTime now = LocalDateTime.now();
        return of(now.toLocalDate(), now);
    }

    public static TimePoint now(Clock clock) {
        LocalDateTime now = LocalDateTime.now(clock);
        return of(now.toLocalDate(), now);
    }

}
