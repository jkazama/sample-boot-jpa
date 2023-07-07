package sample.model.asset;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

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
import sample.context.orm.JpqlBuilder;
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
 * A cash flow action that expresses a account transfer deposit/withdrawal
 * request.
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

    /**
     * ID (account transfer deposit/withdrawal request No.)
     * low: Make it a meaningful ID as it will be the ID used to respond to customer
     * inquiries
     */
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
    private Long cashflowId;
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
        // low: the business day of the disbursement. Get a simple business day here.
        TimePoint now = rep.dh().time().tp();
        // business validation
        AppValidator.validate(v -> {
            v.verify(statusType.isUnprocessed(), DomainErrorKeys.StatusType);
            v.verify(now.afterEqualsDay(eventDay), AssetErrorKeys.BeforeEventDay);
        });
        Long cashflowId = Cashflow.register(rep, this.toRegCasflow()).getCashflowId();
        // Reflects processed status
        this.setStatusType(ActionStatusType.PROCESSED);
        this.setCashflowId(cashflowId);
        return rep.update(this);
    }

    private RegCashflow toRegCasflow() {
        BigDecimal amount = this.withdrawal ? this.absAmount.negate() : this.absAmount;
        var cashflowType = this.withdrawal ? CashflowType.CASH_OUT : CashflowType.CASH_IN;
        // low: The abstract is simple for now. In fact, it is better to have a usage
        // field to CashInOut (change the abstract according to the source method).
        String remark = this.withdrawal ? Remarks.CashOut : Remarks.CashIn;
        return RegCashflow.builder()
                .accountId(this.accountId)
                .currency(this.currency)
                .amount(amount)
                .cashflowType(cashflowType)
                .remark(remark)
                .eventDay(this.eventDay)
                .valueDay(this.valueDay)
                .build();
    }

    /**
     * Cancel the request.
     * <p>
     * The "not yet processed" and "not yet accrual date" are required.
     */
    public CashInOut cancel(final OrmRepository rep) {
        TimePoint now = rep.dh().time().tp();
        // business validation
        AppValidator.validate((v) -> {
            v.verify(statusType.isUnprocessing(), DomainErrorKeys.StatusType);
            v.verify(now.beforeDay(eventDay), AssetErrorKeys.AfterEqualsEventDay);
        });
        // Reflects canceled status
        this.setStatusType(ActionStatusType.CANCELLED);
        return rep.update(this);
    }

    /**
     * Put the request in an error state.
     * <p>
     * Call it when it fails during processing.
     * low: In actuality, error reasons, etc., are taken as arguments and retained.
     */
    public CashInOut error(final OrmRepository rep) {
        // business validation
        AppValidator.validate((v) -> {
            v.verify(statusType.isUnprocessed(), DomainErrorKeys.StatusType);
        });
        // Reflects error status
        this.setStatusType(ActionStatusType.ERROR);
        return rep.update(this);
    }

    public static String formatId(long v) {
        // low: Correct code formatting
        return "C" + StringUtils.leftPad(String.valueOf(v), 10, '0');
    }

    public static CashInOut load(final OrmRepository rep, String cashInOutId) {
        return rep.load(CashInOut.class, cashInOutId);
    }

    public static List<CashInOut> find(final OrmRepository rep, final FindCashInOut param) {
        // low: Normally, put a from/to time period check in advance.
        var jpql = JpqlBuilder.of("SELECT cio FROM CashInOut cio")
                .equal("cio.currency", param.currency())
                .in("cio.statusType", param.statusTypes())
                .between("cio.updateDate", param.updFromDay().atStartOfDay(), DateUtils.dateTo(param.updToDay()))
                .orderBy("cio.updateDate DESC");
        return rep.tmpl().find(jpql.build(), jpql.args());
    }

    /**
     * Search parameter for account transfer deposit/withdrawal requests.
     * low: Usually different usage conditions from customer perspective/internal
     * perspective.
     */
    @Builder
    public static record FindCashInOut(
            @CurrencyEmpty String currency,
            Collection<ActionStatusType> statusTypes,
            @ISODate LocalDate updFromDay,
            @ISODate LocalDate updToDay) implements Dto {
    }

    /**
     * Searches for listings that occurred on the same day and have not yet been
     * processed.
     */
    public static List<CashInOut> findUnprocessed(final OrmRepository rep) {
        var jpql = """
                SELECT cio
                FROM CashInOut cio
                WHERE cio.eventDay=?1 AND cio.statusType IN (?2)
                ORDER BY cio.cashInOutId
                """;
        return rep.tmpl().find(jpql, rep.dh().time().day(), ActionStatusType.UNPROCESSED_TYPES);
    }

    /** Searches for listings that have not yet been processed. (by account) */
    public static List<CashInOut> findUnprocessed(
            final OrmRepository rep, String accountId, String currency, boolean withdrawal) {
        var jpql = """
                SELECT cio
                FROM CashInOut cio
                WHERE cio.accountId=?1 AND cio.currency=?2 AND cio.withdrawal=?3 AND cio.statusType IN (?4)
                ORDER BY cio.cashInOutId
                """;
        return rep.tmpl().find(jpql, accountId, currency, withdrawal, ActionStatusType.UNPROCESSED_TYPES);
    }

    /** Searches for listings that have not yet been processed. (by account) */
    public static List<CashInOut> findUnprocessed(final OrmRepository rep, String accountId) {
        var jpql = """
                SELECT cio
                FROM CashInOut cio
                WHERE cio.accountId=?1 AND cio.statusType IN (?2)
                ORDER BY cio.updateDate DESC
                """;
        return rep.tmpl().find(jpql, accountId, ActionStatusType.UNPROCESSED_TYPES);
    }

    /** Request a withdrawal. */
    public static CashInOut withdraw(final OrmRepository rep, final BusinessDayHandler day, final RegCashOut p) {
        DomainHelper dh = rep.dh();
        TimePoint now = dh.time().tp();
        // low: In many cases, the date of occurrence differs from the business day due
        // to a combination of closing time, etc., so it is often managed in a separate
        // DB.
        LocalDate eventDay = day.day();
        // low: In reality, T+N calculations must take into account holidays of each
        // financial institution/currency
        LocalDate valueDay = day.day(3);

        // business validation
        AppValidator.validate((v) -> {
            v.verifyField(0 < p.absAmount().signum(), "absAmount", DomainErrorKeys.AbsAmountZero);
            boolean canWithdraw = Asset.of(p.accountId())
                    .canWithdraw(rep, p.currency(), p.absAmount(), valueDay);
            v.verifyField(canWithdraw, "absAmount", AssetErrorKeys.WithdrawAmount);
        });

        // Register withdrawal request information
        String cashInOutId = rep.dh().uid().generate(CashInOut.class);
        var acc = FiAccount.load(rep, p.accountId(), Remarks.CashOut, p.currency());
        var selfAcc = SelfFiAccount.load(rep, Remarks.CashOut, p.currency());
        String updateActor = dh.actor().id();
        return rep.save(p.create(cashInOutId, now, eventDay, valueDay, acc, selfAcc, updateActor));
    }

    @Builder
    public static record RegCashOut(
            @IdStrEmpty String accountId,
            @Currency String currency,
            @AbsAmount BigDecimal absAmount) implements Dto {

        public CashInOut create(
                String cashInOutId,
                final TimePoint now,
                LocalDate eventDay,
                LocalDate valueDay,
                final FiAccount acc,
                final SelfFiAccount selfAcc,
                String updActor) {
            CashInOut m = new CashInOut();
            m.setCashInOutId(cashInOutId);
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
