package sample;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.*;
import org.springframework.web.filter.CorsFilter;

import sample.context.security.*;
import sample.context.security.SecurityConfigurer.*;

/**
 * アプリケーションのセキュリティ定義を表現します。
 */
@Configuration
@EnableConfigurationProperties({ SecurityProperties.class })
public class ApplicationSecurityConfig {
    
    /** パスワード用のハッシュ(BCrypt)エンコーダー。 */
    @Bean
    PasswordEncoder passwordEncoder() {
        //low: きちんとやるのであれば、strengthやSecureRandom使うなど外部切り出し含めて検討してください
        return new BCryptPasswordEncoder();
    }

    /** CORS全体適用 */
    @Bean
    @ConditionalOnProperty(prefix = "extension.security.cors", name = "enabled", matchIfMissing = false)
    CorsFilter corsFilter(SecurityProperties props) {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(props.cors().isAllowCredentials());
        config.addAllowedOrigin(props.cors().getAllowedOrigin());
        config.addAllowedHeader(props.cors().getAllowedHeader());
        config.addAllowedMethod(props.cors().getAllowedMethod());
        config.setMaxAge(props.cors().getMaxAge());
        source.registerCorsConfiguration(props.cors().getPath(), config);
        return new CorsFilter(source);
    }

    /** Spring Security を用いた API 認証/認可定義を表現します。 */
    @Configuration
    @EnableWebSecurity
    @EnableGlobalMethodSecurity(prePostEnabled = true, proxyTargetClass = true)
    @Order(org.springframework.boot.autoconfigure.security.SecurityProperties.BASIC_AUTH_ORDER)
    static class AuthSecurityConfig {
    
        /** Spring Security 全般の設定 ( 認証/認可 ) を定義します。 */
        @Bean
        @Order(org.springframework.boot.autoconfigure.security.SecurityProperties.BASIC_AUTH_ORDER)
        SecurityConfigurer securityConfigurer() {
            return new SecurityConfigurer();
        }
        
        /** Spring Security のカスタム認証プロセス管理コンポーネント。 */
        @Bean
        AuthenticationManager authenticationManager() throws Exception {
            return securityConfigurer().authenticationManagerBean();
        }
        
        /** Spring Security のカスタム認証プロバイダ。 */
        @Bean
        SecurityProvider securityProvider() {
            return new SecurityProvider();
        }
        
        /** Spring Security のカスタムエントリポイント。 */
        @Bean
        SecurityEntryPoint securityEntryPoint() {
            return new SecurityEntryPoint();
        }
        
        /** Spring Security におけるログイン/ログアウト時の振る舞いを拡張するHandler。 */
        @Bean
        LoginHandler loginHandler() {
            return new LoginHandler();
        }
        
        /** Spring Security で利用される認証/認可対象となるユーザ情報を提供します。 */
        @Bean
        SecurityActorFinder securityActorFinder() {
            return new SecurityActorFinder();
        }
    }    
}
