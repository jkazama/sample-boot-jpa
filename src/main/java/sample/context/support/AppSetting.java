package sample.context.support;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Data;
import sample.context.DomainEntity;
import sample.context.Dto;
import sample.context.orm.JpqlBuilder;
import sample.context.orm.OrmMatchMode;
import sample.context.orm.OrmRepository;
import sample.model.constraints.CategoryEmpty;
import sample.model.constraints.IdStr;
import sample.model.constraints.OutlineEmpty;

/**
 * Represents application configuration information.
 * <p>
 * It is assumed that the initial data is registered in advance, and only
 * changes in values are allowed.
 */
@Entity
@Data
public class AppSetting implements DomainEntity {

    @Id
    @IdStr(max = 120)
    private String id;
    @CategoryEmpty(max = 60)
    private String category;
    @OutlineEmpty(max = 1300)
    private String outline;
    @OutlineEmpty(max = 1300)
    @Column(name = "setting_value", length = 1300, nullable = false)
    private String value;

    public String str() {
        return value;
    }

    public String str(String defaultValue) {
        return value == null ? defaultValue : value;
    }

    public int intValue() {
        return Integer.parseInt(value);
    }

    public int intValue(int defaultValue) {
        return value == null ? defaultValue : Integer.parseInt(value);
    }

    public long longValue() {
        return Long.parseLong(value);
    }

    public long longValue(long defaultValue) {
        return value == null ? defaultValue : Long.parseLong(value);
    }

    public boolean bool() {
        return Boolean.parseBoolean(value);
    }

    public boolean bool(OrmRepository rep, boolean defaultValue) {
        return value == null ? defaultValue : Boolean.parseBoolean(value);
    }

    public BigDecimal decimal() {
        return new BigDecimal(value);
    }

    public BigDecimal decimal(BigDecimal defaultValue) {
        return value == null ? defaultValue : new BigDecimal(value);
    }

    public static Optional<AppSetting> get(OrmRepository rep, String id) {
        return rep.get(AppSetting.class, id);
    }

    public static AppSetting load(OrmRepository rep, String id) {
        return rep.load(AppSetting.class, id);
    }

    /** Change information value. */
    public AppSetting change(OrmRepository rep, String value) {
        setValue(value);
        return rep.update(this);
    }

    /** Search application configuration information. */
    public static List<AppSetting> find(OrmRepository rep, FindAppSetting p) {
        var jpql = JpqlBuilder.of("SELECT s FROM AppSetting s")
                .like(List.of("s.id", "s.category", "s.outline"), p.keyword, OrmMatchMode.ANYWHERE);
        return rep.tmpl().find(jpql.build(), jpql.args());
    }

    /** search parameter */
    @Builder
    public record FindAppSetting(@OutlineEmpty String keyword) implements Dto {
    }

    public static AppSetting of(String id, String value) {
        var m = new AppSetting();
        m.id = id;
        m.value = value;
        return m;
    }

}
