package sample.context.security;

import java.util.*;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.*;

import lombok.Setter;
import sample.context.actor.Actor;

/**
 * Return user info to be targeted for the certification / authorization used in Spring Security.
 */
@Setter
public class SecurityActorFinder {

    @Autowired
    private SecurityProperties props;
    @Autowired
    @Lazy
    private SecurityUserService userService;
    @Autowired(required = false)
    @Lazy
    private SecurityAdminService adminService;

    /** Return UserDetailService depending on a current process state. */
    public SecurityActorService detailsService() {
        return props.auth().isAdmin() ? adminService() : userService;
    }

    private SecurityAdminService adminService() {
        return Optional.ofNullable(adminService)
                .orElseThrow(() -> new IllegalStateException("SecurityAdminServiceをコンテナへBean登録してください。"));
    }

    /**
     * Return effective certification information.
     */
    public static Optional<Authentication> authentication() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication());
    }

    /**
     * Return effective certification information.
     * <p>When you want to take user login information, please use this.
     */
    public static Optional<ActorDetails> actorDetails() {
        return authentication().map((auth) -> {
            return (auth.getDetails() instanceof ActorDetails) ? (ActorDetails) auth.getDetails() : null;
        });
    }

    /**
     * The user info that is used by the certification / authorization.
     * <p>It is peculiar to a project and customizes it.
     */
    public static class ActorDetails implements UserDetails {
        private static final long serialVersionUID = 1L;
        /** The login user information */
        private Actor actor;
        /** Certification password (encrypted) */
        private String password;
        /** List of possession authority of the user */
        private Collection<GrantedAuthority> authorities;

        public ActorDetails(Actor actor, String password, Collection<GrantedAuthority> authorities) {
            this.actor = actor;
            this.password = password;
            this.authorities = authorities;
        }

        public ActorDetails bindRequestInfo(HttpServletRequest request) {
            //low: Check the header if you think about L/B way properly
            actor.setSource(request.getRemoteAddr());
            return this;
        }

        public Actor actor() {
            return actor;
        }

        @Override
        public String getUsername() {
            return actor.getId();
        }

        @Override
        public String getPassword() {
            return password;
        }

        @Override
        public boolean isAccountNonExpired() {
            return true;
        }

        @Override
        public boolean isAccountNonLocked() {
            return true;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }

        @Override
        public boolean isEnabled() {
            return true;
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return authorities;
        }

        public Collection<String> getAuthorityIds() {
            return authorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());
        }

    }

    /** UserDetailsService adapted in Actor */
    public static interface SecurityActorService extends UserDetailsService {
        /**
         * Return user info for the certification / authorization to the cause by a given login ID.
         * @see org.springframework.security.core.userdetails.UserDetailsService#loadUserByUsername(java.lang.String)
         */
        @Override
        public abstract ActorDetails loadUserByUsername(String username) throws UsernameNotFoundException;
    }

    /** I/F for general users */
    public static interface SecurityUserService extends SecurityActorService {
    }

    /** I/F for admin users */
    public static interface SecurityAdminService extends SecurityActorService {
    }

}
