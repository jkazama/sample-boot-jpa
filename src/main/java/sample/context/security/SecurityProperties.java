package sample.context.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * セキュリティ関連の設定情報を表現します。
 */
@Data
@ConfigurationProperties(prefix = "extension.security")
public class SecurityProperties {
    /** Spring Security依存の認証/認可設定情報 */
    private SecurityAuthProperties auth = new SecurityAuthProperties();
    /** CORS設定情報 */
    private SecurityCorsProperties cors = new SecurityCorsProperties();

    public SecurityAuthProperties auth() {
        return auth;
    }

    public SecurityCorsProperties cors() {
        return cors;
    }

    /** Spring Securityに対する拡張設定情報 */
    @Data
    public static class SecurityAuthProperties {
        /** リクエスト時のログインIDを取得するキー */
        private String loginKey = "loginId";
        /** リクエスト時のパスワードを取得するキー */
        private String passwordKey = "password";
        /** 認証対象パス */
        private String[] path = new String[] { "/api/**" };
        /** 認証対象パス(管理者向け) */
        private String[] pathAdmin = new String[] { "/api/admin/**" };
        /** 認証除外パス(認証対象からの除外) */
        private String[] excludesPath = new String[] { "/api/system/job/**" };
        /** 認証無視パス(フィルタ未適用の認証未考慮、静的リソース等) */
        private String[] ignorePath = new String[] { "/css/**", "/js/**", "/img/**", "/*/favicon.ico" };
        /** ログインAPIパス */
        private String loginPath = "/api/login";
        /** ログアウトAPIパス */
        private String logoutPath = "/api/logout";
        /** 一人が同時利用可能な最大セッション数 */
        private int maximumSessions = 2;
        /**
         * 社員向けモードの時はtrue。
         * <p>ログインパスは同じですが、ログイン処理の取り扱いが切り替わります。
         * <ul>
         * <li>true: SecurityUserService
         * <li>false: SecurityAdminService
         * </ul>
         */
        private boolean admin = false;
        /** 認証が有効な時はtrue */
        private boolean enabled = true;
    }

    /** CORS設定情報を表現します。 */
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
