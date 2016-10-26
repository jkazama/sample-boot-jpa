package sample.model.asset;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

import lombok.*;
import sample.ActionStatusType;
import sample.ValidationException.ErrorKeys;
import sample.context.*;
import sample.context.orm.*;
import sample.model.*;
import sample.model.account.FiAccount;
import sample.model.asset.Cashflow.RegCashflow;
import sample.model.asset.type.CashflowType;
import sample.model.constraints.*;
import sample.model.master.SelfFiAccount;
import sample.util.*;

/**
 * Cashflow action to ask for a transfer account activity.
 * low: It is a sample, a branch and a name, and considerably originally omit required information.
 */
@Entity
@Data
@EqualsAndHashCode(callSuper = false)
public class CashInOut extends OrmActiveMetaRecord<CashInOut> {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id;
    @IdStr
    private String accountId;
    @Currency
    private String currency;
    @AbsAmount
    private BigDecimal absAmount;
    private boolean withdrawal;
    @ISODate
    private LocalDate requestDay;
    @ISODateTime
    private LocalDateTime requestDate;
    @ISODate
    private LocalDate eventDay;
    @ISODate
    private LocalDate valueDay;
    @IdStr
    private String targetFiCode;
    @IdStr
    private String targetFiAccountId;
    @IdStr
    private String selfFiCode;
    @IdStr
    private String selfFiAccountId;
    @NotNull
    @Enumerated(EnumType.STRING)
    private ActionStatusType statusType;
    /** Set only with a processed status. */
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
     * <p>Processed CashInOut and generate Cashflow.
     */
    public CashInOut process(final OrmRepository rep) {
        TimePoint now = rep.dh().time().tp();
        validate((v) -> {
            v.verify(statusType.isUnprocessed(), ErrorKeys.ActionUnprocessing);
            v.verify(now.afterEqualsDay(eventDay), AssetErrorKeys.CashInOutAfterEqualsDay);
        });
        
        setStatusType(ActionStatusType.Processed);
        setCashflowId(Cashflow.register(rep, regCf()).getId());
        return update(rep);
    }

    private RegCashflow regCf() {
        BigDecimal amount = withdrawal ? absAmount.negate() : absAmount;
        CashflowType cashflowType = withdrawal ? CashflowType.CashOut : CashflowType.CashIn;
        String remark = withdrawal ? Remarks.CashOut : Remarks.CashIn;
        return new RegCashflow(accountId, currency, amount, cashflowType, remark, eventDay, valueDay);
    }

    /** Cancelled status. */
    public CashInOut cancel(final OrmRepository rep) {
        TimePoint now = rep.dh().time().tp();
        validate((v) -> {
            v.verify(statusType.isUnprocessing(), ErrorKeys.ActionUnprocessing);
            v.verify(now.beforeDay(eventDay), AssetErrorKeys.CashInOutBeforeEqualsDay);
        });
        
        setStatusType(ActionStatusType.Cancelled);
        return update(rep);
    }

    /**
     * Mark error status.
     * low: Actually, Take error reasons in an argument and maintain it.
     */
    public CashInOut error(final OrmRepository rep) {
        validate((v) -> v.verify(statusType.isUnprocessed(), ErrorKeys.ActionUnprocessing));

        setStatusType(ActionStatusType.Error);
        return update(rep);
    }

    public static CashInOut load(final OrmRepository rep, Long id) {
        return rep.load(CashInOut.class, id);
    }

    public static List<CashInOut> find(final OrmRepository rep, final FindCashInOut p) {
        // low: check during a period of from/to if usual
        return rep.tmpl().find(CashInOut.class, (criteria) -> criteria
                .equal("currency", p.getCurrency())
                .in("statusType", p.getStatusTypes())
                .between("updateDate", p.getUpdFromDay().atStartOfDay(), DateUtils.dateTo(p.getUpdToDay()))
                .sortDesc("updateDate")
                .result());
    }

    public static List<CashInOut> findUnprocessed(final OrmRepository rep) {
        return rep.tmpl().find("from CashInOut c where c.eventDay=?1 and c.statusType in ?2 order by c.id",
                rep.dh().time().day(), ActionStatusType.unprocessedTypes);
    }

    public static List<CashInOut> findUnprocessed(final OrmRepository rep, String accountId, String currency,
            boolean withdrawal) {
        return rep.tmpl().find(
                "from CashInOut c where c.accountId=?1 and c.currency=?2 and c.withdrawal=?3 and c.statusType in ?4 order by c.id",
                accountId, currency, withdrawal,
                ActionStatusType.unprocessedTypes);
    }

    public static List<CashInOut> findUnprocessed(final OrmRepository rep, String accountId) {
        return rep.tmpl().find(
                "from CashInOut c where c.accountId=?1 and c.statusType in ?2 order by c.updateDate desc", accountId,
                ActionStatusType.unprocessedTypes);
    }

    public static CashInOut withdraw(final OrmRepository rep, final BusinessDayHandler day, final RegCashOut p) {
        DomainHelper dh = rep.dh();
        TimePoint now = dh.time().tp();
        // low: It is often managed DB or properties.
        LocalDate eventDay = day.day();
        // low: T+N calculation that we consider the holiday of each financial institution / currency.
        LocalDate valueDay = day.day(3);

        Validator.validate((v) -> {
            v.verifyField(0 < p.getAbsAmount().signum(), "absAmount", DomainErrorKeys.AbsAmountZero);
            boolean canWithdraw = Asset.of(p.getAccountId()).canWithdraw(rep, p.getCurrency(), p.getAbsAmount(),
                    valueDay);
            v.verifyField(canWithdraw, "absAmount", AssetErrorKeys.CashInOutWithdrawAmount);
        });

        FiAccount acc = FiAccount.load(rep, p.getAccountId(), Remarks.CashOut, p.getCurrency());
        SelfFiAccount selfAcc = SelfFiAccount.load(rep, Remarks.CashOut, p.getCurrency());
        String updateActor = dh.actor().getId();
        return p.create(now, eventDay, valueDay, acc, selfAcc, updateActor).save(rep);
    }

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

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegCashOut implements Dto {
        private static final long serialVersionUID = 1L;
        @IdStrEmpty
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
            m.setStatusType(ActionStatusType.Unprocessed);
            return m;
        }
    }

}
