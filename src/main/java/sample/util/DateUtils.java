package sample.util;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.Year;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQuery;
import java.util.Date;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

/**
 * Represents a frequently used date/time utility.
 */
public abstract class DateUtils {

    private static WeekendQuery WeekendQuery = new WeekendQuery();

    /** Converts a given string (YYYY-MM-DD) to LocalDate. */
    public static LocalDate day(String dayStr) {
        return dayOpt(dayStr).orElse(null);
    }

    /** Converts to LocalDate based on the specified string and format type. */
    public static LocalDate day(String dateStr, DateTimeFormatter formatter) {
        return dayOpt(dateStr, formatter).orElse(null);
    }

    /** Converts to LocalDate based on the specified string and format string. */
    public static LocalDate day(String dateStr, String format) {
        return day(dateStr, DateTimeFormatter.ofPattern(format));
    }

    /** Converts a given string (YYYYY-MM-DD) to LocalDate. */
    public static Optional<LocalDate> dayOpt(String dayStr) {
        if (StringUtils.isBlank(dayStr)) {
            return Optional.empty();
        }
        return Optional.of(LocalDate.parse(dayStr.trim(), DateTimeFormatter.ISO_LOCAL_DATE));
    }

    /** Converts to LocalDate based on the specified string and format type. */
    public static Optional<LocalDate> dayOpt(String dateStr, DateTimeFormatter formatter) {
        if (StringUtils.isBlank(dateStr)) {
            return Optional.empty();
        }
        return Optional.of(LocalDate.parse(dateStr.trim(), formatter));
    }

    /** Converts to LocalDate based on the specified string and format type. */
    public static Optional<LocalDate> dayOpt(String dateStr, String format) {
        return dayOpt(dateStr, DateTimeFormatter.ofPattern(format));
    }

    /** Converts from Date to LocalDateTime. */
    public static LocalDateTime date(Date date) {
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    /** Converts from LocalDateTime to Date. */
    public static Date date(LocalDateTime date) {
        return Date.from(date.atZone(ZoneId.systemDefault()).toInstant());
    }

    /** Converts to LocalDateTime based on the specified string and format type. */
    public static LocalDateTime date(String dateStr, DateTimeFormatter formatter) {
        return dateOpt(dateStr, formatter).orElse(null);
    }

    /**
     * Converts to LocalDateTime based on the specified string and format string.
     */
    public static LocalDateTime date(String dateStr, String format) {
        return date(dateStr, DateTimeFormatter.ofPattern(format));
    }

    /** Converts from Date to LocalDateTime. */
    public static Optional<LocalDateTime> dateOpt(Date date) {
        return date == null ? Optional.empty() : Optional.of(date(date));
    }

    /** Converts from LocalDateTime to Date. */
    public static Optional<Date> dateOpt(LocalDateTime date) {
        return date == null ? Optional.empty() : Optional.of(date(date));
    }

    /** Converts to LocalDateTime based on the specified string and format type. */
    public static Optional<LocalDateTime> dateOpt(String dateStr, DateTimeFormatter formatter) {
        if (StringUtils.isBlank(dateStr))
            return Optional.empty();
        return Optional.of(LocalDateTime.parse(dateStr.trim(), formatter));
    }

    /** Converts to LocalDateTime based on the specified string and format type. */
    public static Optional<LocalDateTime> dateOpt(String dateStr, String format) {
        return dateOpt(dateStr, DateTimeFormatter.ofPattern(format));
    }

    /** Converts the given date to LocalDateTime. */
    public static LocalDateTime dateByDay(LocalDate day) {
        return dateByDayOpt(day).orElse(null);
    }

    /** Converts the given date to LocalDateTime. */
    public static Optional<LocalDateTime> dateByDayOpt(LocalDate day) {
        return Optional.ofNullable(day).map((v) -> v.atStartOfDay());
    }

    /**
     * Returns the date and time one millisecond subtracted from the day after the
     * specified date.
     */
    public static LocalDateTime dateTo(LocalDate day) {
        return dateToOpt(day).orElse(null);
    }

    /**
     * Returns the date and time one millisecond subtracted from the day after the
     * specified date.
     */
    public static Optional<LocalDateTime> dateToOpt(LocalDate day) {
        return Optional.ofNullable(day).map((v) -> v.atTime(23, 59, 59));
    }

    /**
     * Changes to a string (YYYYY-MM-DD) based on the specified date/time type and
     * format type.
     */
    public static String dayFormat(LocalDate day) {
        return dayFormatOpt(day).orElse(null);
    }

    /**
     * Changes to a string (YYYYY-MM-DD) based on the specified date/time type and
     * format type.
     */
    public static Optional<String> dayFormatOpt(LocalDate day) {
        return Optional.ofNullable(day).map((v) -> v.format(DateTimeFormatter.ISO_LOCAL_DATE));
    }

    /**
     * Changes to a string based on the specified LocalDateTime type and format
     * type.
     */
    public static String dateFormat(LocalDateTime date, DateTimeFormatter formatter) {
        return dateFormatOpt(date, formatter).orElse(null);
    }

    /**
     * Changes to a string based on the specified LocalDateTime type and format
     * type.
     */
    public static Optional<String> dateFormatOpt(LocalDateTime date, DateTimeFormatter formatter) {
        return Optional.ofNullable(date).map((v) -> v.format(formatter));
    }

    /**
     * Changes the specified LocalDateTime type and format string to a string based
     * on the specified date/time type and format string.
     */
    public static String dateFormat(LocalDateTime date, String format) {
        return dateFormatOpt(date, format).orElse(null);
    }

    /**
     * Changes the specified LocalDateTime type and format string to a string based
     * on the specified date/time type and format string.
     */
    public static Optional<String> dateFormatOpt(LocalDateTime date, String format) {
        return Optional.ofNullable(date).map((v) -> v.format(DateTimeFormatter.ofPattern(format)));
    }

    /** Get the date interval. */
    public static Optional<Period> between(LocalDate start, LocalDate end) {
        if (start == null || end == null) {
            return Optional.empty();
        }
        return Optional.of(Period.between(start, end));
    }

    /** Get the interval between LocalDateTime. */
    public static Optional<Duration> between(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return Optional.empty();
        }
        return Optional.of(Duration.between(start, end));
    }

    /** true if targetDay <= baseDay */
    public static boolean isBeforeEquals(LocalDate baseDay, LocalDate targetDay) {
        return targetDay.isBefore(baseDay) || targetDay.isEqual(baseDay);
    }

    /** true if baseDay <= targetDay */
    public static boolean isAfterEquals(LocalDate baseDay, LocalDate targetDay) {
        return targetDay.isBefore(baseDay) || targetDay.isEqual(baseDay);
    }

    /** true if targetDate <= baseDate */
    public static boolean isBeforeEquals(LocalDateTime baseDate, LocalDateTime targetDate) {
        return targetDate.isBefore(baseDate) || targetDate.isEqual(baseDate);
    }

    /** true if baseDate <= targetDate */
    public static boolean isAfterEquals(LocalDateTime baseDate, LocalDateTime targetDate) {
        return targetDate.isBefore(baseDate) || targetDate.isEqual(baseDate);
    }

    /**
     * Determines if the specified business day is a weekend (Saturday or Sunday).
     * (Argument is required)
     */
    public static boolean isWeekend(LocalDate day) {
        Assert.notNull(day, "day is required.");
        return day.query(WeekendQuery);
    }

    /** Get the last day of the designated year. */
    public static LocalDate dayTo(int year) {
        return LocalDate.ofYearDay(year, Year.of(year).isLeap() ? 366 : 365);
    }

    /** TemporalQuery&gt;Boolean&lt; for weekend decisions. */
    public static class WeekendQuery implements TemporalQuery<Boolean> {
        @Override
        public Boolean queryFrom(TemporalAccessor temporal) {
            var dayOfWeek = DayOfWeek.of(temporal.get(ChronoField.DAY_OF_WEEK));
            return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
        }
    }

}
