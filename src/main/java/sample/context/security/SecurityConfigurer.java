package sample.context.security;

import java.io.IOException;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;
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
 * Spring Security(認証/認可)全般の設定を行います。
 * <p>認証はベーシック認証ではなく、HttpSessionを用いた従来型のアプローチで定義しています。
 * <p>設定はパターンを決め打ちしている関係上、既存の定義ファイルをラップしています。
 * securityプリフィックスではなくextension.securityプリフィックスのものを利用してください。
 * <p>low: HttpSessionを利用しているため、横スケールする際に問題となります。その際は上位のL/Bで制御するか、
 * SpringSession(HttpSessionの実装をRedis等でサポート)を利用する事でコード変更無しに対応が可能となります。
 * <p>low: 本サンプルでは無効化していますが、CSRF対応はプロジェクト毎に適切な利用を検討してください。
 */
@Setter
@Getter
public class SecurityConfigurer extends WebSecurityConfigurerAdapter {

    /** Spring Boot のサーバ情報 */
    @Autowired
    private ServerProperties serverProps;
    /** 拡張セキュリティ情報 */
    @Autowired
    private SecurityProperties props;
    /** 認証/認可利用者サービス */
    @Autowired
    @Lazy
    private SecurityActorFinder actorFinder;
    /** カスタムエントリポイント(例外対応) */
    @Autowired
    @Lazy
    private SecurityEntryPoint entryPoint;
    /** ログイン/ログアウト時の拡張ハンドラ */
    @Autowired
    @Lazy
    private LoginHandler loginHandler;
    /** ThreadLocalスコープの利用者セッション */
    @Autowired
    private ActorSession actorSession;
    /** CORS利用時のフィルタ */
    @Autowired(required = false)
    private CorsFilter corsFilter;
    /** 認証配下に置くServletFilter */
    @Autowired(required = false)
    private SecurityFilters filters;

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().mvcMatchers(
                serverProps.getServlet().getPathsArray(props.auth().getIgnorePath()));
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

        // login/logout
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

    /**
     * Spring Securityのカスタム認証プロバイダ。
     * <p>主にパスワード照合を行っています。
     */
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
                throw new BadCredentialsException("ログイン認証に失敗しました");
            }
            SecurityActorService service = actorFinder.detailsService();
            UserDetails details = service.loadUserByUsername(authentication.getPrincipal().toString());
            String presentedPassword = authentication.getCredentials().toString();
            if (!encoder.matches(presentedPassword, details.getPassword())) {
                throw new BadCredentialsException("ログイン認証に失敗しました");
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
     * Spring Securityのカスタムエントリポイント。
     * <p>API化を念頭に例外発生時の実装差込をしています。
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
     * SpringSecurityの認証情報(Authentication)とActorSessionを紐付けるServletFilter。
     * <p>dummyLoginが有効な時は常にSecurityContextHolderへAuthenticationを紐付けます。 
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

    /**
     * Spring Securityにおけるログイン/ログアウト時の振る舞いを拡張するHandler。
     */
    @Getter
    @Setter
    public static class LoginHandler
            implements AuthenticationSuccessHandler, AuthenticationFailureHandler, LogoutSuccessHandler {
        @Autowired
        private SecurityProperties props;

        /** ログイン成功処理 */
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

        /** ログイン失敗処理 */
        @Override
        public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                AuthenticationException exception) throws IOException, ServletException {
            if (response.isCommitted()) {
                return;
            }
            writeReponseEmpty(response, HttpServletResponse.SC_BAD_REQUEST);
        }

        /** ログアウト成功処理 */
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
