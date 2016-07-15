package sample.model.account;

import java.util.Optional;

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
 * 口座を表現します。
 * low: サンプル用に必要最低限の項目だけ
 */
@Entity
@Data
@EqualsAndHashCode(callSuper = false)
public class Account extends OrmActiveRecord<Account> {
    private static final long serialVersionUID = 1L;

    /** 口座ID */
    @Id
    @IdStr
    private String id;
    /** 口座名義 */
    @Name
    private String name;
    /** メールアドレス */
    @Email
    private String mail;
    /** 口座状態 */
    @NotNull
    @Enumerated(EnumType.STRING)
    private AccountStatusType statusType;

    public Actor actor() {
        return new Actor(id, name, ActorRoleType.User);
    }

    /** 口座に紐付くログイン情報を取得します。 */
    public Login loadLogin(final OrmRepository rep) {
        return Login.load(rep, id);
    }

    /** 口座を変更します。 */
    public Account change(final OrmRepository rep, final ChgAccount p) {
        return p.bind(this).update(rep);
    }

    /** 口座を取得します。 */
    public static Optional<Account> get(final OrmRepository rep, String id) {
        return rep.get(Account.class, id);
    }

    /** 有効な口座を取得します。 */
    public static Optional<Account> getValid(final OrmRepository rep, String id) {
        return get(rep, id).filter((acc) -> acc.getStatusType().valid());
    }

    /** 口座を取得します。(例外付) */
    public static Account load(final OrmRepository rep, String id) {
        return rep.load(Account.class, id);
    }

    /** 有効な口座を取得します。(例外付) */
    public static Account loadValid(final OrmRepository rep, String id) {
        return getValid(rep, id).orElseThrow(() -> new ValidationException("error.Account.loadValid"));
    }

    /** 
     * 口座の登録を行います。
     * <p>ログイン情報も同時に登録されます。
     */
    public static Account register(final OrmRepository rep, final PasswordEncoder encoder, RegAccount p) {
        Validator.validate((v) -> v.checkField(!get(rep, p.id).isPresent(), "id", ErrorKeys.DuplicateId));
        p.createLogin(encoder.encode(p.plainPassword)).save(rep);
        return p.create().save(rep);
    }
    
    public static PagingList<Account> find(OrmRepository rep) {
        return rep.tmpl().find(Account.class, (criteria) -> {
            return criteria.isNotNull("name");
        }, new Pagination(1, 2));
    }

    /** 登録パラメタ */
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
        /** パスワード(未ハッシュ) */
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

    /** 変更パラメタ */
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
