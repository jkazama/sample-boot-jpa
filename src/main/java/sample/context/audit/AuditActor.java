package sample.context.audit;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Set;

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
import sample.context.actor.Actor;
import sample.context.actor.type.ActorRoleType;
import sample.context.orm.JpqlBuilder;
import sample.context.orm.OrmMatchMode;
import sample.context.orm.OrmRepository;
import sample.model.constraints.Category;
import sample.model.constraints.CategoryEmpty;
import sample.model.constraints.DescriptionEmpty;
import sample.model.constraints.ISODateTime;
import sample.model.constraints.IdStr;
import sample.model.constraints.IdStrEmpty;
import sample.util.ConvertUtils;
import sample.util.DateUtils;

/**
 * Represents the audit log of application users.
 */
@Entity
@Data
public class AuditActor implements DomainEntity {
    private static final String SequenceId = "audit_actor_id_seq";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SequenceId)
    @SequenceGenerator(name = SequenceId, sequenceName = SequenceId, allocationSize = 1)
    private Long id;
    @IdStr
    private String actorId;
    @NotNull
    @Enumerated
    private ActorRoleType roleType;
    private String source;
    @Category
    private String category;
    private String message;
    @NotNull
    @Enumerated
    private ActionStatusType statusType;
    @DescriptionEmpty
    private String errorReason;
    /** Processing time (msec) */
    private Long time;
    @NotNull
    private LocalDateTime startDate;
    /** End date and time (null if not completed) */
    private LocalDateTime endDate;

    /** User audit log is set to PROCESSED status. */
    public AuditActor finish(final OrmRepository rep) {
        if (Actor.ANONYMOUS.id().equals(this.actorId)) {
            setActorId(rep.dh().actor().id());
        }
        LocalDateTime now = rep.dh().time().date();
        setStatusType(ActionStatusType.PROCESSED);
        setEndDate(now);
        setTime(DateUtils.between(startDate, endDate).get().toMillis());
        return rep.update(this);
    }

    /** User audit log is set to CANCELLED status. */
    public AuditActor cancel(final OrmRepository rep, String errorReason) {
        LocalDateTime now = rep.dh().time().date();
        setStatusType(ActionStatusType.CANCELLED);
        setErrorReason(StringUtils.abbreviate(errorReason, 250));
        setEndDate(now);
        setTime(DateUtils.between(startDate, endDate).get().toMillis());
        return rep.update(this);
    }

    /** Set the user audit log to ERROR status. */
    public AuditActor error(final OrmRepository rep, String errorReason) {
        LocalDateTime now = rep.dh().time().date();
        setStatusType(ActionStatusType.ERROR);
        setErrorReason(StringUtils.abbreviate(errorReason, 250));
        setEndDate(now);
        setTime(DateUtils.between(startDate, endDate).get().toMillis());
        return rep.update(this);
    }

    /** Search user audit logs. */
    public static Page<AuditActor> find(final OrmRepository rep, final FindAuditActor p) {
        var jpql = JpqlBuilder.of("SELECT aa FROM AuditActor aa")
                .like(Arrays.asList("aa.actorId", "aa.source"), p.actorId, OrmMatchMode.ANYWHERE)
                .equal("aa.category", p.category)
                .in("aa.roleType", p.roleTypes)
                .equal("aa.statusType", p.statusType)
                .like(Arrays.asList("aa.message", "aa.errorReason"), p.keyword, OrmMatchMode.ANYWHERE)
                .between("aa.startDate", p.fromDate, p.toDate)
                .orderBy("aa.startDate DESC");
        return rep.tmpl().find(jpql.build(), p.pageable(), jpql.args());
    }

    /** search parameter */
    @Builder
    public static record FindAuditActor(
            @IdStrEmpty String actorId,
            @CategoryEmpty String category,
            @DescriptionEmpty String keyword,
            @NotNull Set<ActorRoleType> roleTypes,
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

    /** Register user audit logs. */
    public static AuditActor register(final OrmRepository rep, final RegAuditActor p) {
        return rep.save(p.create(rep.dh().actor(), rep.dh().time().date()));
    }

    /** registered parameter */
    @Builder
    public static record RegAuditActor(
            String category,
            String message) implements Dto {

        public AuditActor create(final Actor actor, LocalDateTime now) {
            var audit = new AuditActor();
            audit.setActorId(actor.id());
            audit.setRoleType(actor.roleType());
            audit.setSource(actor.source());
            audit.setCategory(category);
            audit.setMessage(ConvertUtils.left(message, 300));
            audit.setStatusType(ActionStatusType.PROCESSING);
            audit.setStartDate(now);
            return audit;
        }

        public static RegAuditActor of(String message) {
            return of("default", message);
        }

        public static RegAuditActor of(String category, String message) {
            return RegAuditActor.builder()
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
