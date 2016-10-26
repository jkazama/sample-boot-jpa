package sample.model.master;

import java.util.List;

import javax.persistence.*;

import lombok.*;
import sample.context.orm.*;
import sample.model.constraints.*;

/**
 * The authority that was assigned to an staff.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class StaffAuthority extends OrmActiveRecord<StaffAuthority> {
    private static final long serialVersionUID = 1l;

    @Id
    @GeneratedValue
    private Long id;
    @IdStr
    private String staffId;
    /** authority. (give "ROLE_" before) */
    @Name
    private String authority;

    public static List<StaffAuthority> find(final OrmRepository rep, String staffId) {
        return rep.tmpl().find("from StaffAuthority where staffId=?1", staffId);
    }

}
