package sample.context.audit;

import java.time.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.criterion.MatchMode;

import lombok.*;
import sample.ActionStatusType;
import sample.context.Dto;
import sample.context.actor.Actor;
import sample.context.actor.Actor.ActorRoleType;
import sample.context.orm.*;
import sample.context.orm.Sort.SortOrder;
import sample.model.constraints.*;
import sample.util.*;

/**
 * The auditing log of actor.
 */
@Entity
@Data
@EqualsAndHashCode(callSuper = false)
public class AuditActor extends OrmActiveRecord<AuditActor> {
    private static final long serialVersionUID = 1l;

    @Id
    @GeneratedValue
    private Long id;
    @IdStr
    private String actorId;
    @NotNull
    @Enumerated(EnumType.STRING)
    private ActorRoleType roleType;
    /** Actor source (including the IP) */
    private String source;
    @Category
    private String category;
    private String message;
    @NotNull
    @Enumerated(EnumType.STRING)
    private ActionStatusType statusType;
    @DescriptionEmpty
    private String errorReason;
    /** The processing time (msec) */
    private Long time;
    @NotNull
    private LocalDateTime startDate;
    /** The end date and time (at the time of non-completion null) */
    private LocalDateTime endDate;

    public AuditActor finish(final SystemRepository rep) {
        LocalDateTime now = rep.dh().time().date();
        setStatusType(ActionStatusType.Processed);
        setEndDate(now);
        setTime(DateUtils.between(startDate, endDate).get().toMillis());
        return update(rep);
    }

    public AuditActor cancel(final SystemRepository rep, String errorReason) {
        LocalDateTime now = rep.dh().time().date();
        setStatusType(ActionStatusType.Cancelled);
        setErrorReason(StringUtils.abbreviate(errorReason, 250));
        setEndDate(now);
        setTime(DateUtils.between(startDate, endDate).get().toMillis());
        return update(rep);
    }

    public AuditActor error(final SystemRepository rep, String errorReason) {
        LocalDateTime now = rep.dh().time().date();
        setStatusType(ActionStatusType.Error);
        setErrorReason(StringUtils.abbreviate(errorReason, 250));
        setEndDate(now);
        setTime(DateUtils.between(startDate, endDate).get().toMillis());
        return update(rep);
    }

    public static AuditActor register(final SystemRepository rep, final RegAuditActor p) {
        return p.create(rep.dh().actor(), rep.dh().time().date()).save(rep);
    }

    public static PagingList<AuditActor> find(final SystemRepository rep, final FindAuditActor p) {
        return rep.tmpl().find(AuditActor.class, (criteria) -> {
            return criteria
                    .like(new String[] { "actorId", "source" }, p.actorId, MatchMode.ANYWHERE)
                    .equal("category", p.category)
                    .equal("roleType", p.roleType)
                    .equal("statusType", p.statusType)
                    .like(new String[] { "message", "errorReason" }, p.keyword, MatchMode.ANYWHERE)
                    .between("startDate", p.fromDay.atStartOfDay(), DateUtils.dateTo(p.toDay));
        }, p.page.sortIfEmpty(SortOrder.desc("startDate")));
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FindAuditActor implements Dto {
        private static final long serialVersionUID = 1l;
        @IdStrEmpty
        private String actorId;
        @CategoryEmpty
        private String category;
        @DescriptionEmpty
        private String keyword;
        @NotNull
        private ActorRoleType roleType = ActorRoleType.User;
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
    public static class RegAuditActor implements Dto {
        private static final long serialVersionUID = 1l;
        private String category;
        private String message;

        public AuditActor create(final Actor actor, LocalDateTime now) {
            AuditActor audit = new AuditActor();
            audit.setActorId(actor.getId());
            audit.setRoleType(actor.getRoleType());
            audit.setSource(actor.getSource());
            audit.setCategory(category);
            audit.setMessage(ConvertUtils.left(message, 300));
            audit.setStatusType(ActionStatusType.Processing);
            audit.setStartDate(now);
            return audit;
        }

        public static RegAuditActor of(String message) {
            return of("default", message);
        }

        public static RegAuditActor of(String category, String message) {
            return new RegAuditActor(category, message);
        }
    }

}
