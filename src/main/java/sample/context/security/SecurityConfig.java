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
import sample.context.security.SecurityAuthConfig.*;
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
	    config.setAllowCredentials(props.cors().isAllowCredentials());
	    config.addAllowedOrigin(props.cors().getAllowedOrigin());
	    config.addAllowedHeader(props.cors().getAllowedHeader());
	    config.addAllowedMethod(props.cors().getAllowedMethod());
	    config.setMaxAge(props.cors().getMaxAge());
	    source.registerCorsConfiguration(props.cors().getPath(), config);
	    return new CorsFilter(source);
	}
	
	/** セキュリティ関連の設定情報を表現します。 */
	@Data
	@ConfigurationProperties(prefix = "extension.security")
	public static class SecurityProperties {
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
