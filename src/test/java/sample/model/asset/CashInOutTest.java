package sample.model.asset;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.Test;

import sample.*;
import sample.ValidationException.ErrorKeys;
import sample.model.DomainErrorKeys;
import sample.model.account.*;
import sample.model.asset.CashInOut.*;
import sample.model.asset.type.CashflowType;
import sample.model.master.SelfFiAccount;

//low: Minimum test.
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
            assertThat(
                    CashInOut.find(rep, findParam(baseDay, basePlus1Day)),
                    hasSize(1));
            assertThat(
                    CashInOut.find(rep, findParam(baseDay, basePlus1Day, ActionStatusType.Unprocessed)),
                    hasSize(1));
            assertThat(
                    CashInOut.find(rep, findParam(baseDay, basePlus1Day, ActionStatusType.Processed)),
                    empty());
            assertThat(
                    CashInOut.find(rep, findParam(basePlus1Day, basePlus2Day, ActionStatusType.Unprocessed)),
                    empty());
        });
    }

    private FindCashInOut findParam(LocalDate fromDay, LocalDate toDay, ActionStatusType... statusTypes) {
        return new FindCashInOut(ccy, statusTypes, fromDay, toDay);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void withdraw() {
        LocalDate baseDay = businessDay.day();
        LocalDate basePlus3Day = businessDay.day(3);
        tx(() -> {
            try {
                CashInOut.withdraw(rep, businessDay, new RegCashOut(accId, ccy, new BigDecimal("1001")));
                fail();
            } catch (ValidationException e) {
                assertThat(e.getMessage(), is(AssetErrorKeys.CashInOutWithdrawAmount));
            }

            try {
                CashInOut.withdraw(rep, businessDay, new RegCashOut(accId, ccy, BigDecimal.ZERO));
                fail();
            } catch (ValidationException e) {
                assertThat(e.getMessage(), is(DomainErrorKeys.AbsAmountZero));
            }

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
                    hasProperty("statusType", is(ActionStatusType.Unprocessed)),
                    hasProperty("cashflowId", is(nullValue()))));

            try {
                CashInOut.withdraw(rep, businessDay, new RegCashOut(accId, ccy, new BigDecimal("701")));
                fail();
            } catch (ValidationException e) {
                assertThat(e.getMessage(), is(AssetErrorKeys.CashInOutWithdrawAmount));
            }
        });
    }

    @Test
    public void cancel() {
        LocalDate baseDay = businessDay.day();
        tx(() -> {
            // Cancel a request of the CF having not yet processed
            CashInOut normal = fixtures.cio(accId, "300", true).save(rep);
            assertThat(normal.cancel(rep), hasProperty("statusType", is(ActionStatusType.Cancelled)));

            // When Reach an event day, I cannot cancel it. [ValidationException]
            CashInOut today = fixtures.cio(accId, "300", true);
            today.setEventDay(baseDay);
            today.save(rep);
            try {
                today.cancel(rep);
                fail();
            } catch (ValidationException e) {
                assertThat(e.getMessage(), is(AssetErrorKeys.CashInOutBeforeEqualsDay));
            }
        });
    }

    @Test
    public void error() {
        LocalDate baseDay = businessDay.day();
        tx(() -> {
            CashInOut normal = fixtures.cio(accId, "300", true).save(rep);
            assertThat(normal.error(rep), hasProperty("statusType", is(ActionStatusType.Error)));

            // When it is processed, an error cannot do it. [ValidationException]
            CashInOut today = fixtures.cio(accId, "300", true);
            today.setEventDay(baseDay);
            today.setStatusType(ActionStatusType.Processed);
            today.save(rep);
            try {
                today.error(rep);
                fail();
            } catch (ValidationException e) {
                assertThat(e.getMessage(), is(ErrorKeys.ActionUnprocessing));
            }
        });
    }

    @Test
    @SuppressWarnings("unchecked")
    public void process() {
        LocalDate baseDay = businessDay.day();
        LocalDate basePlus3Day = businessDay.day(3);
        tx(() -> {
            // It is handled non-arrival on an event day [ValidationException]
            CashInOut future = fixtures.cio(accId, "300", true).save(rep);
            try {
                future.process(rep);
                fail();
            } catch (ValidationException e) {
                assertThat(e.getMessage(), is(AssetErrorKeys.CashInOutAfterEqualsDay));
            }

            // Event day arrival processing.
            CashInOut normal = fixtures.cio(accId, "300", true);
            normal.setEventDay(baseDay);
            normal.save(rep);
            assertThat(normal.process(rep), allOf(
                    hasProperty("statusType", is(ActionStatusType.Processed)),
                    hasProperty("cashflowId", not(nullValue()))));
            // Check the Cashflow that CashInOut produced.
            assertThat(Cashflow.load(rep, normal.getCashflowId()), allOf(
                    hasProperty("accountId", is(accId)),
                    hasProperty("currency", is(ccy)),
                    hasProperty("amount", is(new BigDecimal("-300"))),
                    hasProperty("cashflowType", is(CashflowType.CashOut)),
                    hasProperty("remark", is(Remarks.CashOut)),
                    hasProperty("eventDay", is(baseDay)),
                    hasProperty("valueDay", is(basePlus3Day)),
                    hasProperty("statusType", is(ActionStatusType.Unprocessed))));
        });
    }

}
