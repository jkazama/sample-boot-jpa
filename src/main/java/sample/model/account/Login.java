package sample.model.account;

import java.util.Optional;

import javax.persistence.*;

import org.springframework.security.crypto.password.PasswordEncoder;

import lombok.*;
import sample.ValidationException.ErrorKeys;
import sample.context.Dto;
import sample.context.orm.*;
import sample.model.constraints.*;

/**
 * Account login.
 * low: The minimum columns with this sample.
 */
@Entity
@Data
@ToString(callSuper = false, exclude = { "password" })
@EqualsAndHashCode(callSuper = false)
public class Login extends OrmActiveRecord<Login> {
    private static final long serialVersionUID = 1L;

    /** account ID */
    @Id
    @IdStr
    private String id;
    private String loginId;
    /** password (encrypted) */
    @Password
    private String password;

    public Login change(final OrmRepository rep, final ChgLoginId p) {
        boolean exists = rep.tmpl().get("from Login where id<>?1 and loginId=?2", id, p.loginId).isPresent();
        validate((v) -> v.checkField(!exists, "loginId", ErrorKeys.DuplicateId));
        return p.bind(this).update(rep);
    }

    public Login change(final OrmRepository rep, final PasswordEncoder encoder, final ChgPassword p) {
        return p.bind(this, encoder.encode(p.plainPassword)).update(rep);
    }

    public static Optional<Login> get(final OrmRepository rep, String id) {
        return rep.get(Login.class, id);
    }

    public static Optional<Login> getByLoginId(final OrmRepository rep, String loginId) {
        return Optional.ofNullable(loginId).flatMap(lid -> rep.tmpl().get("from Login where loginId=?1", lid));
    }

    public static Login load(final OrmRepository rep, String id) {
        return rep.load(Login.class, id);
    }

    @Value
    public static class ChgLoginId implements Dto {
        private static final long serialVersionUID = 1l;
        @IdStr
        private String loginId;

        public Login bind(final Login m) {
            m.setLoginId(loginId);
            return m;
        }
    }

    @Value
    public static class ChgPassword implements Dto {
        private static final long serialVersionUID = 1l;
        @Password
        private String plainPassword;

        public Login bind(final Login m, String password) {
            m.setPassword(password);
            return m;
        }
    }

}
