package sample.model.master;

import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Data;
import sample.context.DomainEntity;
import sample.context.Dto;
import sample.context.actor.Actor;
import sample.context.actor.type.ActorRoleType;
import sample.context.orm.JpqlBuilder;
import sample.context.orm.OrmMatchMode;
import sample.context.orm.OrmRepository;
import sample.model.DomainErrorKeys;
import sample.model.constraints.IdStr;
import sample.model.constraints.Name;
import sample.model.constraints.OutlineEmpty;
import sample.model.constraints.Password;
import sample.model.master.Login.RegLogin;
import sample.util.AppValidator;

/**
 * Representation of staffs.
 */
@Entity
@Data
public class Staff implements DomainEntity {

    /** staff ID */
    @Id
    @IdStr
    private String staffId;
    /** staff name */
    @Name
    private String name;
    /** staff role type (INTERNAL or ADMINISTRATOR) */
    private ActorRoleType roleType;

    public Actor actor() {
        return Actor.of(this.staffId, this.name, this.roleType);
    }

    /** Change staff information. */
    public Staff change(final OrmRepository rep, final ChgStaff param) {
        return rep.update(param.bind(this));
    }

    @Builder
    public static record ChgStaff(
            @Name String name) implements Dto {

        public Staff bind(final Staff current) {
            current.setName(name);
            return current;
        }
    }

    public static Optional<Staff> get(final OrmRepository rep, final String staffId) {
        return rep.get(Staff.class, staffId);
    }

    public static Staff load(final OrmRepository rep, final String staffId) {
        return rep.load(Staff.class, staffId);
    }

    public static List<Staff> find(final OrmRepository rep, final FindStaff param) {
        var jpql = JpqlBuilder.of("SELECT s FROM Staff s")
                .like(List.of("s.id", "s.name"), param.keyword, OrmMatchMode.ANYWHERE)
                .orderBy("s.staffId");
        return rep.tmpl().find(jpql.build(), jpql.args());
    }

    @Builder
    public static record FindStaff(
            @OutlineEmpty String keyword) implements Dto {
    }

    /**
     * Staff registration.
     * <p>
     * Login information will be registered at the same time.
     */
    public static Staff register(final OrmRepository rep, final PasswordEncoder encoder, final RegStaff param) {
        AppValidator.validate((v) -> {
            v.checkField(!get(rep, param.staffId).isPresent(), "staffId", DomainErrorKeys.DuplicateId);
        });
        Login.register(rep, encoder, param.createLogin());
        return rep.save(param.create());
    }

    @Builder
    public static record RegStaff(
            @IdStr String staffId,
            @Name String name,
            boolean administrator,
            /** Password (unhashed) */
            @Password String plainPassword) implements Dto {

        public Staff create() {
            var m = new Staff();
            m.setStaffId(staffId);
            m.setName(name);
            m.setRoleType(administrator ? ActorRoleType.ADMINISTRATOR : ActorRoleType.INTERNAL);
            return m;
        }

        public RegLogin createLogin() {
            return RegLogin.builder()
                    .actorId(staffId)
                    .roleType(administrator ? ActorRoleType.ADMINISTRATOR : ActorRoleType.INTERNAL)
                    .plainPassword(plainPassword)
                    .build();
        }
    }

}
