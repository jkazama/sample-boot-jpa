package sample.model.asset;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.Test;

import sample.*;
import sample.ValidationException.ErrorKeys;

//low: Minimum test.
public class CashflowTest extends EntityTestSupport {

    @Override
    protected void setupPreset() {
        targetEntities(Cashflow.class, CashBalance.class);
    }

    @Test
    public void register() {
        LocalDate baseDay = businessDay.day();
        LocalDate baseMinus1Day = businessDay.day(-1);
        LocalDate basePlus1Day = businessDay.day(1);
        tx(() -> {
            // It is cashflow outbreak by the delivery of the past date. [ValidationException]
            try {
                Cashflow.register(rep, fixtures.cfReg("test1", "1000", baseMinus1Day));
                fail();
            } catch (ValidationException e) {
                assertThat(e.getMessage(), is(AssetErrorKeys.CashflowBeforeEqualsDay));
            }
            // Cashflow occurs by delivery the next day.
            assertThat(Cashflow.register(rep, fixtures.cfReg("test1", "1000", basePlus1Day)),
                    allOf(
                            hasProperty("amount", is(new BigDecimal("1000"))),
                            hasProperty("statusType", is(ActionStatusType.Unprocessed)),
                            hasProperty("eventDay", is(baseDay)),
                            hasProperty("valueDay", is(basePlus1Day))));
        });
    }

    @Test
    public void realize() {
        LocalDate baseDay = businessDay.day();
        LocalDate baseMinus1Day = businessDay.day(-1);
        LocalDate baseMinus2Day = businessDay.day(-2);
        LocalDate basePlus1Day = businessDay.day(1);
        tx(() -> {
            CashBalance.getOrNew(rep, "test1", "JPY");

            // Value day of non-arrival. [ValidationException]
            Cashflow cfFuture = fixtures.cf("test1", "1000", baseDay, basePlus1Day).save(rep);
            try {
                cfFuture.realize(rep);
                fail();
            } catch (ValidationException e) {
                assertThat(e.getMessage(), is(AssetErrorKeys.CashflowRealizeDay));
            }

            // Balance reflection inspection of the cashflow.  0 + 1000 = 1000
            Cashflow cfNormal = fixtures.cf("test1", "1000", baseMinus1Day, baseDay).save(rep);
            assertThat(cfNormal.realize(rep), hasProperty("statusType", is(ActionStatusType.Processed)));
            assertThat(CashBalance.getOrNew(rep, "test1", "JPY"),
                    hasProperty("amount", is(new BigDecimal("1000"))));

            // Re-realization of the treated cashflow. [ValidationException]
            try {
                cfNormal.realize(rep);
                fail();
            } catch (ValidationException e) {
                assertThat(e.getMessage(), is(ErrorKeys.ActionUnprocessing));
            }

            // Balance reflection inspection of the other day cashflow. 1000 + 2000 = 3000
            Cashflow cfPast = fixtures.cf("test1", "2000", baseMinus2Day, baseMinus1Day).save(rep);
            assertThat(cfPast.realize(rep), hasProperty("statusType", is(ActionStatusType.Processed)));
            assertThat(CashBalance.getOrNew(rep, "test1", "JPY"),
                    hasProperty("amount", is(new BigDecimal("3000"))));
        });
    }

    @Test
    public void registerAndRealize() {
        LocalDate baseDay = businessDay.day();
        tx(() -> {
            CashBalance.getOrNew(rep, "test1", "JPY");
            // Cashflow is realized immediately
            Cashflow.register(rep, fixtures.cfReg("test1", "1000", baseDay));
            assertThat(CashBalance.getOrNew(rep, "test1", "JPY"),
                    hasProperty("amount", is(new BigDecimal("1000"))));
        });
    }

}
