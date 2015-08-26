package sample.controller.system;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.math.BigDecimal;

import org.junit.*;
import org.springframework.beans.factory.annotation.Autowired;

import sample.*;
import sample.model.BusinessDayHandler;
import sample.model.asset.*;

//low: 簡易な正常系検証が中心。100万保有のsampleを前提としてしまっています。
public class JobControllerTest extends WebTestSupport {

	@Autowired
	private BusinessDayHandler businessDay;
	
	@Override
	protected String prefix() {
		return "/api/system/job";
	}

	@Test
	public void processDay() throws Exception {
		String day = businessDay.day();
		String dayPlus1 = businessDay.day(1);
		String dayPlus2 = businessDay.day(2);
		assertThat(time.day(), is(day));
		performPost("/daily/processDay");
		assertThat(time.day(), is(dayPlus1));
		performPost("/daily/processDay");
		assertThat(time.day(), is(dayPlus2));
	}

	@Test
	public void closingCashOut() throws Exception {
		// 当日発生の振込出金依頼を準備
		CashInOut co = fixtures.cio("sample", "3000", true);
		co.setEventDay(time.day());
		co.save(rep);
		assertThat(CashInOut.load(rep, co.getId()), hasProperty("statusType", is(ActionStatusType.UNPROCESSED)));
		// 実行検証
		performPost("/daily/closingCashOut");
		assertThat(CashInOut.load(rep, co.getId()), hasProperty("statusType", is(ActionStatusType.PROCESSED)));
	}

	@Test
	public void realizeCashflow() throws Exception {
		String dayMinus1 = businessDay.day(-1);
		String day = businessDay.day();
		// 当日実現のキャッシュフローを準備
		Cashflow cf = fixtures.cf("sample", "3000", dayMinus1, day).save(rep);
		assertThat(Cashflow.load(rep, cf.getId()), hasProperty("statusType", is(ActionStatusType.UNPROCESSED)));
		assertThat(CashBalance.getOrNew(rep, "sample", "JPY"),
				hasProperty("amount", is(new BigDecimal("1000000.0000"))));
		// 実行検証
		performPost("/daily/realizeCashflow");
		assertThat(Cashflow.load(rep, cf.getId()), hasProperty("statusType", is(ActionStatusType.PROCESSED)));
		assertThat(CashBalance.getOrNew(rep, "sample", "JPY"),
				hasProperty("amount", is(new BigDecimal("1003000.0000"))));
	}

}
