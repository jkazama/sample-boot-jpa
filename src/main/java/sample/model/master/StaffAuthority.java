package sample.model.master;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;
import sample.context.DomainEntity;
import sample.context.orm.OrmRepository;
import sample.model.constraints.IdStr;
import sample.model.constraints.Name;

/**
 * Represents the authority assigned to an staff.
 */
@Entity
@Data
public class StaffAuthority implements DomainEntity {

    @Id
    @GeneratedValue
    private Long id;
    @IdStr
    private String staffId;
    /** Authority Name. */
    @Name
    private String authority;

    /** Returns a list of privileges associated with the staff ID. */
    public static List<StaffAuthority> find(final OrmRepository rep, String staffId) {
        var jpql = "SELECT sa FROM StaffAuthority sa WHERE staffId=?1";
        return rep.tmpl().find(jpql, staffId);
    }

}
