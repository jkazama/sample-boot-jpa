package sample.model.account;

import javax.persistence.*;

import lombok.*;
import sample.context.orm.*;
import sample.model.constraints.*;

/**
 * the financial institution account in an account.
 * <p>Use it by an account activity.
 * low: The minimum columns with this sample.
 */
@Entity
@Data
@EqualsAndHashCode(callSuper = false)
public class FiAccount extends OrmActiveRecord<FiAccount> {
    private static final long serialVersionUID = 1L;

    /** ID */
    @Id
    @GeneratedValue
    private Long id;
    /** 口座ID */
    @IdStr
    private String accountId;
    /** 利用用途カテゴリ */
    @Category
    private String category;
    /** 通貨 */
    @Currency
    private String currency;
    /** 金融機関コード */
    @IdStr
    private String fiCode;
    /** 金融機関口座ID */
    @IdStr
    private String fiAccountId;

    public static FiAccount load(final OrmRepository rep, String accountId, String category, String currency) {
        return rep.tmpl().load("from FiAccount a where a.accountId=?1 and a.category=?2 and a.currency=?3", accountId,
                category, currency);
    }
}
