package sample.usecase;

import java.util.*;

import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.context.annotation.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import sample.ValidationException.ErrorKeys;
import sample.context.security.SecurityActorFinder.*;
import sample.context.security.SecurityConfigurer;
import sample.util.ConvertUtils;

/**
 * Define the user access component of Spring Security.
 */
@Configuration
public class SecurityService {

    /** General user information. */
    @Bean
    @ConditionalOnBean(SecurityConfigurer.class)
    public SecurityUserService securityUserService(final AccountService service) {
        return new SecurityUserService() {
            /**
             * Identify a use account in the next procedure.
             * <ul>
             * <li>Is there login information to accord in ID.
             * <li>Is there effective account information in account ID.
             * </ul>
             * <p>Authority of "ROLE_USER" is automatically assigned to a general user.
             */
            @Override
            public ActorDetails loadUserByUsername(String username) throws UsernameNotFoundException {
                return (ActorDetails) Optional.ofNullable(username).map(ConvertUtils::zenkakuToHan)
                        .flatMap((loginId) -> service.getLoginByLoginId(loginId)
                                .flatMap((login) -> service.getAccount(login.getId()).map((account) -> {
                    List<GrantedAuthority> authorities = Arrays.asList(new GrantedAuthority[] {
                            new SimpleGrantedAuthority("ROLE_USER") });
                    return new ActorDetails(account.actor(), login.getPassword(), authorities);
                }))).orElseThrow(() -> new UsernameNotFoundException(ErrorKeys.Login));
            }
        };
    }

    /** User information for staff. */
    @Bean
    @ConditionalOnBean(SecurityConfigurer.class)
    @ConditionalOnProperty(prefix = "extension.security.auth", name = "admin", matchIfMissing = false)
    public SecurityAdminService securityAdminService(final MasterAdminService service) {
        return new SecurityAdminService() {
            /**
             * Identify a staff account in the next procedure.
             * <ul>
             * <li>Is there staff information to accord in ID.
             * <li>Is there authority to have a string in staff information.
             * </ul>
             * <p>Authority of "ROLE_ADMIN" is automatically assigned to a staff.
             */
            @Override
            public ActorDetails loadUserByUsername(String username) throws UsernameNotFoundException {
                return (ActorDetails) Optional.ofNullable(username).map(ConvertUtils::zenkakuToHan)
                        .flatMap((staffId) -> service.getStaff(staffId).map((staff) -> {
                    List<GrantedAuthority> authorities = new ArrayList<>(Arrays.asList(new GrantedAuthority[] {
                            new SimpleGrantedAuthority("ROLE_ADMIN") }));
                    service.findStaffAuthority(staffId)
                            .forEach((auth) -> authorities.add(new SimpleGrantedAuthority(auth.getAuthority())));
                    return new ActorDetails(staff.actor(), staff.getPassword(), authorities);
                })).orElseThrow(() -> new UsernameNotFoundException(ErrorKeys.Login));
            }
        };
    }
}
