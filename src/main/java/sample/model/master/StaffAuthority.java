package sample.model.master;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import sample.context.orm.OrmActiveRecord;
import sample.context.orm.OrmRepository;
import sample.model.constraints.IdStr;
import sample.model.constraints.Name;

/**
 * 社員に割り当てられた権限を表現します。
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class StaffAuthority extends OrmActiveRecord<StaffAuthority> {
    private static final long serialVersionUID = 1l;

    /** ID */
    @Id
    @GeneratedValue
    private Long id;
    /** 社員ID */
    @IdStr
    private String staffId;
    /** 権限名称。(「プリフィックスにROLE_」を付与してください) */
    @Name
    private String authority;

    /** 口座IDに紐付く権限一覧を返します。 */
    public static List<StaffAuthority> find(final OrmRepository rep, String staffId) {
        return rep.tmpl().find("from StaffAuthority where staffId=?1", staffId);
    }

}
