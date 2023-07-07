package sample.model.asset;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Getter;
import sample.context.orm.OrmRepository;
import sample.util.Calculator;

/**
 * Represents the asset concept of the account.
 * Entity under asset is handled across the board.
 * low: In actual development, considerations for multi-currency and
 * in-execution/binding cash flow actions can be quite complex for some
 * services.
 */
@Getter
public class Asset {
    private final String accountId;

    private Asset(String accountId) {
        this.accountId = accountId;
    }

    /**
     * Determines if a withdrawal is possible.
     * <p>
     * 0 &lt;= account balance + unrealized cash flow - (withdrawal request bound
     * amount + withdrawal request amount)
     * low: Since this is a judgment only, the scale specification is omitted. When
     * returning the surplus amount, specify it properly.
     */
    public boolean canWithdraw(final OrmRepository rep, String currency, BigDecimal absAmount, LocalDate valueDay) {
        var calc = Calculator.of(CashBalance.getOrNew(rep, accountId, currency).getAmount());
        Cashflow.findUnrealize(rep, accountId, currency, valueDay).stream().forEach((cf) -> calc.add(cf.getAmount()));
        CashInOut.findUnprocessed(rep, accountId, currency, true)
                .forEach((withdrawal) -> calc.add(withdrawal.getAbsAmount().negate()));
        calc.add(absAmount.negate());
        return 0 <= calc.decimal().signum();
    }

    public static Asset of(String accountId) {
        return new Asset(accountId);
    }

}
