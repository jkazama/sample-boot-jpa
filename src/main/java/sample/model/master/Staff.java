package sample.model.master;

import java.util.*;

import javax.persistence.*;

import org.hibernate.criterion.MatchMode;
import org.springframework.security.crypto.password.PasswordEncoder;

import lombok.*;
import sample.ValidationException.ErrorKeys;
import sample.context.Dto;
import sample.context.actor.Actor;
import sample.context.actor.Actor.ActorRoleType;
import sample.context.orm.*;
import sample.model.constraints.*;
import sample.util.Validator;

/**
 * Staff of the service company.
 */
@Entity
@Data
@ToString(callSuper = false, exclude = { "password" })
@EqualsAndHashCode(callSuper = false)
public class Staff extends OrmActiveRecord<Staff> {
    private static final long serialVersionUID = 1l;

    @Id
    @IdStr
    private String id;
    @Name
    private String name;
    /** password (encrypted) */
    @Password
    private String password;

    public Actor actor() {
        return new Actor(id, name, ActorRoleType.Internal);
    }

    public Staff change(final OrmRepository rep, final PasswordEncoder encoder, final ChgPassword p) {
        return p.bind(this, encoder.encode(p.plainPassword)).update(rep);
    }

    public Staff change(final OrmRepository rep, ChgStaff p) {
        return p.bind(this).update(rep);
    }

    public static Optional<Staff> get(final OrmRepository rep, final String id) {
        return rep.get(Staff.class, id);
    }

    public static Staff load(final OrmRepository rep, final String id) {
        return rep.load(Staff.class, id);
    }

    public static List<Staff> find(final OrmRepository rep, final FindStaff p) {
        return rep.tmpl().find(Staff.class,
                (criteria) -> criteria.like(new String[] { "id", "name" }, p.keyword, MatchMode.ANYWHERE)
                        .sort("id").result());
    }

    public static Staff register(final OrmRepository rep, final PasswordEncoder encoder, RegStaff p) {
        Validator.validate((v) -> v.checkField(!get(rep, p.id).isPresent(), "id", ErrorKeys.DuplicateId));
        return p.create(encoder.encode(p.plainPassword)).save(rep);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FindStaff implements Dto {
        private static final long serialVersionUID = 1l;
        @OutlineEmpty
        private String keyword;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegStaff implements Dto {
        private static final long serialVersionUID = 1l;

        @IdStr
        private String id;
        @Name
        private String name;
        /** password (plain) */
        @Password
        private String plainPassword;

        public Staff create(String password) {
            Staff m = new Staff();
            m.setId(id);
            m.setName(name);
            m.setPassword(password);
            return m;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChgStaff implements Dto {
        private static final long serialVersionUID = 1l;
        @Name
        private String name;

        public Staff bind(final Staff m) {
            m.setName(name);
            return m;
        }
    }

    @Value
    public static class ChgPassword implements Dto {
        private static final long serialVersionUID = 1l;
        @Password
        private String plainPassword;

        public Staff bind(final Staff m, String password) {
            m.setPassword(password);
            return m;
        }
    }

}
