package sample.model.asset;

import java.math.*;
import java.time.*;
import java.util.Optional;

import javax.persistence.*;

import lombok.*;
import sample.context.orm.*;
import sample.model.constraints.*;
import sample.util.*;

/**
 * The account balance.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class CashBalance extends OrmActiveRecord<CashBalance> {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id;
    @IdStr
    private String accountId;
    @ISODate
    private LocalDate baseDay;
    @Currency
    private String currency;
    @Amount
    private BigDecimal amount;
    @ISODateTime
    private LocalDateTime updateDate;

    /**
     * low Use Currency here, but the real number of the currency figures and fraction processing definition
     *  are managed with DB or the configuration file.
     */
    public CashBalance add(final OrmRepository rep, BigDecimal addAmount) {
        int scale = java.util.Currency.getInstance(currency).getDefaultFractionDigits();
        RoundingMode mode = RoundingMode.DOWN;
        setAmount(Calculator.of(amount).scale(scale, mode).add(addAmount).decimal());
        return update(rep);
    }

    /**
     * Acquire the balance of the designated account.
     * (when I do not exist, acquire it after carrying forward preservation)
     * low: The appropriate consideration and examination of plural currencies are omitted.
     */
    public static CashBalance getOrNew(final OrmRepository rep, String accountId, String currency) {
        LocalDate baseDay = rep.dh().time().day();
        Optional<CashBalance> m = rep.tmpl().get(
                "from CashBalance c where c.accountId=?1 and c.currency=?2 and c.baseDay=?3 order by c.baseDay desc",
                accountId, currency, baseDay);
        return m.orElseGet(() -> create(rep, accountId, currency));
    }

    private static CashBalance create(final OrmRepository rep, String accountId, String currency) {
        TimePoint now = rep.dh().time().tp();
        Optional<CashBalance> m = rep.tmpl().get(
                "from CashBalance c where c.accountId=?1 and c.currency=?2 order by c.baseDay desc", accountId,
                currency);
        if (m.isPresent()) { // roll over
            CashBalance prev = m.get();
            return new CashBalance(null, accountId, now.getDay(), currency, prev.getAmount(), now.getDate()).save(rep);
        } else {
            return new CashBalance(null, accountId, now.getDay(), currency, BigDecimal.ZERO, now.getDate()).save(rep);
        }
    }

}
