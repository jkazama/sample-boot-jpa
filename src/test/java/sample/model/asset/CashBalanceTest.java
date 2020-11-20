package sample.model.asset;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import sample.EntityTestSupport;

// low: 簡易な正常系検証のみ
public class CashBalanceTest extends EntityTestSupport {

    @Override
    protected void setupPreset() {
        targetEntities(CashBalance.class);
    }

    @Test
    public void 現金残高を追加する() {
        LocalDate baseDay = businessDay.day();
        tx(() -> {
            CashBalance cb = fixtures.cb("test1", baseDay, "USD", "10.02").save(rep);

            // 10.02 + 11.51 = 21.53
            assertEquals(new BigDecimal("21.53"), cb.add(rep, new BigDecimal("11.51")).getAmount());

            // 21.53 + 11.516 = 33.04 (端数切捨確認)
            assertEquals(new BigDecimal("33.04"), cb.add(rep, new BigDecimal("11.516")).getAmount());

            // 33.04 - 41.51 = -8.47 (マイナス値/マイナス残許容)
            assertEquals(new BigDecimal("-8.47"), cb.add(rep, new BigDecimal("-41.51")).getAmount());
        });
    }

    @Test
    public void 現金残高を取得する() {
        LocalDate baseDay = businessDay.day();
        LocalDate baseMinus1Day = businessDay.day(-1);
        tx(() -> {
            fixtures.cb("test1", baseDay, "JPY", "1000").save(rep);
            fixtures.cb("test2", baseMinus1Day, "JPY", "3000").save(rep);

            // 存在している残高の検証
            CashBalance cbNormal = CashBalance.getOrNew(rep, "test1", "JPY");
            assertEquals("test1", cbNormal.getAccountId());
            assertEquals(baseDay, cbNormal.getBaseDay());
            assertEquals(new BigDecimal("1000"), cbNormal.getAmount());

            // 基準日に存在していない残高の繰越検証
            CashBalance cbRoll = CashBalance.getOrNew(rep, "test2", "JPY");
            assertEquals("test2", cbRoll.getAccountId());
            assertEquals(baseDay, cbRoll.getBaseDay());
            assertEquals(new BigDecimal("3000"), cbRoll.getAmount());

            // 残高を保有しない口座の生成検証
            CashBalance cbNew = CashBalance.getOrNew(rep, "test3", "JPY");
            assertEquals("test3", cbNew.getAccountId());
            assertEquals(baseDay, cbNew.getBaseDay());
            assertEquals(BigDecimal.ZERO, cbNew.getAmount());
        });
    }
}
