package sample.context;

import java.math.BigDecimal;
import java.util.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.Size;

import org.hibernate.criterion.MatchMode;

import lombok.*;
import sample.context.orm.*;
import sample.model.constraints.OutlineEmpty;

/**
 * Application setting information.
 * <p>Premise that initial data are registered beforehand and permit only the change of the value.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class AppSetting extends OrmActiveRecord<AppSetting> {
    private static final long serialVersionUID = 1l;

    @Id
    @Size(max = 120)
    private String id;
    @Size(max = 60)
    private String category;
    @Size(max = 1300)
    private String outline;
    @Size(max = 1300)
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

    public AppSetting update(OrmRepository rep, String value) {
        setValue(value);
        return update(rep);
    }

    public static List<AppSetting> find(OrmRepository rep, FindAppSetting p) {
        return rep.tmpl().find(AppSetting.class, (criteria) -> {
            return criteria
                    .like(new String[] { "id", "category", "outline" }, p.keyword, MatchMode.ANYWHERE)
                    .result();
        });
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FindAppSetting implements Dto {
        private static final long serialVersionUID = 1l;
        @OutlineEmpty
        private String keyword;
    }

}
