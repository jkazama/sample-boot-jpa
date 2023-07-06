package sample.model.master;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import sample.context.DomainEntity;
import sample.context.Dto;
import sample.context.actor.type.ActorRoleType;
import sample.context.orm.OrmRepository;
import sample.model.DomainErrorKeys;
import sample.model.constraints.IdStr;
import sample.model.constraints.Password;
import sample.model.master.Login.LoginId;
import sample.util.AppValidator;

/**
 * Represents an actor (account or staff) login.
 * low: Only the minimum required fields for the sample.
 */
@Entity
@IdClass(LoginId.class)
@Data
@ToString(exclude = { "password" })
public class Login implements DomainEntity {

    /** actor ID */
    @Id
    @IdStr
    private String actorId;
    /** actor role type */
    @Id
    @Enumerated
    private ActorRoleType roleType;
    /** login ID */
    private String loginId;
    /** Password (hashed) */
    @NotBlank
    private String password;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor(staticName = "of")
    public static class LoginId implements Dto {
        @IdStr
        private String actorId;
        @NotNull
        private ActorRoleType actorRoleType;
    }

    /**
     * Change login Id.
     * <p>
     * Perform a format check of the login ID before calling.
     */
    public Login changeLoginId(final OrmRepository rep, String loginId) {
        AppValidator.validate((v) -> {
            var jpqlExists = "SELECT l FROM Login l WHERE l.actorId<>?1 AND l.roleType=?2 AND l.loginId=?3";
            boolean exists = rep.tmpl().get(jpqlExists, this.actorId, roleType, loginId).isPresent();
            v.checkField(!exists, "loginId", DomainErrorKeys.DuplicateId);
        });
        this.setLoginId(loginId);
        return rep.update(this);
    }

    /**
     * Change password.
     * <p>
     * Perform a format check of the password before calling.
     */
    public Login changePassword(
            final OrmRepository rep, final PasswordEncoder encoder, String plainPassword) {
        this.setPassword(encoder.encode(plainPassword));
        return rep.update(this);
    }

    public static Optional<Login> get(final OrmRepository rep, LoginId id) {
        return rep.get(Login.class, id);
    }

    public static Optional<Login> getByLoginId(final OrmRepository rep, ActorRoleType roleType, String loginId) {
        var jpql = "SELECT l FROM Login l WHERE l.roleType=?1 AND l.loginId=?2";
        return rep.tmpl().get(jpql, roleType, loginId);
    }

    public static Login load(final OrmRepository rep, LoginId id) {
        return rep.load(Login.class, id);
    }

    public static Login register(final OrmRepository rep, final PasswordEncoder encoder, final RegLogin param) {
        AppValidator.validate(v -> {
            var duplicate = getByLoginId(rep, param.roleType, param.loginId);
            v.verifyField(duplicate.isEmpty(), "loginId", DomainErrorKeys.DuplicateId);
        });
        return rep.save(param.create(encoder.encode(param.plainPassword)));
    }

    @Builder
    public static record RegLogin(
            @IdStr String actorId,
            @NotNull ActorRoleType roleType,
            String loginId,
            @Password String plainPassword) implements Dto {

        public Login create(String password) {
            var m = new Login();
            m.setActorId(actorId);
            m.setRoleType(roleType);
            m.setLoginId(loginId);
            m.setPassword(password);
            return m;
        }
    }

}
