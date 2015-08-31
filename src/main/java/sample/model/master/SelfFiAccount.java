package sample.model.master;

import javax.persistence.*;

import lombok.*;
import sample.context.orm.*;
import sample.model.constraints.*;

/**
 * サービス事業者の決済金融機関を表現します。
 * low: サンプルなので支店や名称、名義といったなど本来必須な情報をかなり省略しています。(通常は全銀仕様を踏襲します)
 */
@Entity
@Data
@EqualsAndHashCode(callSuper = false)
public class SelfFiAccount extends OrmActiveRecord<SelfFiAccount> {

	private static final long serialVersionUID = 1L;

	/** ID */
	@Id
	@GeneratedValue
	private Long id;
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

	public static SelfFiAccount load(final OrmRepository rep, String category, String currency) {
		return rep.tmpl().load("from SelfFiAccount a where a.category=?1 and a.currency=?2", category, currency);
	}

}
