package sample.util;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.*;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

/**
 * 頻繁に利用される日時ユーティリティを表現します。
 */
public abstract class DateUtils {

    private static WeekendQuery weekendQuery = new WeekendQuery();

    /** 指定された文字列(YYYY-MM-DD)を元に日付へ変換します。 */
    public static LocalDate day(String dayStr) {
        return dayOpt(dayStr).orElse(null);
    }

    public static Optional<LocalDate> dayOpt(String dayStr) {
        if (StringUtils.isBlank(dayStr))
            return Optional.empty();
        return Optional.of(LocalDate.parse(dayStr.trim(), DateTimeFormatter.ISO_LOCAL_DATE));
    }

    /** 指定された文字列とフォーマット型を元に日時へ変換します。 */
    public static LocalDateTime date(String dateStr, DateTimeFormatter formatter) {
        return dateOpt(dateStr, formatter).orElse(null);
    }

    public static Optional<LocalDateTime> dateOpt(String dateStr, DateTimeFormatter formatter) {
        if (StringUtils.isBlank(dateStr))
            return Optional.empty();
        return Optional.of(LocalDateTime.parse(dateStr.trim(), formatter));
    }

    /** 指定された文字列とフォーマット文字列を元に日時へ変換します。 */
    public static LocalDateTime date(String dateStr, String format) {
        return date(dateStr, DateTimeFormatter.ofPattern(format));
    }

    public static Optional<LocalDateTime> dateOpt(String dateStr, String format) {
        return dateOpt(dateStr, DateTimeFormatter.ofPattern(format));
    }

    /** 指定された日付を日時へ変換します。*/
    public static LocalDateTime dateByDay(LocalDate day) {
        return dateByDayOpt(day).orElse(null);
    }

    public static Optional<LocalDateTime> dateByDayOpt(LocalDate day) {
        return Optional.ofNullable(day).map((v) -> v.atStartOfDay());
    }

    /** 指定した日付の翌日から1msec引いた日時を返します。 */
    public static LocalDateTime dateTo(LocalDate day) {
        return dateToOpt(day).orElse(null);
    }

    public static Optional<LocalDateTime> dateToOpt(LocalDate day) {
        return Optional.ofNullable(day).map((v) -> v.atTime(23, 59, 59));
    }

    /** 指定された日時型とフォーマット型を元に文字列(YYYY-MM-DD)へ変更します。 */
    public static String dayFormat(LocalDate day) {
        return dayFormatOpt(day).orElse(null);
    }

    public static Optional<String> dayFormatOpt(LocalDate day) {
        return Optional.ofNullable(day).map((v) -> v.format(DateTimeFormatter.ISO_LOCAL_DATE));
    }

    /** 指定された日時型とフォーマット型を元に文字列へ変更します。 */
    public static String dateFormat(LocalDateTime date, DateTimeFormatter formatter) {
        return dateFormatOpt(date, formatter).orElse(null);
    }

    public static Optional<String> dateFormatOpt(LocalDateTime date, DateTimeFormatter formatter) {
        return Optional.ofNullable(date).map((v) -> v.format(formatter));
    }

    /** 指定された日時型とフォーマット文字列を元に文字列へ変更します。 */
    public static String dateFormat(LocalDateTime date, String format) {
        return dateFormatOpt(date, format).orElse(null);
    }

    public static Optional<String> dateFormatOpt(LocalDateTime date, String format) {
        return Optional.ofNullable(date).map((v) -> v.format(DateTimeFormatter.ofPattern(format)));
    }

    /** 日付の間隔を取得します。 */
    public static Optional<Period> between(LocalDate start, LocalDate end) {
        if (start == null || end == null)
            return Optional.empty();
        return Optional.of(Period.between(start, end));
    }

    /** 日時の間隔を取得します。 */
    public static Optional<Duration> between(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null)
            return Optional.empty();
        return Optional.of(Duration.between(start, end));
    }

    /** 指定営業日が週末(土日)か判定します。(引数は必須) */
    public static boolean isWeekend(LocalDate day) {
        Assert.notNull(day);
        return day.query(weekendQuery);
    }

    /** 指定年の最終日を取得します。 */
    public static LocalDate dayTo(int year) {
        return LocalDate.ofYearDay(year, Year.of(year).isLeap() ? 366 : 365);
    }

    /** 週末判定用のTemporalQuery&gt;Boolean&lt;を表現します。 */
    public static class WeekendQuery implements TemporalQuery<Boolean> {
        @Override
        public Boolean queryFrom(TemporalAccessor temporal) {
            DayOfWeek dayOfWeek = DayOfWeek.of(temporal.get(ChronoField.DAY_OF_WEEK));
            return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
        }
    }

}
