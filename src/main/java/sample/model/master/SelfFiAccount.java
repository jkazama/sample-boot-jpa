package sample.model.master;

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
 * Represents the service provider's settlement financial institution.
 * low: Since this is a sample, much of the required information such as
 * branches, names, names, etc. has been omitted.
 */
@Entity
@Data
public class SelfFiAccount implements DomainEntity {
    private static final String SequenceId = "self_fi_account_id_seq";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SequenceId)
    @SequenceGenerator(name = SequenceId, sequenceName = SequenceId, allocationSize = 1)
    private Long id;
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

    public static SelfFiAccount load(final OrmRepository rep, String category, String currency) {
        var jpql = "SELECT a FROM SelfFiAccount a WHERE a.category=?1 AND a.currency=?2";
        return rep.tmpl().load(jpql, category, currency);
    }

}
