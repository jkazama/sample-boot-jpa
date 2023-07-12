package sample.model.account;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import lombok.Data;
import sample.context.DomainEntity;
import sample.context.orm.OrmRepository;
import sample.model.constraints.Category;
import sample.model.constraints.Currency;
import sample.model.constraints.IdStr;

/**
 * Represents the financial institution account associated with the account.
 * <p>
 * Used for deposits and withdrawals with an account as the counterparty.
 * low: Since this is a sample, much of the originally required information such
 * as branches, names, and names are omitted.
 */
@Entity
@Data
public class FiAccount implements DomainEntity {
    private static final String SequenceId = "fi_account_id_seq";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SequenceId)
    @SequenceGenerator(name = SequenceId, sequenceName = SequenceId, allocationSize = 1)
    private Long id;
    /** account ID */
    @IdStr
    private String accountId;
    /** Usage Categories */
    @Category
    private String category;
    @Currency
    private String currency;
    /** Financial Institution Code */
    @IdStr
    private String fiCode;
    /** Financial Institution Account No. */
    @IdStr
    private String fiAccountId;

    public static FiAccount load(final OrmRepository rep, String accountId, String category, String currency) {
        String jpql = """
                SELECT a
                FROM FiAccount a
                WHERE a.accountId=?1 AND a.category=?2 AND a.currency=?3
                """;
        return rep.tmpl().load(jpql, accountId, category, currency);
    }
}
