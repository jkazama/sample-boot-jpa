package sample.context;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import sample.util.*;

/**
 * 日時ユーティリティコンポーネント。
 */
@Component
public class Timestamper {
	public static final String KEY_DAY = "system.businessDay.day";

	@Autowired
	private AppSettingHandler settingHandler;
	
	/** 設定時は固定日/日時を返すモックモードとする */
	private final Optional<String> mockDay;
	private final Optional<Date> mockDate;
	
	public Timestamper() {
		mockDay = Optional.empty();
		mockDate = Optional.empty();
	}
	
	public Timestamper(final TimePoint mockDay) {
		this.mockDay = Optional.of(mockDay.getDay());
		this.mockDate = Optional.of(mockDay.getDate());
	}
	
	/**
	 * @return 営業日を返します。
	 */
	public String day() {
		return mockDay.orElseGet(() -> settingHandler.setting(KEY_DAY).str());
	}

	/**
	 * @return 日時を返します。
	 */
	public Date date() {
		return mockDate.orElse(new Date());
	}

	/**
	 * @return 営業日/日時を返します。
	 */
	public TimePoint tp() {
		return new TimePoint(day(), date());
	}

	/**
	 * 営業日を指定日へ進めます。
	 * @param day 更新営業日
	 */
	public Timestamper proceedDay(String day) {
		settingHandler.update(KEY_DAY, day);
		return this;
	}

}
