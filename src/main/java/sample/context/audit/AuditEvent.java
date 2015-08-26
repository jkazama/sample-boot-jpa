package sample.context.audit;

import java.util.Date;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.criterion.MatchMode;

import lombok.*;
import sample.ActionStatusType;
import sample.context.Dto;
import sample.context.orm.*;
import sample.model.constraints.*;
import sample.util.DateUtils;

/**
 * システムイベントの監査ログを表現します。
 */
@Entity
@Data
@EqualsAndHashCode(callSuper = false)
public class AuditEvent extends OrmActiveRecord<AuditEvent> {
	private static final long serialVersionUID = 1l;

	@Id
	@GeneratedValue
	private Long id;
	/** カテゴリ */
	private String category;
	/** メッセージ */
	private String message;
	/** 処理ステータス */
	@Enumerated(EnumType.STRING)
	private ActionStatusType statusType;
	/** エラー事由 */
	private String errorReason;
	/** 処理時間(msec) */
	private Long time;
	/** 開始日時 */
	private Date startDate;
	/** 終了日時(未完了時はnull) */
	private Date endDate;

	/** イベント監査ログを完了状態にします。 */
	public AuditEvent finish(final SystemRepository rep) {
		Date now = rep.dh().time().date();
		setStatusType(ActionStatusType.PROCESSED);
		setEndDate(now);
		setTime(endDate.getTime() - startDate.getTime());
		return update(rep);
	}

	/** イベント監査ログを取消状態にします。 */
	public AuditEvent cancel(final SystemRepository rep, String errorReason) {
		Date now = rep.dh().time().date();
		setStatusType(ActionStatusType.CANCELLED);
		setErrorReason(StringUtils.abbreviate(errorReason, 250));
		setEndDate(now);
		setTime(endDate.getTime() - startDate.getTime());
		return update(rep);
	}
	
	/** イベント監査ログを例外状態にします。 */
	public AuditEvent error(final SystemRepository rep, String errorReason) {
		Date now = rep.dh().time().date();
		setStatusType(ActionStatusType.ERROR);
		setErrorReason(StringUtils.abbreviate(errorReason, 250));
		setEndDate(now);
		setTime(endDate.getTime() - startDate.getTime());
		return update(rep);
	}

	/** イベント監査ログを登録します。 */
	public static AuditEvent register(final SystemRepository rep,
			final RegAuditEvent p) {
		return p.create(rep.dh().time().date()).save(rep);
	}

	/** イベント監査ログを検索します。 */
	public static PagingList<AuditEvent> find(final SystemRepository rep, final FindAuditEvent p) {
		OrmTemplate tmpl = rep.tmpl();
		OrmCriteria<AuditEvent> criteria = rep.criteria(AuditEvent.class);
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
	public static class FindAuditEvent implements Dto {
		private static final long serialVersionUID = 1l;
		@NameEmpty
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
	public static class RegAuditEvent implements Dto {
		private static final long serialVersionUID = 1l;
		@NameEmpty
		private String category;
		private String message;

		public AuditEvent create(Date now) {
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
			return new RegAuditEvent(category, message);
		}
	}

}
