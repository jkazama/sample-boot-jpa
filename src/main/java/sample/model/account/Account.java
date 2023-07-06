package sample.model.account;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import sample.context.DomainEntity;
import sample.context.Dto;
import sample.context.ErrorKeys;
import sample.context.ValidationException;
import sample.context.actor.Actor;
import sample.context.actor.type.ActorRoleType;
import sample.context.orm.OrmRepository;
import sample.model.DomainErrorKeys;
import sample.model.account.type.AccountStatusType;
import sample.model.constraints.IdStr;
import sample.model.constraints.MailAddress;
import sample.model.constraints.Name;
import sample.model.constraints.Password;
import sample.model.master.Login;
import sample.model.master.Login.RegLogin;
import sample.util.AppValidator;

/**
 * Represents an account.
 * low: Only the minimum required fields for the sample.
 */
@Entity
@Data
public class Account implements DomainEntity {

    /** account ID */
    @Id
    @IdStr
    private String accountId;
    /** account name */
    @Name
    private String name;
    @MailAddress
    private String mailAddress;
    @NotNull
    @Enumerated
    private AccountStatusType statusType;

    public Actor actor() {
        return Actor.of(accountId, name, ActorRoleType.USER);
    }

    public Account change(final OrmRepository rep, final ChgAccount param) {
        return rep.update(param.bind(this));
    }

    @Builder
    public static record ChgAccount(
            @IdStr String accountId,
            @Name String name,
            @MailAddress String mailAddress) implements Dto {

        public Account bind(final Account current) {
            current.setName(this.name);
            current.setMailAddress(this.mailAddress);
            return current;
        }
    }

    public static Optional<Account> get(final OrmRepository rep, String accountId) {
        return rep.get(Account.class, accountId);
    }

    /** Obtain a valid account. */
    public static Optional<Account> getValid(final OrmRepository rep, String accountId) {
        return get(rep, accountId).filter((v) -> v.getStatusType().isValid());
    }

    /** Returns a valid account. */
    public static Account load(final OrmRepository rep, String accountId) {
        return rep.load(Account.class, accountId);
    }

    /** Returns a valid account. */
    public static Account loadValid(final OrmRepository rep, String accountId) {
        return getValid(rep, accountId)
                .orElseThrow(() -> ValidationException.of(ErrorKeys.EntityNotFound, accountId));
    }

    /**
     * Register your account.
     * <p>
     * Login information will be registered at the same time.
     */
    public static Account register(
            final OrmRepository rep, final PasswordEncoder encoder, final RegAccount param) {
        AppValidator.validate((v) -> {
            v.checkField(
                    !get(rep, param.accountId).isPresent(), "accountId", DomainErrorKeys.DuplicateId);
        });
        Login.register(rep, encoder, param.createLogin());
        return rep.save(param.create());
    }

    @Builder
    public static record RegAccount(
            @IdStr String accountId,
            @Name String name,
            @MailAddress String mailAddress,
            /** Password (unhashed) */
            @Password String plainPassword) implements Dto {

        public Account create() {
            var m = new Account();
            m.setAccountId(this.accountId);
            m.setName(this.name);
            m.setMailAddress(this.mailAddress);
            m.setStatusType(AccountStatusType.NORMAL);
            return m;
        }

        public RegLogin createLogin() {
            return RegLogin.builder()
                    .actorId(accountId)
                    .roleType(ActorRoleType.USER)
                    .plainPassword(plainPassword)
                    .build();
        }
    }

}
