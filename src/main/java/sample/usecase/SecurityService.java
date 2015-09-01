package sample.usecase;

import java.util.*;

import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.context.annotation.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import sample.ValidationException.ErrorKeys;
import sample.context.security.SecurityActorFinder.*;
import sample.context.security.SecurityAuthConfig;
import sample.util.ConvertUtils;

/**
 * SpringSecurityのユーザアクセスコンポーネントを定義します。
 */
@Configuration
public class SecurityService {

	/** 一般利用者情報を提供します。(see SecurityActorFinder) */
	@Bean
	@ConditionalOnBean(SecurityAuthConfig.class)
	public SecurityUserService securityUserService(final AccountService service) {
		return new SecurityUserService() {
			/**
			 * 以下の手順で利用口座を特定します。
			 * <ul>
			 * <li>ログインID(全角は半角に自動変換)に合致するログイン情報があるか
			 * <li>口座IDに合致する有効な口座情報があるか
			 * </ul>
			 * <p>一般利用者には「ROLE_USER」の権限が自動で割り当てられます。
			 */
			@Override
			public ActorDetails loadUserByUsername(String username) throws UsernameNotFoundException {
				return (ActorDetails)Optional.ofNullable(username).map(ConvertUtils::zenkakuToHan).flatMap((loginId) ->
					service.getLoginByLoginId(loginId).flatMap((login) ->							
						service.getAccount(login.getId()).map((account) -> {
							List<GrantedAuthority> authorities = Arrays.asList(new GrantedAuthority[] {
								new SimpleGrantedAuthority("ROLE_USER") });
							return new ActorDetails(account.actor(), login.getPassword(), authorities);
						})
					)).orElseThrow(() -> new UsernameNotFoundException(ErrorKeys.Login));
			}
		};
	}
	
	/** 社内管理向けの利用者情報を提供します。(see SecurityActorFinder) */
	@Bean
	@ConditionalOnBean(SecurityAuthConfig.class)
	@ConditionalOnProperty(prefix = "extension.security.auth", name = "admin", matchIfMissing = false)
	public SecurityAdminService securityAdminService(final MasterAdminService service) {
		return new SecurityAdminService() {
			/**
			 * 以下の手順で社員を特定します。
			 * <ul>
			 * <li>社員ID(全角は半角に自動変換)に合致する社員情報があるか
			 * <li>社員情報に紐付く権限があるか
			 * </ul>
			 * <p>社員には「ROLE_ADMIN」の権限が自動で割り当てられます。
			 */
			@Override
			public ActorDetails loadUserByUsername(String username) throws UsernameNotFoundException {
				return (ActorDetails)Optional.ofNullable(username).map(ConvertUtils::zenkakuToHan).flatMap((staffId) ->
					service.getStaff(staffId).map((staff) -> {
						List<GrantedAuthority> authorities = new ArrayList<>(Arrays.asList(new GrantedAuthority[] {
							new SimpleGrantedAuthority("ROLE_ADMIN") }));
						service.findStaffAuthority(staffId).forEach((auth) ->
							authorities.add(new SimpleGrantedAuthority(auth.getAuthority())));
						return new ActorDetails(staff.actor(), staff.getPassword(), authorities);
					})).orElseThrow(() -> new UsernameNotFoundException(ErrorKeys.Login));
			}
		};
	}	
}
