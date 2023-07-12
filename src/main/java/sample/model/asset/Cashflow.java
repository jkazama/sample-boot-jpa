package sample.model.asset;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import sample.context.ActionStatusType;
import sample.context.DomainMetaEntity;
import sample.context.Dto;
import sample.context.orm.OrmRepository;
import sample.model.DomainErrorKeys;
import sample.model.asset.type.CashflowType;
import sample.model.constraints.Amount;
import sample.model.constraints.Category;
import sample.model.constraints.Currency;
import sample.model.constraints.ISODate;
import sample.model.constraints.ISODateEmpty;
import sample.model.constraints.ISODateTime;
import sample.model.constraints.IdStr;
import sample.util.AppValidator;
import sample.util.TimePoint;

/**
 * Represents cash flow of deposits and withdrawals.
 * Cash Flow is the information on deposits and withdrawals in a confirmed state
 * (without request cancellation, etc.) generated from cash flow actions such as
 * payments/transfers.
 * low: Since we are only conveying concepts, we use the minimum number of items
 * necessary to express them.
 * low: Search related will be used mainly for accounting checks, forms, etc.
 */
@Entity
@Data
public class Cashflow implements DomainMetaEntity {
    private static final String SequenceId = "cashflow_id_seq";

    /** cashflow ID */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SequenceId)
    @SequenceGenerator(name = SequenceId, sequenceName = SequenceId, allocationSize = 1)
    private Long cashflowId;
    /** account ID */
    @IdStr
    private String accountId;
    @Currency
    private String currency;
    @Amount
    private BigDecimal amount;
    @NotNull
    @Enumerated
    private CashflowType cashflowType;
    @Category
    private String remark;
    /** Amount Accrual date */
    @ISODate
    private LocalDate eventDay;
    /** Amount Accrual date/time */
    @ISODateTime
    private LocalDateTime eventDate;
    /** Amount Delivery date */
    @ISODate
    private LocalDate valueDay;
    @NotNull
    @Enumerated
    private ActionStatusType statusType;
    @ISODateTime
    private LocalDateTime createDate;
    @IdStr
    private String createId;
    @ISODateTime
    private LocalDateTime updateDate;
    @IdStr
    private String updateId;

    /** The cash flow is processed and reflected in the balance. */
    public Cashflow realize(final OrmRepository rep) {
        AppValidator.validate((v) -> {
            v.verify(canRealize(rep), AssetErrorKeys.RealizeDay);
            v.verify(statusType.isUnprocessing(), DomainErrorKeys.StatusType);
        });

        setStatusType(ActionStatusType.PROCESSED);
        rep.update(this);
        CashBalance.getOrNew(rep, accountId, currency).add(rep, amount);
        return this;
    }

    /**
     * Change the cash flow into an error state.
     * <p>
     * Call it when it fails during processing.
     * low: In actuality, error reasons, etc., are taken as arguments and retained.
     */
    public Cashflow error(final OrmRepository rep) {
        AppValidator.validate((v) -> {
            v.verify(statusType.isUnprocessed(), DomainErrorKeys.StatusType);
        });

        setStatusType(ActionStatusType.ERROR);
        return rep.update(this);
    }

    /** Determine if cash flow can be realized (delivered). */
    public boolean canRealize(final OrmRepository rep) {
        return rep.dh().time().tp().afterEqualsDay(valueDay);
    }

    public static Cashflow load(final OrmRepository rep, Long cashflowId) {
        return rep.load(Cashflow.class, cashflowId);
    }

    /**
     * Search a list of unrealized cash flows as of the specified delivery date.
     * (by account currency)
     */
    public static List<Cashflow> findUnrealize(
            final OrmRepository rep,
            String accountId,
            String currency,
            LocalDate valueDay) {
        var jpql = """
                SELECT c
                FROM Cashflow c
                WHERE c.accountId=?1 AND c.currency=?2 AND c.valueDay<=?3 AND c.statusType IN (?4)
                ORDER BY c.cashflowId
                """;
        return rep.tmpl().find(jpql, accountId, currency, valueDay, ActionStatusType.UNPROCESSING_TYPES);
    }

    /**
     * Search the list of cash flows to be realized at the specified delivery date.
     */
    public static List<Cashflow> findDoRealize(final OrmRepository rep, LocalDate valueDay) {
        var jpql = """
                SELECT c
                FROM Cashflow c
                WHERE c.valueDay=?1 AND c.statusType IN (?2)
                ORDER BY c.id
                """;
        return rep.tmpl().find(jpql, valueDay, ActionStatusType.UNPROCESSED_TYPES);
    }

    /**
     * Register cash flow.
     * When the delivery date has been reached, the balance is reflected as it is.
     */
    public static Cashflow register(final OrmRepository rep, final RegCashflow p) {
        TimePoint now = rep.dh().time().tp();
        AppValidator.validate((v) -> {
            v.checkField(now.beforeEqualsDay(p.valueDay()), "valueDay", AssetErrorKeys.AfterValueDay);
        });
        Cashflow cf = rep.save(p.create(now));
        return cf.canRealize(rep) ? cf.realize(rep) : cf;
    }

    @Builder
    public static record RegCashflow(
            @IdStr String accountId,
            @Currency String currency,
            @Amount BigDecimal amount,
            @NotNull CashflowType cashflowType,
            @Category String remark,
            /** If not set, set a business day */
            @ISODateEmpty LocalDate eventDay,
            @ISODate LocalDate valueDay) implements Dto {

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
