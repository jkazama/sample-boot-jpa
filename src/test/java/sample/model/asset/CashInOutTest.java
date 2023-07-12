package sample.model.asset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sample.context.ActionStatusType;
import sample.context.ValidationException;
import sample.model.BusinessDayHandler;
import sample.model.DataFixtures;
import sample.model.DomainErrorKeys;
import sample.model.DomainTester;
import sample.model.DomainTester.DomainTesterBuilder;
import sample.model.account.Account;
import sample.model.account.FiAccount;
import sample.model.asset.CashInOut.FindCashInOut;
import sample.model.asset.CashInOut.RegCashOut;
import sample.model.asset.type.CashflowType;
import sample.model.master.SelfFiAccount;
import sample.model.support.HolidayAccessorMock;

// low: Focus is on simple normal system verification. Assumes unit verification path for dependent Cashflow/CashBalance.
public class CashInOutTest {
    private static final String ccy = "JPY";
    private static final String accId = "test";

    private DomainTester tester;
    private BusinessDayHandler businessDay;

    @BeforeEach
    public void before() {
        tester = DomainTesterBuilder
                .from(Account.class, FiAccount.class, SelfFiAccount.class,
                        CashInOut.class, Cashflow.class, CashBalance.class)
                .build();
        businessDay = BusinessDayHandler.of(tester.time(), new HolidayAccessorMock());
        tester.txInitializeData(rep -> {
            // Prepare an account(test) with a balance of 1,000.
            LocalDate baseDay = businessDay.day();
            rep.save(DataFixtures.selfFiAcc(Remarks.CashOut, ccy));
            rep.save(DataFixtures.account(accId));
            rep.save(DataFixtures.fiAcc(accId, Remarks.CashOut, ccy));
            rep.save(DataFixtures.cb(accId, baseDay, ccy, "1000"));
        });
    }

    @AfterEach
    public void after() {
        tester.close();
    }

    @Test
    public void find() {
        LocalDate baseDay = businessDay.day();
        LocalDate basePlus1Day = businessDay.day(1);
        LocalDate basePlus2Day = businessDay.day(2);
        tester.tx(rep -> {
            rep.save(DataFixtures.cio(rep.dh().uid(), businessDay, accId, "300", true));
            // low: Minimal verification as it is very difficult to do it properly.
            assertEquals(
                    1, CashInOut.find(rep, findParam(baseDay, basePlus1Day)).size());
            assertEquals(
                    1, CashInOut.find(rep, findParam(baseDay, basePlus1Day, ActionStatusType.UNPROCESSED)).size());
            assertTrue(
                    CashInOut.find(rep, findParam(baseDay, basePlus1Day, ActionStatusType.PROCESSED)).isEmpty());
            assertTrue(
                    CashInOut.find(rep, findParam(basePlus1Day, basePlus2Day, ActionStatusType.UNPROCESSED)).isEmpty());
        });
    }

    private FindCashInOut findParam(LocalDate fromDay, LocalDate toDay, ActionStatusType... statusTypes) {
        return FindCashInOut.builder()
                .currency(ccy)
                .statusTypes(List.of(statusTypes))
                .updFromDay(fromDay)
                .updToDay(toDay)
                .build();
    }

    @Test
    public void withdraw() {
        LocalDate baseDay = businessDay.day();
        LocalDate basePlus3Day = businessDay.day(3);
        tester.tx(rep -> {
            // Excess Withdrawal Requests [Exception].
            try {
                CashInOut.withdraw(rep, businessDay, RegCashOut.builder()
                        .accountId(accId)
                        .currency(ccy)
                        .absAmount(new BigDecimal("1001"))
                        .build());
                fail();
            } catch (ValidationException e) {
                assertEquals(AssetErrorKeys.WithdrawAmount, e.getMessage());
            }

            // Withdrawal Requests for 0 Withdrawals [Exception].
            try {
                CashInOut.withdraw(rep, businessDay, RegCashOut.builder()
                        .accountId(accId)
                        .currency(ccy)
                        .absAmount(BigDecimal.ZERO)
                        .build());
                fail();
            } catch (ValidationException e) {
                assertEquals(DomainErrorKeys.AbsAmountZero, e.getMessage());
            }

            // Normal case
            var normal = CashInOut.withdraw(rep, businessDay, RegCashOut.builder()
                    .accountId(accId)
                    .currency(ccy)
                    .absAmount(new BigDecimal("300"))
                    .build());
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
            assertEquals(ActionStatusType.UNPROCESSED, normal.getStatusType());
            assertNull(normal.getCashflowId());

            // Withdrawal request taking into account the amount of the restraint
            // [Exception].
            try {
                CashInOut.withdraw(rep, businessDay, new RegCashOut(accId, ccy, new BigDecimal("701")));
                fail();
            } catch (ValidationException e) {
                assertEquals(AssetErrorKeys.WithdrawAmount, e.getMessage());
            }
        });
    }

    @Test
    public void cancel() {
        LocalDate baseDay = businessDay.day();
        tester.tx(rep -> {
            // Cancel requests that have not yet generated CF
            CashInOut normal = rep.save(DataFixtures.cio(rep.dh().uid(), businessDay, accId, "300", true));
            assertEquals(ActionStatusType.CANCELLED, normal.cancel(rep).getStatusType());

            // Cannot be canceled if the accrual date has been reached [Exception].
            CashInOut today = DataFixtures.cio(rep.dh().uid(), businessDay, accId, "300", true);
            today.setEventDay(baseDay);
            rep.save(today);
            try {
                today.cancel(rep);
                fail();
            } catch (ValidationException e) {
                assertEquals(AssetErrorKeys.AfterEqualsEventDay, e.getMessage());
            }
        });
    }

    @Test
    public void error() {
        LocalDate baseDay = businessDay.day();
        tester.tx(rep -> {
            CashInOut normal = rep.save(DataFixtures.cio(rep.dh().uid(), businessDay, accId, "300", true));
            assertEquals(ActionStatusType.ERROR, normal.error(rep).getStatusType());

            // Cannot make an error when already processed [exception].
            CashInOut today = rep.save(DataFixtures.cio(rep.dh().uid(), businessDay, accId, "300", true));
            today.setEventDay(baseDay);
            today.setStatusType(ActionStatusType.PROCESSED);
            rep.save(today);
            try {
                today.error(rep);
                fail();
            } catch (ValidationException e) {
                assertEquals(DomainErrorKeys.StatusType, e.getMessage());
            }
        });
    }

    @Test
    public void process() {
        LocalDate baseDay = businessDay.day();
        LocalDate basePlus3Day = businessDay.day(3);
        tester.tx(rep -> {
            // Processing of accrual date not yet arrived [Exception].
            CashInOut future = rep.save(DataFixtures.cio(rep.dh().uid(), businessDay, accId, "300", true));
            try {
                future.process(rep);
                fail();
            } catch (ValidationException e) {
                assertEquals(AssetErrorKeys.BeforeEventDay, e.getMessage());
            }

            // Processing of arrival of accrual date
            CashInOut normal = DataFixtures.cio(rep.dh().uid(), businessDay, accId, "300", true);
            normal.setEventDay(baseDay);
            rep.save(normal);
            CashInOut processed = normal.process(rep);
            assertEquals(ActionStatusType.PROCESSED, processed.getStatusType());
            assertNotNull(processed.getCashflowId());

            // Verification of cash flow generated
            var cf = Cashflow.load(rep, normal.getCashflowId());
            assertEquals(accId, cf.getAccountId());
            assertEquals(ccy, cf.getCurrency());
            assertEquals(new BigDecimal("-300"), cf.getAmount());
            assertEquals(CashflowType.CASH_OUT, cf.getCashflowType());
            assertEquals(Remarks.CashOut, cf.getRemark());
            assertEquals(baseDay, cf.getEventDay());
            assertEquals(basePlus3Day, cf.getValueDay());
            assertEquals(ActionStatusType.UNPROCESSED, cf.getStatusType());
        });
    }

}
