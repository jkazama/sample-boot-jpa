package sample.model.master;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import sample.context.Dto;
import sample.context.orm.OrmActiveMetaRecord;
import sample.context.orm.OrmRepository;
import sample.model.constraints.Category;
import sample.model.constraints.CategoryEmpty;
import sample.model.constraints.ISODate;
import sample.model.constraints.ISODateTime;
import sample.model.constraints.IdStr;
import sample.model.constraints.Name;
import sample.model.constraints.Year;
import sample.util.DateUtils;

/**
 * 休日マスタを表現します。
 */
@Entity
@Data
@EqualsAndHashCode(callSuper = false)
public class Holiday extends OrmActiveMetaRecord<Holiday> {
    private static final long serialVersionUID = 1l;
    public static final String CategoryDefault = "default";

    /** ID */
    @Id
    @GeneratedValue
    private Long id;
    /** 休日区分 */
    @Category
    private String category;
    /** 休日 */
    @ISODate
    @Column(name = "holiday")
    private LocalDate day;
    /** 休日名称 */
    @Name(max = 40)
    private String name;
    @ISODateTime
    private LocalDateTime createDate;
    @IdStr
    private String createId;
    @ISODateTime
    private LocalDateTime updateDate;
    @IdStr
    private String updateId;

    /** 休日マスタを取得します。 */
    public static Optional<Holiday> get(final OrmRepository rep, LocalDate day) {
        return get(rep, day, CategoryDefault);
    }

    public static Optional<Holiday> get(final OrmRepository rep, LocalDate day, String category) {
        return rep.tmpl().get("from Holiday h where h.category=?1 and h.day=?2", category, day);
    }

    /** 休日マスタを取得します。(例外付) */
    public static Holiday load(final OrmRepository rep, LocalDate day) {
        return load(rep, day, CategoryDefault);
    }

    public static Holiday load(final OrmRepository rep, LocalDate day, String category) {
        return rep.tmpl().load("from Holiday h where h.category=?1 and h.day=?2", category, day);
    }

    /** 休日情報を検索します。 */
    public static List<Holiday> find(final OrmRepository rep, final int year) {
        return find(rep, year, CategoryDefault);
    }

    public static List<Holiday> find(final OrmRepository rep, final int year, final String category) {
        return rep.tmpl().find("from Holiday h where h.category=?1 and h.day between ?2 and ?3 order by h.day",
                category, LocalDate.ofYearDay(year, 1), DateUtils.dayTo(year));
    }

    /** 休日マスタを登録します。 */
    public static void register(final OrmRepository rep, final RegHoliday p) {
        rep.tmpl().execute("delete from Holiday h where h.category=?1 and h.day between ?2 and ?3",
                p.category, LocalDate.ofYearDay(p.year, 1), DateUtils.dayTo(p.year));
        p.list.forEach(v -> v.create(p).save(rep));
    }

    /** 登録パラメタ */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegHoliday implements Dto {
        private static final long serialVersionUID = 1l;
        @CategoryEmpty
        private String category = CategoryDefault;
        @Year
        private Integer year;
        @Valid
        private List<RegHolidayItem> list = new ArrayList<>();

        public RegHoliday(int year, final List<RegHolidayItem> list) {
            this.year = year;
            this.list = list;
        }
    }

    /** 登録パラメタ(要素) */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegHolidayItem implements Dto {
        private static final long serialVersionUID = 1l;
        @ISODate
        private LocalDate day;
        @Name(max = 40)
        private String name;

        public Holiday create(RegHoliday p) {
            Holiday holiday = new Holiday();
            holiday.setCategory(p.category);
            holiday.setDay(day);
            holiday.setName(name);
            return holiday;
        }
    }

}
