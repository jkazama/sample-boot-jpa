package sample.model.master;

import java.util.*;

import javax.persistence.*;

import org.hibernate.criterion.MatchMode;
import org.springframework.security.crypto.password.PasswordEncoder;

import lombok.*;
import sample.ValidationException;
import sample.context.Dto;
import sample.context.actor.Actor;
import sample.context.actor.Actor.ActorRoleType;
import sample.context.orm.*;
import sample.context.security.SecurityActorFinder;
import sample.context.security.SecurityActorFinder.ActorDetails;
import sample.model.constraints.*;
import sample.util.Validator;

/**
 * 社員を表現します。
 */
@Entity
@Data
@ToString(callSuper = false, exclude = { "password"})
@EqualsAndHashCode(callSuper = false)
public class Staff extends OrmActiveRecord<Staff> {
	private static final long serialVersionUID = 1l;

	/** ID */
	@Id
	@IdStr
	private String id;
	/** 名前 */
	@Name
	private String name;
	/** パスワード(暗号化済) */
	@Password
	private String password;

	public Actor actor() {
		return new Actor(id, name, ActorRoleType.INTERNAL);
	}
	
	/** パスワードを変更します。 */
	public Staff change(final OrmRepository rep, final PasswordEncoder encoder, final ChgPassowrd p) {
		return p.bind(this, encoder.encode(p.plainPassword)).update(rep);
	}

	/** 社員情報を変更します。 */
	public Staff change(final OrmRepository rep, ChgStaff p) {
		return p.bind(this).update(rep);
	}
	
	/** 社員を取得します。 */
	public static Optional<Staff> get(final OrmRepository rep, final String id) {
		return rep.get(Staff.class, id);
	}
	
	/** 社員を取得します。(例外付) */
	public static Staff load(final OrmRepository rep, final String id) {
		return rep.load(Staff.class, id);
	}
	
	/** ログイン情報を取得します。 */
	public static LoginStaff loadLoginStaff(final OrmRepository rep) {
		ActorDetails actorDetails = SecurityActorFinder.actorDetails().orElseThrow(() -> new ValidationException("ログイン状態ではありません"));
		Actor actor = actorDetails.actor();
		return new LoginStaff(actor.getId(), actor.getName(), actorDetails.getAuthorityIds());
	}

	/** 社員を検索します。 */
	public static List<Staff> find(final OrmRepository rep, final FindStaff p) {
		return rep.tmpl().find(Staff.class, (criteria) ->
			criteria.like(new String[] { "id", "name" }, p.keyword, MatchMode.ANYWHERE)
			.sort("id").result());
	}

	/** 社員の登録を行います。 */
	public static Staff register(final OrmRepository rep, final PasswordEncoder encoder, RegStaff p) {
		Validator.validate((v) -> v.checkField(get(rep, p.id).isPresent(), "id", "error.common.duplicateCode"));
		return p.create(encoder.encode(p.plainPassword)).save(rep);
	}

	/** クライアント利用用途に絞ったパラメタ */
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class LoginStaff {
		private String id;
		private String name;
		private Collection<String> authorities;
	}

	/** 検索パラメタ */
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class FindStaff implements Dto {
		private static final long serialVersionUID = 1l;
		@OutlineEmpty
		private String keyword;
	}
	
	/** 登録パラメタ */
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class RegStaff implements Dto {
		private static final long serialVersionUID = 1l;

		@IdStr
		private String id;
		@Name
		private String name;
		/** パスワード(未ハッシュ) */
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

	/** 変更パラメタ */
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
	
	/** パスワード変更パラメタ */
	@Value
	public static class ChgPassowrd implements Dto {
		private static final long serialVersionUID = 1l;
		@Password
		private String plainPassword;
		public Staff bind(final Staff m, String password) {
			m.setPassword(password);
			return m;
		}
	}
	
}
