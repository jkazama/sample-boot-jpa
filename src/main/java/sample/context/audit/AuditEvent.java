package sample.context.audit;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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
import sample.context.DomainEntity;
import sample.context.Dto;
import sample.context.orm.JpqlBuilder;
import sample.context.orm.OrmMatchMode;
import sample.context.orm.OrmRepository;
import sample.model.constraints.DescriptionEmpty;
import sample.model.constraints.ISODateTime;
import sample.model.constraints.NameEmpty;
import sample.util.DateUtils;

/**
 * Represents the audit log of system events.
 */
@Entity
@Data
public class AuditEvent implements DomainEntity {
    private static final String SequenceId = "audit_event_id_seq";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SequenceId)
    @SequenceGenerator(name = SequenceId, sequenceName = SequenceId, allocationSize = 1)
    private Long id;
    private String category;
    private String message;
    @Enumerated
    private ActionStatusType statusType;
    private String errorReason;
    /** Processing time (msec) */
    private Long time;
    @NotNull
    private LocalDateTime startDate;
    /** End date and time (null if not completed) */
    private LocalDateTime endDate;

    /** Event audit log is set to PROCESSED status. */
    public AuditEvent finish(final OrmRepository rep) {
        LocalDateTime now = rep.dh().time().date();
        setStatusType(ActionStatusType.PROCESSED);
        setEndDate(now);
        setTime(DateUtils.between(startDate, endDate).get().toMillis());
        return rep.update(this);
    }

    /** Event audit log is set to CANCELLED status. */
    public AuditEvent cancel(final OrmRepository rep, String errorReason) {
        LocalDateTime now = rep.dh().time().date();
        setStatusType(ActionStatusType.CANCELLED);
        setErrorReason(StringUtils.abbreviate(errorReason, 250));
        setEndDate(now);
        setTime(DateUtils.between(startDate, endDate).get().toMillis());
        return rep.update(this);
    }

    /** Set the event audit log to ERROR status. */
    public AuditEvent error(final OrmRepository rep, String errorReason) {
        LocalDateTime now = rep.dh().time().date();
        setStatusType(ActionStatusType.ERROR);
        setErrorReason(StringUtils.abbreviate(errorReason, 250));
        setEndDate(now);
        setTime(DateUtils.between(startDate, endDate).get().toMillis());
        return rep.update(this);
    }

    /** Search the event audit log. */
    public static Page<AuditEvent> find(final OrmRepository rep, final FindAuditEvent p) {
        JpqlBuilder jpql = JpqlBuilder.of("SELECT ae FROM AuditEvent ae")
                .equal("ae.category", p.category)
                .equal("ae.statusType", p.statusType)
                .like(Arrays.asList("ae.message", "ae.errorReason"), p.keyword, OrmMatchMode.ANYWHERE)
                .between("ae.startDate", p.fromDate, p.toDate)
                .orderBy("ae.startDate DESC");
        return rep.tmpl().find(jpql.build(), p.pageable(), jpql.args());
    }

    /** search parameter */
    @Builder
    public static record FindAuditEvent(
            @NameEmpty String category,
            @DescriptionEmpty String keyword,
            ActionStatusType statusType,
            @ISODateTime LocalDateTime fromDate,
            @ISODateTime LocalDateTime toDate,
            Integer size,
            Integer page) {
        public Pageable pageable() {
            return PageRequest.of(
                    page == null ? 0 : page,
                    size == null || size <= 100 ? 100 : size);
        }
    }

    /** Register event audit logs. */
    public static AuditEvent register(final OrmRepository rep, final RegAuditEvent p) {
        return rep.save(p.create(rep.dh().time().date()));
    }

    /** registered parameter */
    @Builder
    public static record RegAuditEvent(
            @NameEmpty String category,
            String message) implements Dto {

        public AuditEvent create(LocalDateTime now) {
            AuditEvent event = new AuditEvent();
            event.setCategory(category);
            event.setMessage(message);
            event.setStatusType(ActionStatusType.PROCESSING);
            event.setStartDate(now);
            return event;
        }

        public static RegAuditEvent of(String message) {
            return of("default", message);
        }

        public static RegAuditEvent of(String category, String message) {
            return RegAuditEvent.builder()
                    .category(category)
                    .message(message)
                    .build();
        }
    }

    public static void purge(final OrmRepository rep, LocalDate expiredDay) {
        var jpql = "DELETE FROM AuditActor aa WHERE aa.startDate <= ?1";
        rep.tmpl().execute(jpql, expiredDay.atStartOfDay());
    }

}
