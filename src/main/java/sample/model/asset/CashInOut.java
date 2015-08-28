package sample.model.asset;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

import lombok.*;
import sample.ActionStatusType;
import sample.context.*;
import sample.context.orm.*;
import sample.model.BusinessDayHandler;
import sample.model.account.FiAccount;
import sample.model.asset.Cashflow.RegCashflow;
import sample.model.asset.type.CashflowType;
import sample.model.constraints.*;
import sample.model.master.SelfFiAccount;
import sample.util.*;

/**
 * 振込入出金依頼を表現するキャッシュフローアクション。
 * <p>相手方/自社方の金融機関情報は依頼後に変更される可能性があるため、依頼時点の状態を
 * 保持するために非正規化して情報を保持しています。
 * low: 相手方/自社方の金融機関情報は項目数が多いのでサンプル用に金融機関コードのみにしています。
 * 実際の開発ではそれぞれ複合クラス(FinantialInstitution)に束ねるアプローチを推奨します。
 */
@Entity
@Data
@EqualsAndHashCode(callSuper = false)
public class CashInOut extends OrmActiveMetaRecord<CashInOut> {

	private static final long serialVersionUID = 1L;

	/** ID(振込依頼No) */
	@Id
	@GeneratedValue
	private Long id;
	/** 口座ID */
	@IdStr
	private String accountId;
	/** 通貨 */
	@Currency
	private String currency;
	/** 金額(絶対値) */
	@AbsAmount
	private BigDecimal absAmount;
	/** 出金時はtrue */
	private boolean withdrawal;
	/** 依頼日/日時 */
	@ISODate
	private LocalDate requestDay;
	@ISODateTime
	private LocalDateTime requestDate;
	/** 発生日 */
	@ISODate
	private LocalDate eventDay;
	/** 受渡日 */
	@ISODate
	private LocalDate valueDay;
	/** 相手方金融機関コード */
	@IdStr
	private String targetFiCode;
	/** 相手方金融機関口座ID */
	@IdStr
	private String targetFiAccountId;
	/** 自社方金融機関コード */
	@IdStr
	private String selfFiCode;
	/** 自社方金融機関口座ID */
	@IdStr
	private String selfFiAccountId;
	/** 処理種別 */
	@NotNull
	@Enumerated(EnumType.STRING)
	private ActionStatusType statusType;
	/** キャッシュフローID。処理済のケースでのみ設定されます。low: 実際は調整CFや消込CFの概念なども有 */
	private Long cashflowId;
	/** 登録日時 */
	@ISODateTime
	private LocalDateTime createDate;
	/** 登録者ID */
	@IdStr
	private String createId;
	/** 更新日時 */
	@ISODateTime
	private LocalDateTime updateDate;
	/** 更新者ID */
	@IdStr
	private String updateId;

	/**
	 * 依頼を処理します。
	 * <p>依頼情報を処理済にしてキャッシュフローを生成します。
	 */
	public CashInOut process(final OrmRepository rep) {
		//low: 出金営業日の取得。ここでは単純な営業日を取得
		TimePoint now = rep.dh().time().tp();
		// 事前審査
		validate((v) -> {
			v.verify(statusType.isUnprocessed(), "error.ActionStatusType.unprocessing");
			v.verify(now.afterEqualsDay(eventDay), "error.CashInOut.afterEqualsDay");
		});
		// 処理済状態を反映
		setStatusType(ActionStatusType.PROCESSED);
		setCashflowId(Cashflow.register(rep, regCf()).getId());
		return update(rep);
	}

	private RegCashflow regCf() {
		BigDecimal amount = withdrawal ? absAmount.negate() : absAmount;
		CashflowType cashflowType = withdrawal ? CashflowType.CashOut : CashflowType.CashIn;
		// low: 摘要はとりあえずシンプルに。実際はCashInOutへ用途フィールドをもたせた方が良い(生成元メソッドに応じて摘要を変える)
		String remark = withdrawal ? Remarks.CashOut : Remarks.CashIn;
		return new RegCashflow(accountId, currency, amount, cashflowType, remark, eventDay, valueDay);
	}

	/**
	 * 依頼を取消します。
	 * <p>"処理済みでない"かつ"発生日を迎えていない"必要があります。
	 */
	public CashInOut cancel(final OrmRepository rep) {
		TimePoint now = rep.dh().time().tp();
		// 事前審査
		validate((v) -> {
			v.verify(statusType.isUnprocessing(), "error.ActionStatusType.unprocessing");
			v.verify(now.beforeDay(eventDay), "error.CashInOut.beforeEqualsDay");			
		});
		// 取消状態を反映
		setStatusType(ActionStatusType.CANCELLED);
		return update(rep);
	}

	/**
	 * 依頼をエラー状態にします。
	 * <p>処理中に失敗した際に呼び出してください。
	 * low: 実際はエラー事由などを引数に取って保持する
	 */
	public CashInOut error(final OrmRepository rep) {
		validate((v) -> v.verify(statusType.isUnprocessed(), "error.ActionStatusType.unprocessing"));

		setStatusType(ActionStatusType.ERROR);
		return update(rep);
	}

	/** 振込入出金依頼を返します。 */
	public static CashInOut load(final OrmRepository rep, Long id) {
		return rep.load(CashInOut.class, id);
	}

	/** 未処理の振込入出金依頼一覧を検索します。  low: criteriaベース実装例 */
	public static List<CashInOut> find(final OrmRepository rep, final FindCashInOut p) {
		// low: 通常であれば事前にfrom/toの期間チェックを入れる
		return rep.tmpl().find(CashInOut.class, (criteria) ->
			criteria
				.equal("currency", p.getCurrency())
				.in("statusType", p.getStatusTypes())
				.between("updateDate", p.getUpdFromDay().atStartOfDay(), DateUtils.dateTo(p.getUpdToDay()))
				.sortDesc("updateDate")
				.result());
	}

	/** 当日発生で未処理の振込入出金一覧を検索します。 */
	public static List<CashInOut> findUnprocessed(final OrmRepository rep) {
		return rep.tmpl().find("from CashInOut c where c.eventDay=?1 and c.statusType in ?2 order by c.id", rep.dh().time().day(), ActionStatusType.unprocessedTypes);
	}

	/** 未処理の振込入出金一覧を検索します。(口座別) */
	public static List<CashInOut> findUnprocessed(final OrmRepository rep, String accountId, String currency,
			boolean withdrawal) {
		return rep.tmpl().find("from CashInOut c where c.accountId=?1 and c.currency=?2 and c.withdrawal=?3 and c.statusType in ?4 order by c.id", accountId, currency, withdrawal,
				ActionStatusType.unprocessedTypes);
	}
	
	/** 未処理の振込入出金一覧を検索します。(口座別) */
	public static List<CashInOut> findUnprocessed(final OrmRepository rep, String accountId) {
		return rep.tmpl().find("from CashInOut c where c.accountId=?1 and c.statusType in ?2 order by c.updateDate desc", accountId, ActionStatusType.unprocessedTypes);
	}

	/** 出金依頼をします。 */
	public static CashInOut withdraw(final OrmRepository rep, final BusinessDayHandler day,	final RegCashOut p) {
		DomainHelper dh = rep.dh();
		TimePoint now = dh.time().tp();
		// low: 発生日は締め時刻等の兼ね合いで営業日と異なるケースが多いため、別途DB管理される事が多い
		LocalDate eventDay = day.day();
		// low: 実際は各金融機関/通貨の休日を考慮しての T+N 算出が必要
		LocalDate valueDay = day.day(3);
		
		// 事前審査
		Validator.validate((v) -> {
			v.verifyField(0 < p.getAbsAmount().signum(), "absAmount", "error.domain.AbsAmount.zero");
			boolean canWithdraw = Asset.by(p.getAccountId()).canWithdraw(rep, p.getCurrency(), p.getAbsAmount(), valueDay);
			v.verifyField(canWithdraw, "absAmount", "error.CashInOut.withdrawAmount");
		});

		// 出金依頼情報を登録
		FiAccount acc = FiAccount.load(rep, p.getAccountId(), Remarks.CashOut, p.getCurrency());
		SelfFiAccount selfAcc = SelfFiAccount.load(rep, Remarks.CashOut, p.getCurrency());
		String updateActor = dh.actor().getId();
		return p.create(now, eventDay, valueDay, acc, selfAcc, updateActor).save(rep);
	}

	/** 振込入出金依頼の検索パラメタ。 low: 通常は顧客視点/社内視点で利用条件が異なる */
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class FindCashInOut implements Dto {
		private static final long serialVersionUID = 1L;
		@CurrencyEmpty
		private String currency;
		private ActionStatusType[] statusTypes;
		@ISODate
		private LocalDate updFromDay;
		@ISODate
		private LocalDate updToDay;
	}

	/** 振込出金の依頼パラメタ。  */
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class RegCashOut implements Dto {
		private static final long serialVersionUID = 1L;
		@IdStr
		private String accountId;
		@Currency
		private String currency;
		@AbsAmount
		private BigDecimal absAmount;

		public CashInOut create(final TimePoint now, LocalDate eventDay, LocalDate valueDay, final FiAccount acc,
				final SelfFiAccount selfAcc, String updActor) {
			CashInOut m = new CashInOut();
			m.setAccountId(accountId);
			m.setCurrency(currency);
			m.setAbsAmount(absAmount);
			m.setWithdrawal(true);
			m.setRequestDay(now.getDay());
			m.setRequestDate(now.getDate());
			m.setEventDay(eventDay);
			m.setValueDay(valueDay);
			m.setTargetFiCode(acc.getFiCode());
			m.setTargetFiAccountId(acc.getFiAccountId());
			m.setSelfFiCode(selfAcc.getFiCode());
			m.setSelfFiAccountId(selfAcc.getFiAccountId());
			m.setStatusType(ActionStatusType.UNPROCESSED);
			return m;
		}
	}

}
