package sample.context.audit;

import java.time.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.criterion.MatchMode;

import lombok.*;
import sample.ActionStatusType;
import sample.context.Dto;
import sample.context.orm.*;
import sample.context.orm.Sort.SortOrder;
import sample.model.constraints.*;
import sample.util.DateUtils;

/**
 * The auditting log of the system event.
 */
@Entity
@Data
@EqualsAndHashCode(callSuper = false)
public class AuditEvent extends OrmActiveRecord<AuditEvent> {
    private static final long serialVersionUID = 1l;

    @Id
    @GeneratedValue
    private Long id;
    private String category;
    private String message;
    @Enumerated(EnumType.STRING)
    private ActionStatusType statusType;
    private String errorReason;
    /** The processing time (msec) */
    private Long time;
    @NotNull
    private LocalDateTime startDate;
    /** The end date and time (at the time of non-completion null) */
    private LocalDateTime endDate;

    public AuditEvent finish(final SystemRepository rep) {
        LocalDateTime now = rep.dh().time().date();
        setStatusType(ActionStatusType.Processed);
        setEndDate(now);
        setTime(DateUtils.between(startDate, endDate).get().toMillis());
        return update(rep);
    }

    public AuditEvent cancel(final SystemRepository rep, String errorReason) {
        LocalDateTime now = rep.dh().time().date();
        setStatusType(ActionStatusType.Cancelled);
        setErrorReason(StringUtils.abbreviate(errorReason, 250));
        setEndDate(now);
        setTime(DateUtils.between(startDate, endDate).get().toMillis());
        return update(rep);
    }

    public AuditEvent error(final SystemRepository rep, String errorReason) {
        LocalDateTime now = rep.dh().time().date();
        setStatusType(ActionStatusType.Error);
        setErrorReason(StringUtils.abbreviate(errorReason, 250));
        setEndDate(now);
        setTime(DateUtils.between(startDate, endDate).get().toMillis());
        return update(rep);
    }

    public static AuditEvent register(final SystemRepository rep, final RegAuditEvent p) {
        return p.create(rep.dh().time().date()).save(rep);
    }

    public static PagingList<AuditEvent> find(final SystemRepository rep, final FindAuditEvent p) {
        return rep.tmpl().find(AuditEvent.class, (criteria) -> {
            return criteria
                    .equal("category", p.category)
                    .equal("statusType", p.statusType)
                    .like(new String[] { "message", "errorReason" }, p.keyword, MatchMode.ANYWHERE)
                    .between("startDate", p.fromDay.atStartOfDay(), DateUtils.dateTo(p.toDay));
        }, p.page.sortIfEmpty(SortOrder.desc("startDate")));
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FindAuditEvent implements Dto {
        private static final long serialVersionUID = 1l;
        @NameEmpty
        private String category;
        @DescriptionEmpty
        private String keyword;
        private ActionStatusType statusType;
        @ISODate
        private LocalDate fromDay;
        @ISODate
        private LocalDate toDay;
        @NotNull
        private Pagination page = new Pagination();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegAuditEvent implements Dto {
        private static final long serialVersionUID = 1l;
        @NameEmpty
        private String category;
        private String message;

        public AuditEvent create(LocalDateTime now) {
            AuditEvent event = new AuditEvent();
            event.setCategory(category);
            event.setMessage(message);
            event.setStatusType(ActionStatusType.Processing);
            event.setStartDate(now);
            return event;
        }

        public static RegAuditEvent of(String message) {
            return of("default", message);
        }

        public static RegAuditEvent of(String category, String message) {
            return new RegAuditEvent(category, message);
        }
    }

}
