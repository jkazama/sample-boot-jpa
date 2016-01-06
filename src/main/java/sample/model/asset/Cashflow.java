package sample.model.asset;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import lombok.*;
import sample.ActionStatusType;
import sample.ValidationException.ErrorKeys;
import sample.context.Dto;
import sample.context.orm.*;
import sample.model.asset.type.CashflowType;
import sample.model.constraints.*;
import sample.util.*;

/**
 * 入出金キャッシュフローを表現します。
 * キャッシュフローは振込/振替といったキャッシュフローアクションから生成される確定状態(依頼取消等の無い)の入出金情報です。
 * low: 概念を伝えるだけなので必要最低限の項目で表現しています。
 * low: 検索関連は主に経理確認や帳票等での利用を想定します
 */
@Entity
@Data
@EqualsAndHashCode(callSuper = false)
public class Cashflow extends OrmActiveMetaRecord<Cashflow> {

    private static final long serialVersionUID = 1L;

    /** ID */
    @Id
    @GeneratedValue
    private Long id;
    /** 口座ID */
    @IdStr
    private String accountId;
    /** 通貨 */
    @Currency
    private String currency;
    /** 金額 */
    @Amount
    private BigDecimal amount;
    /** 入出金 */
    @NotNull
    @Enumerated(EnumType.STRING)
    private CashflowType cashflowType;
    /** 摘要 */
    @Category
    private String remark;
    /** 発生日/日時 */
    @ISODate
    private LocalDate eventDay;
    @ISODateTime
    private LocalDateTime eventDate;
    /** 受渡日 */
    @ISODate
    private LocalDate valueDay;
    /** 処理種別 */
    @NotNull
    @Enumerated(EnumType.STRING)
    private ActionStatusType statusType;
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

    /** キャッシュフローを処理済みにして残高へ反映します。 */
    public Cashflow realize(final OrmRepository rep) {
        validate((v) -> {
            v.verify(canRealize(rep), AssetErrorKeys.CashflowRealizeDay);
            v.verify(statusType.isUnprocessing(), ErrorKeys.ActionUnprocessing); // 「既に処理中/処理済です」
        });

        setStatusType(ActionStatusType.PROCESSED);
        update(rep);
        CashBalance.getOrNew(rep, accountId, currency).add(rep, amount);
        return this;
    }

    /**
     * キャッシュフローをエラー状態にします。
     * <p>処理中に失敗した際に呼び出してください。
     * low: 実際はエラー事由などを引数に取って保持する
     */
    public Cashflow error(final OrmRepository rep) {
        validate((v) -> v.verify(statusType.isUnprocessed(), ErrorKeys.ActionUnprocessing));

        setStatusType(ActionStatusType.ERROR);
        return update(rep);
    }

    /** キャッシュフローを実現(受渡)可能か判定します。 */
    public boolean canRealize(final OrmRepository rep) {
        return rep.dh().time().tp().afterEqualsDay(valueDay);
    }

    /** キャッシュフローを取得します。(例外付) */
    public static Cashflow load(final OrmRepository rep, Long id) {
        return rep.load(Cashflow.class, id);
    }

    /**
     * 指定受渡日時点で未実現のキャッシュフロー一覧を検索します。(口座通貨別)
     */
    public static List<Cashflow> findUnrealize(final OrmRepository rep, String accountId, String currency,
            LocalDate valueDay) {
        return rep.tmpl().find(
                "from Cashflow c where c.accountId=?1 and c.currency=?2 and c.valueDay<=?3 and c.statusType in ?4 order by c.id",
                accountId, currency, valueDay, ActionStatusType.unprocessingTypes);
    }

    /**
     * 指定受渡日で実現対象となるキャッシュフロー一覧を検索します。
     */
    public static List<Cashflow> findDoRealize(final OrmRepository rep, LocalDate valueDay) {
        return rep.tmpl().find("from Cashflow c where c.valueDay=?1 and c.statusType in ?2 order by c.id", valueDay,
                ActionStatusType.unprocessedTypes);
    }

    /**
     * キャッシュフローを登録します。
     * 受渡日を迎えていた時はそのまま残高へ反映します。
     */
    public static Cashflow register(final OrmRepository rep, final RegCashflow p) {
        TimePoint now = rep.dh().time().tp();
        Validator.validate((v) -> v.checkField(now.beforeEqualsDay(p.getValueDay()),
                "valueDay", AssetErrorKeys.CashflowBeforeEqualsDay));
        Cashflow cf = p.create(now).save(rep);
        return cf.canRealize(rep) ? cf.realize(rep) : cf;
    }

    /** 入出金キャッシュフローの登録パラメタ。  */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegCashflow implements Dto {
        private static final long serialVersionUID = 1L;
        @IdStr
        private String accountId;
        @Currency
        private String currency;
        @Amount
        private BigDecimal amount;
        @NotNull
        private CashflowType cashflowType;
        @Category
        private String remark;
        /** 未設定時は営業日を設定 */
        @ISODateEmpty
        private LocalDate eventDay;
        @ISODate
        private LocalDate valueDay;

        public Cashflow create(final TimePoint now) {
            TimePoint eventDate = eventDay == null ? now : new TimePoint(eventDay, now.getDate());
            Cashflow m = new Cashflow();
            m.setAccountId(accountId);
            m.setCurrency(currency);
            m.setAmount(amount);
            m.setCashflowType(cashflowType);
            m.setRemark(remark);
            m.setEventDay(eventDate.getDay());
            m.setEventDate(eventDate.getDate());
            m.setValueDay(valueDay);
            m.setStatusType(ActionStatusType.UNPROCESSED);
            return m;
        }
    }

}
