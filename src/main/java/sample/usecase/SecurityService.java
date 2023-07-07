package sample.usecase;

import java.util.Optional;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;

import lombok.RequiredArgsConstructor;
import sample.context.ErrorKeys;
import sample.context.actor.type.ActorRoleType;
import sample.context.orm.OrmRepository;
import sample.context.orm.TxTemplate;
import sample.model.account.Account;
import sample.model.master.Login;
import sample.model.master.Staff;

/**
 * Define the user access component of SpringSecurity.
 */
@Service
@RequiredArgsConstructor
public class SecurityService implements UserDetailsService, AuthenticationProvider {
    private final OrmRepository rep;
    private final PlatformTransactionManager txm;
    private final PasswordEncoder encoder;

    /** {@inheritDoc} */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        int idx = username.indexOf("-");
        String roleTypeStr = "USER";
        String loginId;
        if (idx == -1) {
            loginId = username;
        } else {
            roleTypeStr = username.substring(0, idx);
            loginId = username.substring(idx + 1);
        }
        ActorRoleType roleType = Optional.of(roleTypeStr)
                .filter(v -> ActorRoleType.has(v.toString()))
                .map(v -> ActorRoleType.valueOf(v.toString()))
                .orElseThrow(() -> new UsernameNotFoundException(ErrorKeys.Login));
        return TxTemplate.of(txm).readOnly().tx(() -> {
            return Login.getByLoginId(rep, roleType, loginId)
                    .map(login -> {
                        return login.getRoleType().isUser()
                                ? Account.load(rep, login.getActorId()).actor()
                                : Staff.load(rep, login.getActorId()).actor();
                    })
                    .orElseThrow(() -> new UsernameNotFoundException(ErrorKeys.Login));
        });
    }

    /** {@inheritDoc} */
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (authentication.getPrincipal() == null ||
                authentication.getCredentials() == null) {
            throw new BadCredentialsException(ErrorKeys.Login);
        }
        UserDetails details = this.loadUserByUsername(authentication.getPrincipal().toString());
        String presentedPassword = authentication.getCredentials().toString();
        if (!encoder.matches(presentedPassword, details.getPassword())) {
            throw new BadCredentialsException(ErrorKeys.Login);
        }
        return new UsernamePasswordAuthenticationToken(details, "", details.getAuthorities());
    }

    /** {@inheritDoc} */
    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }

}
