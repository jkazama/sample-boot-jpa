package sample.model.asset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import sample.EntityTestSupport;
import sample.context.ActionStatusType;
import sample.context.ErrorKeys;
import sample.context.ValidationException;

// low: 簡易な正常系検証が中心。依存するCashBalanceの単体検証パスを前提。
public class CashflowTest extends EntityTestSupport {

    @Override
    protected void setupPreset() {
        targetEntities(Cashflow.class, CashBalance.class);
    }

    @Test
    public void キャッシュフローを登録する() {
        LocalDate baseDay = businessDay.day();
        LocalDate baseMinus1Day = businessDay.day(-1);
        LocalDate basePlus1Day = businessDay.day(1);
        tx(() -> {
            // 過去日付の受渡でキャッシュフロー発生 [例外]
            try {
                Cashflow.register(rep, fixtures.cfReg("test1", "1000", baseMinus1Day));
                fail();
            } catch (ValidationException e) {
                assertEquals(AssetErrorKeys.CashflowBeforeEqualsDay, e.getMessage());
            }
            // 翌日受渡でキャッシュフロー発生
            Cashflow cf = Cashflow.register(rep, fixtures.cfReg("test1", "1000", basePlus1Day));
            assertEquals(new BigDecimal("1000"), cf.getAmount());
            assertEquals(ActionStatusType.UNPROCESSED, cf.getStatusType());
            assertEquals(baseDay, cf.getEventDay());
            assertEquals(basePlus1Day, cf.getValueDay());
        });
    }

    @Test
    public void 未実現キャッシュフローを実現する() {
        LocalDate baseDay = businessDay.day();
        LocalDate baseMinus1Day = businessDay.day(-1);
        LocalDate baseMinus2Day = businessDay.day(-2);
        LocalDate basePlus1Day = businessDay.day(1);
        tx(() -> {
            CashBalance.getOrNew(rep, "test1", "JPY");

            // 未到来の受渡日 [例外]
            Cashflow cfFuture = fixtures.cf("test1", "1000", baseDay, basePlus1Day).save(rep);
            try {
                cfFuture.realize(rep);
                fail();
            } catch (ValidationException e) {
                assertEquals(AssetErrorKeys.CashflowRealizeDay, e.getMessage());
            }

            // キャッシュフローの残高反映検証。 0 + 1000 = 1000
            Cashflow cfNormal = fixtures.cf("test1", "1000", baseMinus1Day, baseDay).save(rep);
            assertEquals(ActionStatusType.PROCESSED, cfNormal.realize(rep).getStatusType());
            assertEquals(new BigDecimal("1000"), CashBalance.getOrNew(rep, "test1", "JPY").getAmount());

            // 処理済キャッシュフローの再実現 [例外]
            try {
                cfNormal.realize(rep);
                fail();
            } catch (ValidationException e) {
                assertEquals(ErrorKeys.ActionUnprocessing, e.getMessage());
            }

            // 過日キャッシュフローの残高反映検証。 1000 + 2000 = 3000
            Cashflow cfPast = fixtures.cf("test1", "2000", baseMinus2Day, baseMinus1Day).save(rep);
            assertEquals(ActionStatusType.PROCESSED, cfPast.realize(rep).getStatusType());
            assertEquals(new BigDecimal("3000"), CashBalance.getOrNew(rep, "test1", "JPY").getAmount());
        });
    }

    @Test
    public void 発生即実現のキャッシュフローを登録する() {
        LocalDate baseDay = businessDay.day();
        tx(() -> {
            CashBalance.getOrNew(rep, "test1", "JPY");
            // 発生即実現
            Cashflow.register(rep, fixtures.cfReg("test1", "1000", baseDay));
            assertEquals(new BigDecimal("1000"), CashBalance.getOrNew(rep, "test1", "JPY").getAmount());
        });
    }

}
