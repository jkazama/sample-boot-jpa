package sample.model.master;

import java.time.*;
import java.util.*;

import javax.persistence.*;
import javax.validation.Valid;

import lombok.*;
import sample.context.Dto;
import sample.context.orm.*;
import sample.model.constraints.*;
import sample.model.constraints.Year;
import sample.util.DateUtils;

/**
 * Holiday of the service company.
 */
@Entity
@Data
@EqualsAndHashCode(callSuper = false)
public class Holiday extends OrmActiveMetaRecord<Holiday> {
    private static final long serialVersionUID = 1l;
    public static final String CategoryDefault = "default";

    @Id
    @GeneratedValue
    private Long id;
    @Category
    private String category;
    @ISODate
    private LocalDate day;
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

    public static Optional<Holiday> get(final OrmRepository rep, LocalDate day) {
        return get(rep, day, CategoryDefault);
    }

    public static Optional<Holiday> get(final OrmRepository rep, LocalDate day, String category) {
        return rep.tmpl().get("from Holiday h where h.category=?1 and h.day=?2", category, day);
    }

    public static Holiday load(final OrmRepository rep, LocalDate day) {
        return load(rep, day, CategoryDefault);
    }

    public static Holiday load(final OrmRepository rep, LocalDate day, String category) {
        return rep.tmpl().load("from Holiday h where h.category=?1 and h.day=?2", category, day);
    }

    public static List<Holiday> find(final OrmRepository rep, final int year) {
        return find(rep, year, CategoryDefault);
    }

    public static List<Holiday> find(final OrmRepository rep, final int year, final String category) {
        return rep.tmpl().find("from Holiday h where h.category=?1 and h.day between ?2 and ?3 order by h.day",
                category, LocalDate.ofYearDay(year, 1), DateUtils.dayTo(year));
    }

    public static void register(final OrmRepository rep, final RegHoliday p) {
        rep.tmpl().execute("delete from Holiday h where h.category=?1 and h.day between ?2 and ?3",
                p.category, LocalDate.ofYearDay(p.year, 1), DateUtils.dayTo(p.year));
        p.list.forEach(v -> v.create(p).save(rep));
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegHoliday implements Dto {
        private static final long serialVersionUID = 1l;
        @CategoryEmpty
        private String category = CategoryDefault;
        @Year
        private int year;
        @Valid
        private List<RegHolidayItem> list;

        public RegHoliday(int year, final List<RegHolidayItem> list) {
            this.year = year;
            this.list = list;
        }
    }

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
