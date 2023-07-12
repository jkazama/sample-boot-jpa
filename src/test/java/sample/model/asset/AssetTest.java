package sample.model.asset;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sample.model.BusinessDayHandler;
import sample.model.DataFixtures;
import sample.model.DomainTester;
import sample.model.DomainTester.DomainTesterBuilder;
import sample.model.account.Account;
import sample.model.support.HolidayAccessorMock;

// low: Focus on simple verification
public class AssetTest {
    private DomainTester tester;
    private BusinessDayHandler businessDay;

    @BeforeEach
    public void before() {
        tester = DomainTesterBuilder.from(
                Account.class, CashBalance.class, Cashflow.class, CashInOut.class).build();
        businessDay = BusinessDayHandler.of(tester.time(), new HolidayAccessorMock());
    }

    @AfterEach
    public void after() {
        tester.close();
    }

    @Test
    public void canWithdraw() {
        // 10000 + (1000 - 2000) - 8000 = 1000
        tester.tx(rep -> {
            rep.save(DataFixtures.account("test"));
            rep.save(DataFixtures
                    .cb("test", LocalDate.of(2014, 11, 18), "JPY", "10000"));
            rep.save(DataFixtures
                    .cf("test", "1000", LocalDate.of(2014, 11, 18), LocalDate.of(2014, 11, 20)));
            rep.save(DataFixtures
                    .cf("test", "-2000", LocalDate.of(2014, 11, 19), LocalDate.of(2014, 11, 21)));
            rep.save(DataFixtures.cio(rep.dh().uid(), businessDay, "test", "8000", true));

            assertTrue(Asset.of("test")
                    .canWithdraw(rep, "JPY", new BigDecimal("1000"), LocalDate.of(2014, 11, 21)));
            assertFalse(Asset.of("test")
                    .canWithdraw(rep, "JPY", new BigDecimal("1001"), LocalDate.of(2014, 11, 21)));
        });
    }

}
