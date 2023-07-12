package sample.util;

import java.io.Serializable;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import sample.model.constraints.ISODate;
import sample.model.constraints.ISODateTime;

/**
 * Represents a LocalDate/LocalDateTime pair.
 * <p>
 * This is intended for use in cases where the business day switch does not
 * occur at 0:00.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimePoint implements Serializable {
    private static final long serialVersionUID = 1L;
    /** Date (business day) */
    @ISODate
    private LocalDate day;
    /** System date and time in date */
    @ISODateTime
    private LocalDateTime date;

    public LocalDate day() {
        return getDay();
    }

    public LocalDateTime date() {
        return getDate();
    }

    /** Is it the same as the specified date? (day == targetDay) */
    public boolean equalsDay(LocalDate targetDay) {
        return day.compareTo(targetDay) == 0;
    }

    /** Is it earlier than the specified date? (day &lt; targetDay) */
    public boolean beforeDay(LocalDate targetDay) {
        return day.compareTo(targetDay) < 0;
    }

    /** Before the specified date? (day &lt;= targetDay) */
    public boolean beforeEqualsDay(LocalDate targetDay) {
        return day.compareTo(targetDay) <= 0;
    }

    /** Is it later than the specified date? (targetDay &lt; day) */
    public boolean afterDay(LocalDate targetDay) {
        return 0 < day.compareTo(targetDay);
    }

    /** Is it after the specified date? (targetDay &lt;= day) */
    public boolean afterEqualsDay(LocalDate targetDay) {
        return 0 <= day.compareTo(targetDay);
    }

    /** Generate a TimePoint based on date/time. */
    public static TimePoint of(LocalDate day, LocalDateTime date) {
        return new TimePoint(day, date);
    }

    /** Generate a TimePoint based on a date. */
    public static TimePoint of(LocalDate day) {
        return of(day, day.atStartOfDay());
    }

    /** Generate TimePoints. */
    public static TimePoint now() {
        LocalDateTime now = LocalDateTime.now();
        return of(now.toLocalDate(), now);
    }

    /** Generate TimePoints. */
    public static TimePoint now(Clock clock) {
        LocalDateTime now = LocalDateTime.now(clock);
        return of(now.toLocalDate(), now);
    }

}
