package sample.model.asset;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.Test;

import sample.EntityTestSupport;
import sample.model.account.Account;

//low: Minimum test.
public class AssetTest extends EntityTestSupport {

    @Override
    protected void setupPreset() {
        targetEntities(Account.class, CashBalance.class, Cashflow.class, CashInOut.class);
    }

    @Test
    public void canWithdraw() {
        // 10000 + (1000 - 2000) - 8000 = 1000
        tx(() -> {
            fixtures.acc("test").save(rep);
            fixtures.cb("test", LocalDate.of(2014, 11, 18), "JPY", "10000").save(rep);
            fixtures.cf("test", "1000", LocalDate.of(2014, 11, 18), LocalDate.of(2014, 11, 20)).save(rep);
            fixtures.cf("test", "-2000", LocalDate.of(2014, 11, 19), LocalDate.of(2014, 11, 21)).save(rep);
            fixtures.cio("test", "8000", true).save(rep);

            assertThat(
                    Asset.of("test").canWithdraw(rep, "JPY", new BigDecimal("1000"), LocalDate.of(2014, 11, 21)),
                    is(true));
            assertThat(
                    Asset.of("test").canWithdraw(rep, "JPY", new BigDecimal("1001"), LocalDate.of(2014, 11, 21)),
                    is(false));
        });
    }

}
