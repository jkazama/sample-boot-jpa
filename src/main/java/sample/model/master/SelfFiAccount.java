package sample.model.master;

import javax.persistence.*;

import lombok.*;
import sample.context.orm.*;
import sample.model.constraints.*;

/**
 * The settlement financial institution of the service company.
 * low: It is a sample, a branch and a name, and considerably originally omit required information.
 */
@Entity
@Data
@EqualsAndHashCode(callSuper = false)
public class SelfFiAccount extends OrmActiveRecord<SelfFiAccount> {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id;
    @Category
    private String category;
    @Currency
    private String currency;
    /** financial institution code */
    @IdStr
    private String fiCode;
    /** financial institution account ID */
    @IdStr
    private String fiAccountId;

    public static SelfFiAccount load(final OrmRepository rep, String category, String currency) {
        return rep.tmpl().load("from SelfFiAccount a where a.category=?1 and a.currency=?2", category, currency);
    }

}
