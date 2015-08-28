package sample.util;

import java.io.Serializable;
import java.time.*;

import javax.validation.constraints.NotNull;

import lombok.Value;

/**
 * 日付と日時のペアを表現します。
 * <p>0:00に営業日切り替えが行われないケースなどでの利用を想定しています。
 */
@Value
public class TimePoint implements Serializable {
	private static final long serialVersionUID = 1L;
	/** 日付(営業日) */
	@NotNull
	private LocalDate day;
	/** 日付におけるシステム日時 */
	@NotNull
	private LocalDateTime date;
	
	public LocalDate day() {
		return getDay();
	}
	
	public LocalDateTime date() {
		return getDate();
	}
	
	/** 指定日付と同じか。(day == targetDay) */
	public boolean equalsDay(LocalDate targetDay) {
		return day.compareTo(targetDay) == 0;
	}
	
	/** 指定日付よりも前か。(day &lt; targetDay) */
	public boolean beforeDay(LocalDate targetDay) {
		return day.compareTo(targetDay) < 0;
	}
	
	/** 指定日付以前か。(day &lt;= targetDay) */
	public boolean beforeEqualsDay(LocalDate targetDay) {
		return day.compareTo(targetDay) <= 0;
	}

	/** 指定日付よりも後か。(targetDay &lt; day) */
	public boolean afterDay(LocalDate targetDay) {
		return 0 < day.compareTo(targetDay);
	}
	
	/** 指定日付以降か。(targetDay &lt;= day) */
	public boolean afterEqualsDay(LocalDate targetDay) {
		return 0 <= day.compareTo(targetDay);
	}

	/** 日付/日時を元にTimePointを生成します。 */
	public static TimePoint of(LocalDate day, LocalDateTime date) {
		return new TimePoint(day, date);
	}
	
	/** 日付を元にTimePointを生成します。 */
	public static TimePoint of(LocalDate day) {
		return of(day, day.atStartOfDay());
	}
	
	/** TimePointを生成します。 */
	public static TimePoint now() {
		LocalDateTime now = LocalDateTime.now();
		return of(now.toLocalDate(), now);
	}
	
	/** TimePointを生成します。 */
	public static TimePoint now(Clock clock) {
		LocalDateTime now = LocalDateTime.now(clock);
		return of(now.toLocalDate(), now);
	}

}
