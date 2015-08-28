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
 * 口座残高を表現します。
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class CashBalance extends OrmActiveRecord<CashBalance> {
	private static final long serialVersionUID = 1L;

	/** ID */
	@Id
	@GeneratedValue
	private Long id;
	/** 口座ID */
	@IdStr
	private String accountId;
	/** 基準日 */
	@ISODate
	private LocalDate baseDay;
	/** 通貨 */
	@Currency
	private String currency;
	/** 金額 */
	@Amount
	private BigDecimal amount;
	/** 更新日 */
	@ISODateTime
	private LocalDateTime updateDate;

	/**
	 * 残高へ指定した金額を反映します。
	 * low ここではCurrencyを使っていますが、実際の通貨桁数や端数処理定義はDBや設定ファイル等で管理されます。
	 */
	public CashBalance add(final OrmRepository rep, BigDecimal addAmount) {
		int scale = java.util.Currency.getInstance(currency).getDefaultFractionDigits();
		RoundingMode mode = RoundingMode.DOWN;
		setAmount(Calculator.of(amount).scale(scale, mode).add(addAmount).decimal());
		return update(rep);
	}

	/**
	 * 指定口座の残高を取得します。(存在しない時は繰越保存後に取得します)
	 * low: 複数通貨の適切な考慮や細かい審査は本筋でないので割愛。
	 */
	public static CashBalance getOrNew(final OrmRepository rep, String accountId, String currency) {
		LocalDate baseDay = rep.dh().time().day();
		Optional<CashBalance> m =
				rep.tmpl().get("from CashBalance c where c.accountId=?1 and c.currency=?2 and c.baseDay=?3 order by c.baseDay desc", accountId, currency, baseDay);
		return m.orElseGet(() -> create(rep, accountId, currency));
	}

	private static CashBalance create(final OrmRepository rep, String accountId, String currency) {
		TimePoint now= rep.dh().time().tp();
		Optional<CashBalance> m =
				rep.tmpl().get("from CashBalance c where c.accountId=?1 and c.currency=?2 order by c.baseDay desc", accountId, currency);
		if (m.isPresent()) { // 残高繰越
			CashBalance prev = m.get();
			return new CashBalance(null, accountId, now.getDay(), currency, prev.getAmount(), now.getDate()).save(rep);
		} else {
			return new CashBalance(null, accountId, now.getDay(), currency, BigDecimal.ZERO, now.getDate()).save(rep);
		}
	}

}
