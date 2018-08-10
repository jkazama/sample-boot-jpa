package sample.context.security;

import java.io.IOException;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.eclipse.collections.impl.list.fixed.ArrayAdapter;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.autoconfigure.web.servlet.*;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.*;
import org.springframework.security.config.annotation.web.builders.*;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.*;
import org.springframework.security.web.authentication.logout.*;
import org.springframework.web.filter.*;

import lombok.*;
import sample.ValidationException.ErrorKeys;
import sample.context.actor.ActorSession;
import sample.context.security.SecurityActorFinder.*;

/**
 * Spring Security (the certification / authorization) General Preferences.
 * <p>The certification defines it by conventional approach using HttpSession not the basic certification.
 * <p>The setting uses an original thing, Use a thing of the "extension.security" prefix not "security" prefix.
 * <p>low: This component destroy it with this sample,
 * but the CSRF correspondence, please examine the appropriate use every project.
 */
@Setter
@Getter
public class SecurityConfigurer extends WebSecurityConfigurerAdapter {

    @Autowired
    private SecurityProperties props;
    @Autowired
    @Lazy
    private SecurityActorFinder actorFinder;
    @Autowired
    @Lazy
    private SecurityEntryPoint entryPoint;
    @Autowired
    @Lazy
    private LoginHandler loginHandler;
    @Autowired
    private ActorSession actorSession;
    @Autowired(required = false)
    private CorsFilter corsFilter;
    @Autowired(required = false)
    private SecurityFilters filters;
    
    @Autowired
    @Qualifier(DispatcherServletAutoConfiguration.DEFAULT_DISPATCHER_SERVLET_REGISTRATION_BEAN_NAME)
    private DispatcherServletRegistrationBean dispatcherServletRegistration;

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().mvcMatchers(
                ArrayAdapter.adapt(props.auth().getIgnorePath())
                    .collect(dispatcherServletRegistration::getRelativePath)
                    .toArray(new String[0]));
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // Target URL
        http
            .authorizeRequests()
            .mvcMatchers(props.auth().getExcludesPath()).permitAll();
        http
            .csrf().disable()
            .authorizeRequests()
            .mvcMatchers(props.auth().getPathAdmin()).hasRole("ADMIN")
            .mvcMatchers(props.auth().getPath()).hasRole("USER");
        
        // Common
        http
            .exceptionHandling().authenticationEntryPoint(entryPoint);
        http
            .sessionManagement()
            .maximumSessions(props.auth().getMaximumSessions())
            .and()
            .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED);
        http
            .addFilterAfter(new ActorSessionFilter(actorSession), UsernamePasswordAuthenticationFilter.class);
        if (corsFilter != null) {
            http.addFilterBefore(corsFilter, LogoutFilter.class);
        }
        if (filters != null) {
            for (Filter filter : filters.filters()) {
                http.addFilterAfter(filter, ActorSessionFilter.class);
            }
        }

        // Login / Logout
        http
            .formLogin().loginPage(props.auth().getLoginPath())
            .usernameParameter(props.auth().getLoginKey()).passwordParameter(props.auth().getPasswordKey())
            .successHandler(loginHandler).failureHandler(loginHandler)
            .permitAll()
            .and()
            .logout().logoutUrl(props.auth().getLogoutPath())
            .logoutSuccessHandler(loginHandler)
            .permitAll();
    }

    public static class SecurityProvider implements AuthenticationProvider {
        @Autowired
        private SecurityActorFinder actorFinder;
        @Autowired
        @Lazy
        private PasswordEncoder encoder;

        @Override
        public Authentication authenticate(Authentication authentication) throws AuthenticationException {
            if (authentication.getPrincipal() == null ||
                    authentication.getCredentials() == null) {
                throw new BadCredentialsException("You failed in the login certification");
            }
            SecurityActorService service = actorFinder.detailsService();
            UserDetails details = service.loadUserByUsername(authentication.getPrincipal().toString());
            String presentedPassword = authentication.getCredentials().toString();
            if (!encoder.matches(presentedPassword, details.getPassword())) {
                throw new BadCredentialsException("You failed in the login certification");
            }
            UsernamePasswordAuthenticationToken ret = new UsernamePasswordAuthenticationToken(
                    authentication.getName(), "", details.getAuthorities());
            ret.setDetails(details);
            return ret;
        }

        @Override
        public boolean supports(Class<?> authentication) {
            return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
        }
    }

    /**
     * A custom entry point of Spring Security.
     */
    public static class SecurityEntryPoint implements AuthenticationEntryPoint {
        @Autowired
        private MessageSource msg;

        @Override
        public void commence(HttpServletRequest request, HttpServletResponse response,
                AuthenticationException authException) throws IOException, ServletException {
            if (response.isCommitted()) {
                return;
            }
            if (authException instanceof InsufficientAuthenticationException) {
                String message = msg.getMessage(ErrorKeys.AccessDenied, new Object[0], Locale.getDefault());
                writeReponseEmpty(response, HttpServletResponse.SC_FORBIDDEN, message);
            } else {
                String message = msg.getMessage(ErrorKeys.Authentication, new Object[0], Locale.getDefault());
                writeReponseEmpty(response, HttpServletResponse.SC_UNAUTHORIZED, message);
            }
        }
        
        private void writeReponseEmpty(HttpServletResponse response, int status, String message) throws IOException {
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setStatus(status);
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"message\": \"" + message + "\"}");
        }
    }

    /**
     * ServletFilter which relates ActorSession with SpringSecurity certification information.
     * <p>When dummyLogin is effective, this filter always connect Authentication with SecurityContextHolder.
     */
    @AllArgsConstructor
    public static class ActorSessionFilter extends GenericFilterBean {
        private ActorSession actorSession;

        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                throws IOException, ServletException {
            Optional<Authentication> authOpt = SecurityActorFinder.authentication();
            if (authOpt.isPresent() && authOpt.get().getDetails() instanceof ActorDetails) {
                ActorDetails details = (ActorDetails) authOpt.get().getDetails();
                actorSession.bind(details.actor());
                try {
                    chain.doFilter(request, response);
                } finally {
                    actorSession.unbind();
                }
            } else {
                actorSession.unbind();
                chain.doFilter(request, response);
            }
        }
    }

    @Getter
    @Setter
    public static class LoginHandler
            implements AuthenticationSuccessHandler, AuthenticationFailureHandler, LogoutSuccessHandler {
        @Autowired
        private SecurityProperties props;

        @Override
        public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                Authentication authentication) throws IOException, ServletException {
            Optional.ofNullable((ActorDetails) authentication.getDetails()).ifPresent(
                    (detail) -> detail.bindRequestInfo(request));
            if (response.isCommitted()) {
                return;
            }
            writeReponseEmpty(response, HttpServletResponse.SC_OK);
        }

        @Override
        public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                AuthenticationException exception) throws IOException, ServletException {
            if (response.isCommitted()) {
                return;
            }
            writeReponseEmpty(response, HttpServletResponse.SC_BAD_REQUEST);
        }

        @Override
        public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
                Authentication authentication)
                        throws IOException, ServletException {
            if (response.isCommitted()) {
                return;
            }
            writeReponseEmpty(response, HttpServletResponse.SC_OK);
        }

        private void writeReponseEmpty(HttpServletResponse response, int status) throws IOException {
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setStatus(status);
            response.getWriter().write("{}");
        }
    }

}
