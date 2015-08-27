package sample.context.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.*;
import org.springframework.context.annotation.*;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.*;
import org.springframework.web.filter.CorsFilter;

import lombok.Data;
import sample.context.security.SecurityAuthConfig.SecurityAuthProperties;
import sample.context.security.SecurityConfig.SecurityProperties;

/**
 * Spring Securityに依存しないセキュリティ関連の設定定義を表現します。
 */
@Configuration
@EnableConfigurationProperties({SecurityProperties.class})
@Order(org.springframework.boot.autoconfigure.security.SecurityProperties.ACCESS_OVERRIDE_ORDER)
public class SecurityConfig {

	/** パスワード用のハッシュ(BCrypt)エンコーダー。 */
	@Bean
	public PasswordEncoder passwordEncoder() {
		//low: きちんとやるのであれば、strengthやSecureRandom使うなど外部切り出し含めて検討してください
		return new BCryptPasswordEncoder();
	}
	
	/** CORS全体適用 */
	@Bean
	@ConditionalOnProperty(prefix = "extension.security.cors", name = "enabled", matchIfMissing = false)
	public CorsFilter corsFilter(SecurityProperties props) {
	    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
	    CorsConfiguration config = new CorsConfiguration();
	    config.setAllowCredentials(true);
	    config.addAllowedOrigin("*");
	    config.addAllowedHeader("*");
	    config.addAllowedMethod("*");
	    config.setMaxAge(3600L);
	    source.registerCorsConfiguration("/**", config);
	    return new CorsFilter(source);
	}
	
	/** Spring Securityに対する拡張設定情報 */
	@Data
	@ConfigurationProperties(prefix = "extension.security")
	public static class SecurityProperties {
		/** Spring Security依存の認証/認可設定情報 */
		private SecurityAuthProperties auth = new SecurityAuthProperties();
		public SecurityAuthProperties auth() {
			return auth;
		}
	}
		
}
