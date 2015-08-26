package sample.model.account;

import java.util.Optional;

import javax.persistence.*;

import org.springframework.security.crypto.password.PasswordEncoder;

import lombok.*;
import sample.context.Dto;
import sample.context.orm.*;
import sample.model.constraints.*;

/**
 * 口座ログインを表現します。
 * low: サンプル用に必要最低限の項目だけ
 */
@Entity
@Data
@ToString(callSuper = false, exclude = { "password"})
@EqualsAndHashCode(callSuper = false)
public class Login extends OrmActiveRecord<Login> {
	private static final long serialVersionUID = 1L;
	
	/** 口座ID */
	@Id
	@IdStr
	private String id;
	/** ログインID */
	private String loginId;
	/** パスワード(暗号化済) */
	@Password
	private String password;
		
	/** パスワードを変更します。 */
	public Login change(final OrmRepository rep, final PasswordEncoder encoder, final ChgPassowrd p) {
		return p.bind(this, encoder.encode(p.plainPassword)).update(rep);
	}

	/** ログイン情報を取得します。 */
	public static Optional<Login> get(final OrmRepository rep, String id) {
		return rep.get(Login.class, id);
	}
	
	/** ログイン情報を取得します。(例外付) */
	public static Login load(final OrmRepository rep, String id) {
		return rep.load(Login.class, id);
	}
	
	/** パスワード変更パラメタ */
	@Value
	public static class ChgLoginId implements Dto {
		private static final long serialVersionUID = 1l;
		@IdStr
		private String loginId;
		public Login bind(final Login m, String password) {
			m.setLoginId(password);
			return m;
		}
	}
	
	/** パスワード変更パラメタ */
	@Value
	public static class ChgPassowrd implements Dto {
		private static final long serialVersionUID = 1l;
		@Password
		private String plainPassword;
		public Login bind(final Login m, String password) {
			m.setPassword(password);
			return m;
		}
	}

}
