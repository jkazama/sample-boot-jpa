package sample.context.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * Security-related setting information.
 */
@Data
@ConfigurationProperties(prefix = "extension.security")
public class SecurityProperties {
    /** Spring Security-dependent certification / authorization setting information */
    private SecurityAuthProperties auth = new SecurityAuthProperties();
    /** CORS setting information*/
    private SecurityCorsProperties cors = new SecurityCorsProperties();

    public SecurityAuthProperties auth() {
        return auth;
    }

    public SecurityCorsProperties cors() {
        return cors;
    }
    
    @Data
    public static class SecurityAuthProperties {
        private String loginKey = "loginId";
        private String passwordKey = "password";
        private String[] path = new String[] { "/api/**" };
        private String[] pathAdmin = new String[] { "/api/admin/**" };
        private String[] excludesPath = new String[] { "/api/system/job/**" };
        private String[] ignorePath = new String[] { "/css/**", "/js/**", "/img/**", "/**/favicon.ico" };
        private String loginPath = "/api/login";
        private String logoutPath = "/api/logout";
        private int maximumSessions = 2;
        /**
         * It is true at the time of mode for admin user.
         * <p>The login path is the same, but the handling of the login processing is replaced.
         * <ul>
         * <li>true: SecurityUserService
         * <li>false: SecurityAdminService
         * </ul> 
         */
        private boolean admin = false;
        /** Is the certification effective. */
        private boolean enabled = true;
    }
    
    @Data
    public static class SecurityCorsProperties {
        private boolean allowCredentials = true;
        private String allowedOrigin = "*";
        private String allowedHeader = "*";
        private String allowedMethod = "*";
        private long maxAge = 3600L;
        private String path = "/**";
    }

}
