package sample.model.asset;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import lombok.Data;
import sample.context.DomainEntity;
import sample.context.orm.OrmRepository;
import sample.model.constraints.Amount;
import sample.model.constraints.Currency;
import sample.model.constraints.ISODate;
import sample.model.constraints.ISODateTime;
import sample.model.constraints.IdStr;
import sample.util.Calculator;
import sample.util.TimePoint;

/**
 * Represents the account balance.
 */
@Entity
@Data
public class CashBalance implements DomainEntity {
    private static final String SequenceId = "cash_balance_id_seq";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SequenceId)
    @SequenceGenerator(name = SequenceId, sequenceName = SequenceId, allocationSize = 1)
    private Long id;
    /** account Id */
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
     * Reflects the specified amount in the balance.
     * low Although Currency is used here, the actual number of currency digits and
     * fractional processing definitions are managed in the DB, configuration files,
     * etc.
     */
    public CashBalance add(final OrmRepository rep, BigDecimal addAmount) {
        int scale = java.util.Currency.getInstance(currency).getDefaultFractionDigits();
        this.setAmount(Calculator.of(amount)
                .scale(scale, RoundingMode.DOWN)
                .add(addAmount)
                .decimal());
        return rep.update(this);
    }

    /**
     * Retrieves the balance of the designated account.
     * (If it does not exist, it will be retrieved after the carryover is saved.)
     * low: Proper consideration of multiple currencies and detailed screening is
     * not the main point, so I will skip it.
     */
    public static CashBalance getOrNew(final OrmRepository rep, String accountId, String currency) {
        LocalDate baseDay = rep.dh().time().day();
        var jpql = """
                SELECT c
                FROM CashBalance c
                WHERE c.accountId=?1 AND c.currency=?2 AND c.baseDay=?3
                ORDER BY c.baseDay DESC
                """;
        Optional<CashBalance> m = rep.tmpl().get(jpql, accountId, currency, baseDay);
        return m.orElseGet(() -> create(rep, accountId, currency));
    }

    private static CashBalance create(final OrmRepository rep, String accountId, String currency) {
        TimePoint now = rep.dh().time().tp();
        var jpql = """
                SELECT c
                FROM CashBalance c
                WHERE c.accountId=?1 AND c.currency=?2
                ORDER BY c.baseDay DESC
                """;
        Optional<CashBalance> current = rep.tmpl().get(jpql, accountId, currency);
        var amount = BigDecimal.ZERO;
        if (current.isPresent()) { // balance carried forward
            amount = current.get().getAmount();
        }
        var m = new CashBalance();
        m.setAccountId(accountId);
        m.setBaseDay(now.getDay());
        m.setCurrency(currency);
        m.setAmount(amount);
        m.setUpdateDate(now.getDate());
        return rep.save(m);

    }

}
