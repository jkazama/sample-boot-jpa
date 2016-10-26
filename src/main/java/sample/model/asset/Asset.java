package sample.model.asset;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Getter;
import sample.context.orm.OrmRepository;
import sample.util.Calculator;

/**
 * The asset of the account.
 */
@Getter
public class Asset {
    /** account ID */
    private final String id;

    private Asset(String id) {
        this.id = id;
    }

    public static Asset of(String accountId) {
        return new Asset(accountId);
    }

    /**
     * 振込出金可能か判定します。
     * <p>0 &lt;= 口座残高 + 未実現キャッシュフロー - (出金依頼拘束額 + 出金依頼額) 
     * low: 判定のみなのでscale指定は省略。余力金額を返す時はきちんと指定する
     */
    public boolean canWithdraw(final OrmRepository rep, String currency, BigDecimal absAmount, LocalDate valueDay) {
        Calculator calc = Calculator.of(CashBalance.getOrNew(rep, id, currency).getAmount());
        Cashflow.findUnrealize(rep, id, currency, valueDay).stream().forEach((cf) -> calc.add(cf.getAmount()));
        CashInOut.findUnprocessed(rep, id, currency, true).stream()
                .forEach((withdrawal) -> calc.add(withdrawal.getAbsAmount().negate()));
        calc.add(absAmount.negate());
        return 0 <= calc.decimal().signum();
    }
}
