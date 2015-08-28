package sample.context;

import java.time.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import sample.util.*;

/**
 * 日時ユーティリティコンポーネント。
 */
@Component
public class Timestamper {
	public static final String KEY_DAY = "system.businessDay.day";

	@Autowired(required = false)
	private AppSettingHandler setting;
	
	private final Clock clock;
	
	public Timestamper() {
		clock = Clock.systemDefaultZone();
	}
	
	public Timestamper(final Clock clock) {
		this.clock = clock;
	}
	
	/** 営業日を返します。 */
	public LocalDate day() {
		return setting == null ? LocalDate.now(clock) : DateUtils.day(setting.setting(KEY_DAY).str());
	}

	/** 日時を返します。 */
	public LocalDateTime date() {
		return LocalDateTime.now(clock);
	}

	/** 営業日/日時を返します。 */
	public TimePoint tp() {
		return TimePoint.of(day(), date());
	}

	/**
	 * 営業日を指定日へ進めます。
	 * <p>AppSettingHandlerを設定時のみ有効です。
	 * @param day 更新営業日
	 */
	public Timestamper proceedDay(LocalDate day) {
		if (setting != null) setting.update(KEY_DAY, DateUtils.dayFormat(day));
		return this;
	}

}
