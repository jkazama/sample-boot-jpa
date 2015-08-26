package sample.usecase;

import java.util.*;

import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.context.annotation.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import sample.context.security.SecurityHandler;
import sample.context.security.SecurityActorFinder.*;
import sample.model.account.*;
import sample.model.master.Staff;
import sample.util.ConvertUtils;

/**
 * SpringSecurityのユーザアクセスコンポーネントを定義します。
 */
@Configuration
public class SecurityService {

	/** 一般利用者情報を提供します。(see SecurityActorFinder) */
	@Bean
	@ConditionalOnBean(SecurityHandler.class)
	public SecurityUserService securityUserService(final AccountService service) {
		return new SecurityUserService() {
			@Override
			public ActorDetails loadUserByUsername(String username) throws UsernameNotFoundException {
				if (username == null) {
					throw new UsernameNotFoundException("error.Login");
				}
				String accountId = ConvertUtils.zenkakuToHan(username);
				Account account = service.getAccount(accountId).orElseThrow(() ->
					new UsernameNotFoundException("error.Login"));
				Login login = service.loadLogin(accountId);
				List<GrantedAuthority> authorities = Arrays.asList(new GrantedAuthority[] {
						new SimpleGrantedAuthority("ROLE_USER") });
				return new ActorDetails(account.actor(), login.getPassword(), authorities);
			}
		};
	}
	
	/** 社内管理向けの利用者情報を提供します。(see SecurityActorFinder) */
	@Bean
	@ConditionalOnBean(SecurityHandler.class)
	@ConditionalOnProperty(prefix = "extension.security", name = "admin", matchIfMissing = false)
	public SecurityAdminService securityAdminService(final MasterAdminService service) {
		return new SecurityAdminService() {
			@Override
			public ActorDetails loadUserByUsername(String username) throws UsernameNotFoundException {
				if (username == null) {
					throw new UsernameNotFoundException("error.Login");
				}
				String staffId = ConvertUtils.zenkakuToHan(username);
				Staff staff = service.getStaff(staffId).orElseThrow(() ->
					new UsernameNotFoundException("error.Login"));
				List<GrantedAuthority> authorities = Arrays.asList(new GrantedAuthority[] {
					new SimpleGrantedAuthority("ROLE_ADMIN") });
				service.findStaffAuthority(staffId).forEach((auth) ->
					authorities.add(new SimpleGrantedAuthority(auth.getAuthority())));
				return new ActorDetails(staff.actor(), staff.getPassword(), authorities);
			}
		};
	}	
}
