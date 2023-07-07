package sample.model.asset;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sample.model.BusinessDayHandler;
import sample.model.DataFixtures;
import sample.model.DomainTester;
import sample.model.DomainTester.DomainTesterBuilder;
import sample.model.support.HolidayAccessorMock;

// low: Simple normal system verification only
public class CashBalanceTest {
    private DomainTester tester;
    private BusinessDayHandler businessDay;

    @BeforeEach
    public void before() {
        tester = DomainTesterBuilder.from(CashBalance.class).build();
        businessDay = BusinessDayHandler.of(tester.time(), new HolidayAccessorMock());
    }

    @AfterEach
    public void after() {
        tester.close();
    }

    @Test
    public void add() {
        LocalDate baseDay = businessDay.day();
        tester.tx(rep -> {
            CashBalance cb = rep.save(DataFixtures.cb("test1", baseDay, "USD", "10.02"));

            // 10.02 + 11.51 = 21.53
            assertEquals(new BigDecimal("21.53"), cb.add(rep, new BigDecimal("11.51")).getAmount());

            // 21.53 + 11.516 = 33.04 (Fractional Rounding Confirmation)
            assertEquals(new BigDecimal("33.04"), cb.add(rep, new BigDecimal("11.516")).getAmount());

            // 33.04 - 41.51 = -8.47 (Negative value/negative residual allowance)
            assertEquals(new BigDecimal("-8.47"), cb.add(rep, new BigDecimal("-41.51")).getAmount());
        });
    }

    @Test
    public void getOrNew() {
        LocalDate baseDay = businessDay.day();
        LocalDate baseMinus1Day = businessDay.day(-1);
        tester.tx(rep -> {
            rep.save(DataFixtures.cb("test1", baseDay, "JPY", "1000"));
            rep.save(DataFixtures.cb("test2", baseMinus1Day, "JPY", "3000"));

            // Verification of balances in existence
            var cbNormal = CashBalance.getOrNew(rep, "test1", "JPY");
            assertEquals("test1", cbNormal.getAccountId());
            assertEquals(baseDay, cbNormal.getBaseDay());
            assertEquals(new BigDecimal("1000"), cbNormal.getAmount());

            // Verification of carryover of balances that do not exist on the base date
            var cbRoll = CashBalance.getOrNew(rep, "test2", "JPY");
            assertEquals("test2", cbRoll.getAccountId());
            assertEquals(baseDay, cbRoll.getBaseDay());
            assertEquals(new BigDecimal("3000"), cbRoll.getAmount());

            // Verification of generation of accounts that do not hold balances
            var cbNew = CashBalance.getOrNew(rep, "test3", "JPY");
            assertEquals("test3", cbNew.getAccountId());
            assertEquals(baseDay, cbNew.getBaseDay());
            assertEquals(BigDecimal.ZERO, cbNew.getAmount());
        });
    }
}
