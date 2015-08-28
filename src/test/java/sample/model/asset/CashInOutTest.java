package sample.model.asset;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.Test;

import sample.*;
import sample.model.account.*;
import sample.model.asset.CashInOut.*;
import sample.model.asset.type.CashflowType;
import sample.model.master.SelfFiAccount;

//low: 簡易な正常系検証が中心。依存するCashflow/CashBalanceの単体検証パスを前提。
public class CashInOutTest extends EntityTestSupport {

	private static final String ccy = "JPY";
	private static final String accId = "test";

	@Override
	protected void setupPreset() {
		targetEntities(Account.class, FiAccount.class, SelfFiAccount.class,
			CashInOut.class, Cashflow.class, CashBalance.class);
	}

	@Override
	public void before() {
		// 残高1000円の口座(test)を用意
		LocalDate baseDay = businessDay.day();
		tx(() -> {
			fixtures.selfFiAcc(Remarks.CashOut, ccy).save(rep);
			fixtures.acc(accId).save(rep);
			fixtures.fiAcc(accId, Remarks.CashOut, ccy).save(rep);
			fixtures.cb(accId, baseDay, ccy, "1000").save(rep);
		});
	}
	
	@Test
	public void find() {
		LocalDate baseDay = businessDay.day();
		LocalDate basePlus1Day = businessDay.day(1);
		LocalDate basePlus2Day = businessDay.day(2);
		tx(() -> {
			fixtures.cio(accId, "300", true).save(rep);
			//low: ちゃんとやると大変なので最低限の検証
			assertThat(
				CashInOut.find(rep, findParam(baseDay, basePlus1Day)),
				hasSize(1));
			assertThat(
				CashInOut.find(rep, findParam(baseDay, basePlus1Day, ActionStatusType.UNPROCESSED)),
				hasSize(1));
			assertThat(
				CashInOut.find(rep, findParam(baseDay, basePlus1Day, ActionStatusType.PROCESSED)),
				empty());
			assertThat(
				CashInOut.find(rep, findParam(basePlus1Day, basePlus2Day, ActionStatusType.UNPROCESSED)),
				empty());
		});
	}
	
	private FindCashInOut findParam(LocalDate fromDay, LocalDate toDay, ActionStatusType... statusTypes) {
		return new FindCashInOut(ccy, statusTypes, fromDay, toDay);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void withdrawal() {
		LocalDate baseDay = businessDay.day();
		LocalDate basePlus3Day = businessDay.day(3);
		tx(() -> {
			// 超過の出金依頼 [例外]
			try {
				CashInOut.withdraw(rep, businessDay, new RegCashOut(accId, ccy, new BigDecimal("1001")));
				fail();
			} catch (ValidationException e) {
				assertThat(e.getMessage(), is("error.CashInOut.withdrawAmount"));
			}
	
			// 0円出金の出金依頼 [例外]
			try {
				CashInOut.withdraw(rep, businessDay, new RegCashOut(accId, ccy, BigDecimal.ZERO));
				fail();
			} catch (ValidationException e) {
				assertThat(e.getMessage(), is("error.domain.AbsAmount.zero"));
			}
	
			// 通常の出金依頼
			CashInOut normal = CashInOut.withdraw(rep, businessDay, new RegCashOut(accId, ccy, new BigDecimal("300")));
			assertThat(normal, allOf(
				hasProperty("accountId", is(accId)), hasProperty("currency", is(ccy)),
				hasProperty("absAmount", is(new BigDecimal(300))), hasProperty("withdrawal", is(true)),
				hasProperty("requestDay", is(baseDay)),
				hasProperty("eventDay", is(baseDay)),
				hasProperty("valueDay", is(basePlus3Day)),
				hasProperty("targetFiCode", is(Remarks.CashOut + "-" + ccy)),
				hasProperty("targetFiAccountId", is("FI" + accId)),
				hasProperty("selfFiCode", is(Remarks.CashOut + "-" + ccy)),
				hasProperty("selfFiAccountId", is("xxxxxx")),
				hasProperty("statusType", is(ActionStatusType.UNPROCESSED)),
				hasProperty("cashflowId", is(nullValue()))));
	
			// 拘束額を考慮した出金依頼 [例外]
			try {
				CashInOut.withdraw(rep, businessDay, new RegCashOut(accId, ccy, new BigDecimal("701")));
				fail();
			} catch (ValidationException e) {
				assertThat(e.getMessage(), is("error.CashInOut.withdrawAmount"));
			}
		});
	}

	@Test
	public void cancel() {
		LocalDate baseDay = businessDay.day();
		tx(() -> {
			// CF未発生の依頼を取消
			CashInOut normal = fixtures.cio(accId, "300", true).save(rep);
			assertThat(normal.cancel(rep), hasProperty("statusType", is(ActionStatusType.CANCELLED)));
			
			// 発生日を迎えた場合は取消できない [例外]
			CashInOut today = fixtures.cio(accId, "300", true);
			today.setEventDay(baseDay);
			today.save(rep);
			try {
				today.cancel(rep);
				fail();
			} catch (ValidationException e) {
				assertThat(e.getMessage(), is("error.CashInOut.beforeEqualsDay"));
			}
		});
	}

	@Test
	public void error() {
		LocalDate baseDay = businessDay.day();
		tx(() -> {
			CashInOut normal = fixtures.cio(accId, "300", true).save(rep);
			assertThat(normal.error(rep), hasProperty("statusType", is(ActionStatusType.ERROR)));
			
			// 処理済の時はエラーにできない [例外]
			CashInOut today = fixtures.cio(accId, "300", true);
			today.setEventDay(baseDay);
			today.setStatusType(ActionStatusType.PROCESSED);
			today.save(rep);
			try {
				today.error(rep);
				fail();
			} catch (ValidationException e) {
				assertThat(e.getMessage(), is("error.ActionStatusType.unprocessing"));
			}
		});
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void process() {
		LocalDate baseDay = businessDay.day();
		LocalDate basePlus3Day = businessDay.day(3);
		tx(() -> {
			// 発生日未到来の処理 [例外]
			CashInOut future = fixtures.cio(accId, "300", true).save(rep);
			try {
				future.process(rep);
				fail();
			} catch (ValidationException e) {
				assertThat(e.getMessage(), is("error.CashInOut.afterEqualsDay"));
			}
	
			// 発生日到来処理
			CashInOut normal = fixtures.cio(accId, "300", true);
			normal.setEventDay(baseDay);
			normal.save(rep);
			assertThat(normal.process(rep), allOf(
				hasProperty("statusType", is(ActionStatusType.PROCESSED)),
				hasProperty("cashflowId", not(nullValue()))));
			// 発生させたキャッシュフローの検証
			assertThat(Cashflow.load(rep, normal.getCashflowId()), allOf(
				hasProperty("accountId", is(accId)),
				hasProperty("currency", is(ccy)),
				hasProperty("amount", is(new BigDecimal("-300"))),
				hasProperty("cashflowType", is(CashflowType.CashOut)),
				hasProperty("remark", is(Remarks.CashOut)),
				hasProperty("eventDay", is(baseDay)),
				hasProperty("valueDay", is(basePlus3Day)),
				hasProperty("statusType", is(ActionStatusType.UNPROCESSED))));
		});
	}
	
}
