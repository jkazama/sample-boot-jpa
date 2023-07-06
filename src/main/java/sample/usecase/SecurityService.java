package sample.usecase;

import java.util.Optional;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import lombok.RequiredArgsConstructor;
import sample.context.ErrorKeys;
import sample.context.ValidationException;
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
public class SecurityService implements UserDetailsService {
    private final OrmRepository rep;
    private final PlatformTransactionManager txm;

    /** {@inheritDoc} */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var request = RequestContextHolder.currentRequestAttributes();
        ActorRoleType roleType = Optional.ofNullable(request.getAttribute("roleType", RequestAttributes.SCOPE_REQUEST))
                .filter(v -> ActorRoleType.has(v.toString()))
                .map(v -> ActorRoleType.valueOf(v.toString()))
                .orElseThrow(() -> ValidationException.of(ErrorKeys.Login));
        return TxTemplate.of(txm).readOnly().tx(() -> {
            return Login.getByLoginId(rep, roleType, username)
                    .map(login -> {
                        return login.getRoleType().isUser()
                                ? Account.load(rep, login.getActorId()).actor()
                                : Staff.load(rep, login.getActorId()).actor();
                    })
                    .orElseThrow(() -> ValidationException.of(ErrorKeys.Login));
        });
    }

}
