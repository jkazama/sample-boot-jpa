package sample.model.account;

import javax.persistence.*;

import lombok.*;
import sample.context.orm.*;
import sample.model.constraints.*;

/**
 * 口座に紐づく金融機関口座を表現します。
 * <p>口座を相手方とする入出金で利用します。
 * low: サンプルなので支店や名称、名義といった本来必須な情報をかなり省略しています。(通常は全銀仕様を踏襲します)
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
