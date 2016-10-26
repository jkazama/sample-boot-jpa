package sample.model.account;

import java.util.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import org.springframework.security.crypto.password.PasswordEncoder;

import lombok.*;
import sample.ValidationException;
import sample.ValidationException.ErrorKeys;
import sample.context.Dto;
import sample.context.actor.Actor;
import sample.context.actor.Actor.ActorRoleType;
import sample.context.orm.*;
import sample.model.account.type.AccountStatusType;
import sample.model.constraints.*;
import sample.util.Validator;

/**
 * Account.
 * low: The minimum columns with this sample.
 */
@Entity
@Data
@EqualsAndHashCode(callSuper = false)
public class Account extends OrmActiveRecord<Account> {
    private static final long serialVersionUID = 1L;

    @Id
    @IdStr
    private String id;
    @Name
    private String name;
    @Email
    private String mail;
    @NotNull
    @Enumerated(EnumType.STRING)
    private AccountStatusType statusType;

    public Actor actor() {
        return new Actor(id, name, ActorRoleType.User);
    }

    public Login loadLogin(final OrmRepository rep) {
        return Login.load(rep, id);
    }

    public Account change(final OrmRepository rep, final ChgAccount p) {
        return p.bind(this).update(rep);
    }

    public static Optional<Account> get(final OrmRepository rep, String id) {
        return rep.get(Account.class, id);
    }

    public static Optional<Account> getValid(final OrmRepository rep, String id) {
        return get(rep, id).filter((acc) -> acc.getStatusType().valid());
    }

    public static Account load(final OrmRepository rep, String id) {
        return rep.load(Account.class, id);
    }

    public static Account loadValid(final OrmRepository rep, String id) {
        return getValid(rep, id).orElseThrow(() -> new ValidationException("error.Account.loadValid"));
    }

    /** 
     * Register account.
     * <p>The login information is registered at the same time.
     */
    public static Account register(final OrmRepository rep, final PasswordEncoder encoder, final RegAccount p) {
        Validator.validate((v) -> v.checkField(!get(rep, p.id).isPresent(), "id", ErrorKeys.DuplicateId));
        p.createLogin(encoder.encode(p.plainPassword)).save(rep);
        return p.create().save(rep);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegAccount implements Dto {
        private static final long serialVersionUID = 1l;

        @IdStr
        private String id;
        @Name
        private String name;
        @Email
        private String mail;
        /** password (plain) */
        @Password
        private String plainPassword;

        public Account create() {
            Account m = new Account();
            m.setId(id);
            m.setName(name);
            m.setMail(mail);
            m.setStatusType(AccountStatusType.Normal);
            return m;
        }

        public Login createLogin(String password) {
            Login m = new Login();
            m.setId(id);
            m.setLoginId(id);
            m.setPassword(password);
            return m;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChgAccount implements Dto {
        private static final long serialVersionUID = 1l;
        @Name
        private String name;
        @Email
        private String mail;

        public Account bind(final Account m) {
            m.setName(name);
            m.setMail(mail);
            return m;
        }
    }

}
