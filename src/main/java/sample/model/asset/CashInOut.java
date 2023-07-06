package sample.model.asset;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import sample.context.ActionStatusType;
import sample.context.DomainHelper;
import sample.context.DomainMetaEntity;
import sample.context.Dto;
import sample.context.ErrorKeys;
import sample.context.orm.OrmRepository;
import sample.model.BusinessDayHandler;
import sample.model.DomainErrorKeys;
import sample.model.account.FiAccount;
import sample.model.asset.Cashflow.RegCashflow;
import sample.model.asset.type.CashflowType;
import sample.model.constraints.AbsAmount;
import sample.model.constraints.Currency;
import sample.model.constraints.CurrencyEmpty;
import sample.model.constraints.ISODate;
import sample.model.constraints.ISODateTime;
import sample.model.constraints.IdStr;
import sample.model.constraints.IdStrEmpty;
import sample.model.master.SelfFiAccount;
import sample.util.AppValidator;
import sample.util.DateUtils;
import sample.util.TimePoint;

/**
 * A cash flow action that expresses a transfer deposit/withdrawal request.
 * <p>
 * Since the financial institution information of the counterparty/owner may be
 * changed after the request, the information is de-normalized to maintain the
 * status at the time of the request.
 * low: It is a sample, a branch and a name, and considerably originally omit
 * required information.
 */
@Entity
@Data
public class CashInOut implements DomainMetaEntity {

    /** ID (deposit/withdrawal request No.) */
    @Id
    @IdStr
    private String cashInOutId;
    @IdStr
    private String accountId;
    @Currency
    private String currency;
    @AbsAmount
    private BigDecimal absAmount;
    /** true at the time of withdrawal */
    private boolean withdrawal;
    @ISODate
    private LocalDate requestDay;
    @ISODateTime
    private LocalDateTime requestDate;
    /** Amount Accrual date */
    @ISODate
    private LocalDate eventDay;
    /** Amount Delivery Date */
    @ISODate
    private LocalDate valueDay;
    /** Counterparty Financial Institution Code */
    @IdStr
    private String targetFiCode;
    /** Counterparty Financial Institution Account ID */
    @IdStr
    private String targetFiAccountId;
    /** Own Financial Institution Code */
    @IdStr
    private String selfFiCode;
    /** Own financial institution account ID */
    @IdStr
    private String selfFiAccountId;
    @NotNull
    @Enumerated
    private ActionStatusType statusType;
    /**
     * Cash flow ID, set only for cases that have been processed. low: actually, the
     * concepts of adjusting CF, erasing CF, etc. need to be added.
     */
    private String cashflowId;
    @ISODateTime
    private LocalDateTime createDate;
    @IdStr
    private String createId;
    @ISODateTime
    private LocalDateTime updateDate;
    @IdStr
    private String updateId;

    /**
     * Processed status.
     * <p>
     * Processed CashInOut and generate Cashflow.
     */
    public CashInOut process(final OrmRepository rep) {
        // low: 出金営業日の取得。ここでは単純な営業日を取得
        TimePoint now = rep.dh().time().tp();
        // 事前審査
        AppValidator.validate(v -> {
            v.verify(statusType.isUnprocessed(), ErrorKeys.ActionUnprocessing);
            v.verify(now.afterEqualsDay(eventDay), AssetErrorKeys.CashInOutAfterEqualsDay);
        });
        // 処理済状態を反映
        setStatusType(ActionStatusType.PROCESSED);
        setCashflowId(Cashflow.register(rep, regCf()).getCashflowId());
        return rep.update(this);
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
     * <p>
     * "処理済みでない"かつ"発生日を迎えていない"必要があります。
     */
    public CashInOut cancel(final OrmRepository rep) {
        TimePoint now = rep.dh().time().tp();
        // 事前審査
        AppValidator.validate((v) -> {
            v.verify(statusType.isUnprocessing(), ErrorKeys.ActionUnprocessing);
            v.verify(now.beforeDay(eventDay), AssetErrorKeys.CashInOutBeforeEqualsDay);
        });
        // 取消状態を反映
        this.setStatusType(ActionStatusType.CANCELLED);
        return rep.update(this);
    }

    /**
     * 依頼をエラー状態にします。
     * <p>
     * 処理中に失敗した際に呼び出してください。
     * low: 実際はエラー事由などを引数に取って保持する
     */
    public CashInOut error(final OrmRepository rep) {
        AppValidator.validate((v) -> {
            v.verify(statusType.isUnprocessed(), ErrorKeys.ActionUnprocessing);
        });

        this.setStatusType(ActionStatusType.ERROR);
        return rep.update(this);
    }

    /** 振込入出金依頼を返します。 */
    public static CashInOut load(final OrmRepository rep, Long id) {
        return rep.load(CashInOut.class, id);
    }

    /** 未処理の振込入出金依頼一覧を検索します。 low: criteriaベース実装例 */
    public static List<CashInOut> find(final OrmRepository rep, final FindCashInOut p) {
        // low: 通常であれば事前にfrom/toの期間チェックを入れる
        return rep.tmpl().find(CashInOut.class, (criteria) -> criteria
                .equal("currency", p.getCurrency())
                .in("statusType", p.getStatusTypes())
                .between("updateDate", p.getUpdFromDay().atStartOfDay(), DateUtils.dateTo(p.getUpdToDay()))
                .sortDesc("updateDate")
                .result());
    }

    /** 当日発生で未処理の振込入出金一覧を検索します。 */
    public static List<CashInOut> findUnprocessed(final OrmRepository rep) {
        return rep.tmpl().find("from CashInOut c where c.eventDay=?1 and c.statusType in ?2 order by c.id",
                rep.dh().time().day(), ActionStatusType.UNPROCESSED_TYPES);
    }

    /** 未処理の振込入出金一覧を検索します。(口座別) */
    public static List<CashInOut> findUnprocessed(final OrmRepository rep, String accountId, String currency,
            boolean withdrawal) {
        return rep.tmpl().find(
                "from CashInOut c where c.accountId=?1 and c.currency=?2 and c.withdrawal=?3 and c.statusType in ?4 order by c.id",
                accountId, currency, withdrawal,
                ActionStatusType.UNPROCESSED_TYPES);
    }

    /** 未処理の振込入出金一覧を検索します。(口座別) */
    public static List<CashInOut> findUnprocessed(final OrmRepository rep, String accountId) {
        return rep.tmpl().find(
                "from CashInOut c where c.accountId=?1 and c.statusType in ?2 order by c.updateDate desc", accountId,
                ActionStatusType.UNPROCESSED_TYPES);
    }

    /** 出金依頼をします。 */
    public static CashInOut withdraw(final OrmRepository rep, final BusinessDayHandler day, final RegCashOut p) {
        DomainHelper dh = rep.dh();
        TimePoint now = dh.time().tp();
        // low: 発生日は締め時刻等の兼ね合いで営業日と異なるケースが多いため、別途DB管理される事が多い
        LocalDate eventDay = day.day();
        // low: 実際は各金融機関/通貨の休日を考慮しての T+N 算出が必要
        LocalDate valueDay = day.day(3);

        // 事前審査
        AppValidator.validate((v) -> {
            v.verifyField(0 < p.absAmount().signum(), "absAmount", DomainErrorKeys.AbsAmountZero);
            boolean canWithdraw = Asset.by(p.accountId()).canWithdraw(rep, p.currency(), p.absAmount(),
                    valueDay);
            v.verifyField(canWithdraw, "absAmount", AssetErrorKeys.CashInOutWithdrawAmount);
        });

        // 出金依頼情報を登録
        FiAccount acc = FiAccount.load(rep, p.accountId(), Remarks.CashOut, p.currency());
        SelfFiAccount selfAcc = SelfFiAccount.load(rep, Remarks.CashOut, p.currency());
        String updateActor = dh.actor().id();
        return rep.save(p.create(now, eventDay, valueDay, acc, selfAcc, updateActor));
    }

    /** 振込入出金依頼の検索パラメタ。 low: 通常は顧客視点/社内視点で利用条件が異なる */
    @Builder
    public static record FindCashInOut(
            @CurrencyEmpty String currency,
            ActionStatusType[] statusTypes,
            @ISODate LocalDate updFromDay,
            @ISODate LocalDate updToDay) implements Dto {
    }

    /** 振込出金の依頼パラメタ。 */
    @Builder
    public static record RegCashOut(
            @IdStrEmpty String accountId,
            @Currency String currency,
            @AbsAmount BigDecimal absAmount) implements Dto {

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
