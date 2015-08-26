package sample.model.master;

import java.util.*;

import javax.persistence.*;
import javax.validation.Valid;

import lombok.*;
import sample.context.Dto;
import sample.context.orm.*;
import sample.model.constraints.*;

/**
 * 祝日マスタを表現します。
 */
@Entity
@Data
@EqualsAndHashCode(callSuper = false)
public class Holiday extends OrmActiveMetaRecord<Holiday> {
	private static final long serialVersionUID = 1l;
	public static final String CATEGORY_DEFAULT = "default";

	/** ID */
	@Id
	@GeneratedValue
	private Long id;
	/** 祝日区分 */
	@Category
	private String category;
	/** 祝日 */
	@Day
	private String day;
	/** 祝日名称 */
	@Name(max = 40)
	private String name;
	private Date createDate;
	@IdStr
	private String createId;
	private Date updateDate;
	@IdStr
	private String updateId;

	/** 祝日マスタを取得します。 */
	public static Optional<Holiday> get(final OrmRepository rep, String day) {
		return get(rep, day, CATEGORY_DEFAULT);
	}
	public static Optional<Holiday> get(final OrmRepository rep, String day, String category) {
		return rep.tmpl().get("from Holiday h where h.category=?1 and h.day=?2", category, day);
	}

	/** 祝日マスタを取得します。(例外付) */
	public static Holiday load(final OrmRepository rep, String day) {
		return load(rep, day, CATEGORY_DEFAULT);
	}
	public static Holiday load(final OrmRepository rep, String day, String category) {
		return rep.tmpl().load("from Holiday h where h.category=?1 and h.day=?2", category, day);
	}

	/** 祝日情報を検索します。 */
	public static List<Holiday> find(final OrmRepository rep, final String year) {
		return find(rep, year, CATEGORY_DEFAULT);
	}
	public static List<Holiday> find(final OrmRepository rep, final String year, final String category) {
		return rep.tmpl().find("from Holiday h where h.category=?1 and h.day like ?2 order by h.day", category, year + "%");
	}

	/** 祝日マスタを登録します。 */
	public static void register(final OrmRepository rep, final RegisterHoliday p) {
		rep.tmpl().execute("delete from Holiday h where h.category=?1 and h.day like ?2'", p.category, p.year + "%");
		p.list.forEach(v -> v.create(p).save(rep));
	}

	/** 登録パラメタ */
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class RegisterHoliday implements Dto {
		private static final long serialVersionUID = 1l;
		@Category
		private String category;
		@Year
		private String year;
		@Valid
		private List<RegisterHolidayItem> list;
	}

	/** 登録パラメタ(要素) */
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class RegisterHolidayItem implements Dto {
		private static final long serialVersionUID = 1l;
		@Day
		private String day;
		@Name(max = 40)
		private String name;

		public Holiday create(RegisterHoliday p) {
			Holiday holiday = new Holiday();
			holiday.setCategory(p.category);
			holiday.setDay(day);
			holiday.setName(name);
			return holiday;
		}
	}

}
