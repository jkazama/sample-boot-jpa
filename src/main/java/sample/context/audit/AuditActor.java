package sample.context.audit;

import java.util.Date;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.criterion.MatchMode;

import lombok.*;
import sample.ActionStatusType;
import sample.context.Dto;
import sample.context.actor.Actor;
import sample.context.orm.*;
import sample.model.constraints.*;
import sample.util.*;

/**
 * システム利用者の監査ログを表現します。
 */
@Entity
@Data
@EqualsAndHashCode(callSuper = false)
public class AuditActor  extends OrmActiveRecord<AuditActor> {
	private static final long serialVersionUID = 1l;

	@Id
	@GeneratedValue
	private Long id;
	/** 利用者ID */
	@IdStr
	private String actorId;
	/** 利用者ソース(IP等) */
	private String source;
	/** カテゴリ */
	@Category
	private String category;
	/** メッセージ */
	private String message;
	/** 処理ステータス */
	@NotNull
	@Enumerated(EnumType.STRING)
	private ActionStatusType statusType;
	/** エラー事由 */
	@DescriptionEmpty
	private String errorReason;
	/** 処理時間(msec) */
	private Long time;
	/** 開始日時 */
	private Date startDate;
	/** 終了日時(未完了時はnull) */
	private Date endDate;

	/** 利用者監査ログを完了状態にします。 */
	public AuditActor finish(final SystemRepository rep) {
		Date now = rep.dh().time().date();
		setStatusType(ActionStatusType.PROCESSED);
		setEndDate(now);
		setTime(endDate.getTime() - startDate.getTime());
		return update(rep);
	}

	/** 利用者監査ログを取消状態にします。 */
	public AuditActor cancel(final SystemRepository rep, String errorReason) {
		Date now = rep.dh().time().date();
		setStatusType(ActionStatusType.CANCELLED);
		setErrorReason(StringUtils.abbreviate(errorReason, 250));
		setEndDate(now);
		setTime(endDate.getTime() - startDate.getTime());
		return update(rep);
	}
	
	/** 利用者監査ログを例外状態にします。 */
	public AuditActor error(final SystemRepository rep, String errorReason) {
		Date now = rep.dh().time().date();
		setStatusType(ActionStatusType.ERROR);
		setErrorReason(StringUtils.abbreviate(errorReason, 250));
		setEndDate(now);
		setTime(endDate.getTime() - startDate.getTime());
		return update(rep);
	}

	/** 利用者監査ログを登録します。 */
	public static AuditActor register(final SystemRepository rep, final RegAuditActor p) {
		return p.create(rep.dh().actor(), rep.dh().time().date()).save(rep);
	}
	
	/** 利用者監査ログを検索します。 */
	public static PagingList<AuditActor> find(final SystemRepository rep, final FindAuditActor p) {
		OrmTemplate tmpl = rep.tmpl();
		OrmCriteria<AuditActor> criteria = rep.criteria(AuditActor.class);
		criteria.like(new String[]{"actorId", "source"}, p.actorId, MatchMode.ANYWHERE);
		criteria.equal("category", p.category);
		criteria.equal("statusType", p.statusType);
		criteria.like(new String[]{"message", "errorReason"}, p.keyword, MatchMode.ANYWHERE);
		criteria.between("startDate", DateUtils.date(p.fromDay), DateUtils.dateTo(p.toDay));
        p.page.getSort().desc("startDate");
		return tmpl.find(criteria.result(), p.page);
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
		private ActionStatusType statusType;
		@Day
		private String fromDay;
		@Day
		private String toDay;
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

		public AuditActor create(final Actor actor, Date now) {
			AuditActor audit = new AuditActor();
			audit.setActorId(actor.getId());
			audit.setSource(actor.getSource());
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
			return new RegAuditActor(category, message);
		}
	}

}
