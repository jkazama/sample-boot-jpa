package sample.model.asset;

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

// low: 簡易な正常系検証が中心。依存するCashflow/CashBalanceの単体検証パスを前提。
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
    public void 振込入出金を検索する() {
        LocalDate baseDay = businessDay.day();
        LocalDate basePlus1Day = businessDay.day(1);
        LocalDate basePlus2Day = businessDay.day(2);
        tx(() -> {
            fixtures.cio(accId, "300", true).save(rep);
            // low: ちゃんとやると大変なので最低限の検証
            assertEquals(
                    1, CashInOut.find(rep, findParam(baseDay, basePlus1Day)).size());
            assertEquals(
                    1, CashInOut.find(rep, findParam(baseDay, basePlus1Day, ActionStatusType.Unprocessed)).size());
            assertTrue(
                    CashInOut.find(rep, findParam(baseDay, basePlus1Day, ActionStatusType.Processed)).isEmpty());
            assertTrue(
                    CashInOut.find(rep, findParam(basePlus1Day, basePlus2Day, ActionStatusType.Unprocessed)).isEmpty());
        });
    }

    private FindCashInOut findParam(LocalDate fromDay, LocalDate toDay, ActionStatusType... statusTypes) {
        return new FindCashInOut(ccy, statusTypes, fromDay, toDay);
    }

    @Test
    public void 振込出金依頼をする() {
        LocalDate baseDay = businessDay.day();
        LocalDate basePlus3Day = businessDay.day(3);
        tx(() -> {
            // 超過の出金依頼 [例外]
            try {
                CashInOut.withdraw(rep, businessDay, new RegCashOut(accId, ccy, new BigDecimal("1001")));
                fail();
            } catch (ValidationException e) {
                assertEquals(AssetErrorKeys.CashInOutWithdrawAmount, e.getMessage());
            }

            // 0円出金の出金依頼 [例外]
            try {
                CashInOut.withdraw(rep, businessDay, new RegCashOut(accId, ccy, BigDecimal.ZERO));
                fail();
            } catch (ValidationException e) {
                assertEquals(DomainErrorKeys.AbsAmountZero, e.getMessage());
            }

            // 通常の出金依頼
            CashInOut normal = CashInOut.withdraw(rep, businessDay, new RegCashOut(accId, ccy, new BigDecimal("300")));
            assertEquals(accId, normal.getAccountId());
            assertEquals(ccy, normal.getCurrency());
            assertEquals(new BigDecimal("300"), normal.getAbsAmount());
            assertTrue(normal.isWithdrawal());
            assertEquals(baseDay, normal.getRequestDay());
            assertEquals(baseDay, normal.getEventDay());
            assertEquals(basePlus3Day, normal.getValueDay());
            assertEquals(Remarks.CashOut + "-" + ccy, normal.getTargetFiCode());
            assertEquals("FI" + accId, normal.getTargetFiAccountId());
            assertEquals(Remarks.CashOut + "-" + ccy, normal.getSelfFiCode());
            assertEquals("xxxxxx", normal.getSelfFiAccountId());
            assertEquals(ActionStatusType.Unprocessed, normal.getStatusType());
            assertNull(normal.getCashflowId());

            // 拘束額を考慮した出金依頼 [例外]
            try {
                CashInOut.withdraw(rep, businessDay, new RegCashOut(accId, ccy, new BigDecimal("701")));
                fail();
            } catch (ValidationException e) {
                assertEquals(AssetErrorKeys.CashInOutWithdrawAmount, e.getMessage());
            }
        });
    }

    @Test
    public void 振込出金依頼を取消する() {
        LocalDate baseDay = businessDay.day();
        tx(() -> {
            // CF未発生の依頼を取消
            CashInOut normal = fixtures.cio(accId, "300", true).save(rep);
            assertEquals(ActionStatusType.Cancelled, normal.cancel(rep).getStatusType());

            // 発生日を迎えた場合は取消できない [例外]
            CashInOut today = fixtures.cio(accId, "300", true);
            today.setEventDay(baseDay);
            today.save(rep);
            try {
                today.cancel(rep);
                fail();
            } catch (ValidationException e) {
                assertEquals(AssetErrorKeys.CashInOutBeforeEqualsDay, e.getMessage());
            }
        });
    }

    @Test
    public void 振込出金依頼を例外状態とする() {
        LocalDate baseDay = businessDay.day();
        tx(() -> {
            CashInOut normal = fixtures.cio(accId, "300", true).save(rep);
            assertEquals(ActionStatusType.Error, normal.error(rep).getStatusType());

            // 処理済の時はエラーにできない [例外]
            CashInOut today = fixtures.cio(accId, "300", true);
            today.setEventDay(baseDay);
            today.setStatusType(ActionStatusType.Processed);
            today.save(rep);
            try {
                today.error(rep);
                fail();
            } catch (ValidationException e) {
                assertEquals(ErrorKeys.ActionUnprocessing, e.getMessage());
            }
        });
    }

    @Test
    public void 発生日を迎えた振込入出金をキャッシュフロー登録する() {
        LocalDate baseDay = businessDay.day();
        LocalDate basePlus3Day = businessDay.day(3);
        tx(() -> {
            // 発生日未到来の処理 [例外]
            CashInOut future = fixtures.cio(accId, "300", true).save(rep);
            try {
                future.process(rep);
                fail();
            } catch (ValidationException e) {
                assertEquals(AssetErrorKeys.CashInOutAfterEqualsDay, e.getMessage());
            }

            // 発生日到来処理
            CashInOut normal = fixtures.cio(accId, "300", true);
            normal.setEventDay(baseDay);
            normal.save(rep);
            CashInOut processed = normal.process(rep);
            assertEquals(ActionStatusType.Processed, processed.getStatusType());
            assertNotNull(processed.getCashflowId());

            // 発生させたキャッシュフローの検証
            Cashflow cf = Cashflow.load(rep, normal.getCashflowId());
            assertEquals(accId, cf.getAccountId());
            assertEquals(ccy, cf.getCurrency());
            assertEquals(new BigDecimal("-300"), cf.getAmount());
            assertEquals(CashflowType.CashOut, cf.getCashflowType());
            assertEquals(Remarks.CashOut, cf.getRemark());
            assertEquals(baseDay, cf.getEventDay());
            assertEquals(basePlus3Day, cf.getValueDay());
            assertEquals(ActionStatusType.Unprocessed, cf.getStatusType());
        });
    }

}
