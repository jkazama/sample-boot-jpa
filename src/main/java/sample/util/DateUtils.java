package sample.util;

import java.text.ParseException;
import java.util.*;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.*;

/**
 * 日時ユーティリティを表現します。low: java8で追加されたクラスで書き換え予定
 */
public abstract class DateUtils {

	/**
	 * 指定された文字列を日時へ変換します。(文字列は8/12/14桁に対応しています)
	 */
	public static Date date(String dateStr) {
		if (StringUtils.isBlank(dateStr)) {
			return null;
		}
		int size = dateStr.length();
		if (size == 8) {
			return date("yyyyMMdd", dateStr);
		} else if (size == 12) {
			return date("yyyyMMddHHmm", dateStr);
		} else if (size == 14) {
			return date("yyyyMMddHHmmss", dateStr);
		} else {
			throw new IllegalArgumentException("サポートされない桁数の日時文字列です。[" + dateStr
					+ "]");
		}
	}

	/**
	 * 指定されたフォーマットと文字列を元に日時へ変換します。
	 */
	public static Date date(String format, String dateStr) {
		if (StringUtils.isBlank(dateStr)) {
			return null;
		}
		try {
			return FastDateFormat.getInstance(format).parse(dateStr);
		} catch (ParseException e) {
			throw new IllegalArgumentException("日時指定に誤りがあります。[format: "
					+ format + "] [dateStr: " + dateStr + "]");
		}
	}

	/** 指定した日付の翌日から1msec引いた日時を返します。 */
	public static Date dateTo(String day) {
		if (StringUtils.isBlank(day)) {
			return null;
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(date(day));
		cal.add(Calendar.DAY_OF_MONTH, 1);
		cal.add(Calendar.MILLISECOND, -1);
		return cal.getTime();
	}

	/** 指定した日付の日付を加算または減算して返します。 */
	public static Date calcDay(String dateStr, int amount) {
		if (StringUtils.isBlank(dateStr)) {
			return null;
		}
		return calcDay(date(dateStr), amount);
	}

	public static String calcDayStr(String dateStr, int amount) {
		if (StringUtils.isBlank(dateStr)) {
			return null;
		}
		return dayFormat(calcDay(date(dateStr), amount));
	}
	
	/** 指定した日付の日付を加算または減算して返します。 */
	public static Date calcDay(Date date, int amount) {
		if (date == null) {
			return null;
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.DAY_OF_MONTH, amount);
		return cal.getTime();
	}

	/** 指定した日付の月を加算または減算して返します。 */
	public static Date calcMonth(String dateStr, int amount) {
		if (StringUtils.isBlank(dateStr)) {
			return null;
		}
		return calcMonth(date(dateStr), amount);
	}

	/** 指定した日付の月を加算または減算して返します。 */
	public static Date calcMonth(Date date, int amount) {
		if (date == null) {
			return null;
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.MONTH, amount);
		return cal.getTime();
	}

	/** 指定した日付の年を加算または減算して返します。 */
	public static Date calcYear(String dateStr, int amount) {
		if (StringUtils.isBlank(dateStr)) {
			return null;
		}
		return calcYear(date(dateStr), amount);
	}

	/** 指定した日付の年を加算または減算して返します。 */
	public static Date calcYear(Date date, int amount) {
		if (date == null) {
			return null;
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.YEAR, amount);
		return cal.getTime();
	}

	/** 日時型を文字列へ変更します。 */
	public static String dateFormat(String format, Date date) {
		return DateFormatUtils.format(date, format);
	}

	/** 日時型を文字列へ変更します。 */
	public static String dayFormat(Date date) {
		return DateFormatUtils.format(date, "yyyyMMdd");
	}

	/** 日時型を文字列へ変更します。 */
	public static String monthFormat(Date date) {
		return DateFormatUtils.format(date, "yyyyMM");
	}

	/** 日付の間隔（日数）を取得します。（dateTo - dateFrom） */
	public static Long diffDay(String dateFrom, String dateTo) {
		return diffDay(date(dateFrom), date(dateTo));
	}

	public static Long diffDay(Date dateFrom, Date dateTo) {
		long timeFrom = dateFrom.getTime();
		long timeTo = dateTo.getTime();
		long diffTime = timeTo - timeFrom;
		return diffTime / 1000 / 60 / 60/ 24;
	}

	/** 基準日時に時分(HHmm)を設定して返します。 */
	public static Date replaceTime(Date baseDate, String timeHHmm) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(baseDate);
		cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeHHmm.substring(0, 2)));
		cal.set(Calendar.MINUTE, Integer.parseInt(timeHHmm.substring(2, 4)));
		cal.set(Calendar.SECOND, Integer.parseInt(timeHHmm.substring(4, 6)));
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}
	
	/** 指定営業日が週末(土日)か判定します。 */
	public static boolean isWeekday(String day) {
		return isWeekday(date(day));
	}
	
	public static boolean isWeekday(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
		return (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY);
	}
	
	/** 営業日時(from/to)に含めるか。（同値は含めます） */
	public static boolean contains(Date from, Date to, Date baseDate) {
		return (from.getTime() <= baseDate.getTime() && baseDate.getTime() <= to.getTime());
	}
	
}
