package sample.model.master;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
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
    private static final String SequenceId = "staff_authority_id_seq";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SequenceId)
    @SequenceGenerator(name = SequenceId, sequenceName = SequenceId, allocationSize = 1)
    private Long id;
    @IdStr
    private String staffId;
    /** Authority Name. */
    @Name
    private String authority;

    /** Returns a list of privileges associated with the staff ID. */
    public static List<StaffAuthority> find(final OrmRepository rep, String staffId) {
        var jpql = "SELECT sa FROM StaffAuthority sa WHERE sa.staffId=?1";
        return rep.tmpl().find(jpql, staffId);
    }

}
