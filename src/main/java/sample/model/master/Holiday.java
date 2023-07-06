package sample.model.master;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.validation.Valid;
import lombok.Builder;
import lombok.Data;
import sample.context.DomainMetaEntity;
import sample.context.Dto;
import sample.context.ErrorKeys;
import sample.context.ValidationException;
import sample.context.orm.OrmRepository;
import sample.model.constraints.Category;
import sample.model.constraints.CategoryEmpty;
import sample.model.constraints.ISODate;
import sample.model.constraints.ISODateTime;
import sample.model.constraints.IdStr;
import sample.model.constraints.Name;
import sample.model.constraints.NameEmpty;
import sample.model.constraints.OutlineEmpty;
import sample.model.constraints.Year;
import sample.util.DateUtils;

/**
 * Represents a holiday master.
 */
@Entity
@Data
public class Holiday implements DomainMetaEntity {
    public static final String CategoryDefault = "default";

    @Id
    @GeneratedValue
    private Long id;
    /** Holiday classification (currency, country, financial institution, etc.) */
    @Category
    private String category;
    @ISODate
    private LocalDate holiday;
    @NameEmpty(max = 40)
    private String name;
    @OutlineEmpty
    private String outline;
    @ISODateTime
    private LocalDateTime createDate;
    @IdStr
    private String createId;
    @ISODateTime
    private LocalDateTime updateDate;
    @IdStr
    private String updateId;

    public static Optional<Holiday> get(final OrmRepository rep, LocalDate day) {
        return get(rep, CategoryDefault, day);
    }

    public static Optional<Holiday> get(final OrmRepository rep, String category, LocalDate day) {
        var jpql = """
                SELECT h FROM Holiday h WHERE h.category=?1 AND h.hoilday=?2
                """;
        return rep.tmpl().get(jpql, category, day);
    }

    public static Holiday load(final OrmRepository rep, LocalDate day) {
        return load(rep, CategoryDefault, day);
    }

    public static Holiday load(final OrmRepository rep, String category, LocalDate holiday) {
        return get(rep, category, holiday)
                .orElseThrow(
                        () -> ValidationException.of(ErrorKeys.EntityNotFound, holiday.toString()));
    }

    public static List<Holiday> find(final OrmRepository rep, final FindHoliday param) {
        var category = param.category != null ? param.category : CategoryDefault;
        var fromDay = LocalDate.ofYearDay(param.year, 1);
        var toDay = DateUtils.dayTo(param.year);
        var jpql = """
                SELECT h
                FROM Holiday h
                WHERE h.category=?1 AND h.hoilday BETWEEN ?2 AND ?3
                ORDER BY h.hoilday
                """;
        return rep.tmpl().find(jpql, category, fromDay, toDay);
    }

    @Builder
    public static record FindHoliday(
            @CategoryEmpty String category,
            @Year Integer year) {

    }

    /**
     * Register holiday master.
     * <p>
     * Batch registration after deleting all holidays for the specified year.
     */
    public static void register(final OrmRepository rep, final RegHoliday param) {
        var fromDay = LocalDate.ofYearDay(param.year, 1);
        var toDay = DateUtils.dayTo(param.year);
        var jpqlDel = """
                DELETE FROM Holiday h WHERE h.category=?1 AND h.hoilday BETWEEN ?2 AND ?3
                """;
        rep.tmpl().execute(jpqlDel, param.category, fromDay, toDay);
        param.list.stream()
                .filter(v -> DateUtils.includes(v.holiday(), fromDay, toDay))
                .forEach(v -> rep.saveOrUpdate(v.create(param)));
    }

    @Builder
    public static record RegHoliday(
            @CategoryEmpty String category,
            @Year Integer year,
            @Valid List<RegHolidayItem> list) implements Dto {
    }

    @Builder
    public static record RegHolidayItem(
            @ISODate LocalDate holiday,
            @Name(max = 40) String name) implements Dto {

        public Holiday create(RegHoliday parent) {
            var m = new Holiday();
            m.setCategory(parent.category != null ? parent.category : CategoryDefault);
            m.setHoliday(this.holiday);
            m.setName(this.name);
            return m;
        }
    }

}
