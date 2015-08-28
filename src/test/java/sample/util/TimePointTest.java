package sample.util;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.time.*;

import org.junit.Test;

public class TimePointTest {
	
	@Test
	public void 初期化検証() {
		LocalDate targetDay = LocalDate.of(2015, 8, 28);
		LocalDateTime targetDate = LocalDateTime.of(2015, 8, 29, 1, 23, 31);
		
		TimePoint tp = TimePoint.of(targetDay, targetDate);
		assertThat(tp, allOf(
			hasProperty("day", is(targetDay)),
			hasProperty("date", is(targetDate))));
	
		TimePoint tpDay = TimePoint.of(targetDay);
		assertThat(tpDay, allOf(
			hasProperty("day", is(targetDay)),
			hasProperty("date", is(targetDay.atStartOfDay()))));
		
		TimePoint now = TimePoint.now();
		assertThat(now, allOf(
			hasProperty("day", not(nullValue())),
			hasProperty("date", not(nullValue()))));
	}

	@Test
	public void 比較検証() {
		LocalDate targetDay = LocalDate.of(2015, 8, 28);
		LocalDateTime targetDate = LocalDateTime.of(2015, 8, 29, 1, 23, 31);
		
		TimePoint tp = TimePoint.of(targetDay, targetDate);
		assertTrue(tp.equalsDay(LocalDate.of(2015, 8, 28)));
		assertFalse(tp.equalsDay(LocalDate.of(2015, 8, 27)));
		assertFalse(tp.equalsDay(LocalDate.of(2015, 8, 29)));
		
		assertTrue(tp.beforeDay(LocalDate.of(2015, 8, 29)));
		assertFalse(tp.beforeDay(LocalDate.of(2015, 8, 28)));
		assertFalse(tp.beforeDay(LocalDate.of(2015, 8, 27)));
		
		assertTrue(tp.afterDay(LocalDate.of(2015, 8, 27)));
		assertFalse(tp.afterDay(LocalDate.of(2015, 8, 28)));
		assertFalse(tp.afterDay(LocalDate.of(2015, 8, 29)));
		
		assertTrue(tp.beforeEqualsDay(LocalDate.of(2015, 8, 29)));
		assertTrue(tp.beforeEqualsDay(LocalDate.of(2015, 8, 28)));
		assertFalse(tp.beforeEqualsDay(LocalDate.of(2015, 8, 27)));
		
		assertTrue(tp.afterEqualsDay(LocalDate.of(2015, 8, 27)));
		assertTrue(tp.afterEqualsDay(LocalDate.of(2015, 8, 28)));
		assertFalse(tp.afterEqualsDay(LocalDate.of(2015, 8, 29)));
	}
	
}
