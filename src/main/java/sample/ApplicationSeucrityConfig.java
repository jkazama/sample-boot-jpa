package sample;

import org.springframework.boot.autoconfigure.condition.*;
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

@Configuration
@EnableConfigurationProperties({ SecurityProperties.class })
public class ApplicationSeucrityConfig {
    
    @Bean
    PasswordEncoder passwordEncoder() {
        //low: I recommend the use of strength and SecureRandom.
        return new BCryptPasswordEncoder();
    }

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

    /** API certification / authorization definition using Spring Security. */
    @Configuration
    @EnableWebSecurity
    @EnableGlobalMethodSecurity(prePostEnabled = true, proxyTargetClass = true)
    @ConditionalOnProperty(prefix = "extension.security.auth", name = "enabled", matchIfMissing = true)
    @Order(org.springframework.boot.autoconfigure.security.SecurityProperties.BASIC_AUTH_ORDER)
    static class AuthSecurityConfig {
    
        @Bean
        @Order(org.springframework.boot.autoconfigure.security.SecurityProperties.BASIC_AUTH_ORDER)
        SecurityConfigurer securityConfigurer() {
            return new SecurityConfigurer();
        }
        
        @Bean
        AuthenticationManager authenticationManager() throws Exception {
            return securityConfigurer().authenticationManagerBean();
        }
        
        @Bean
        SecurityProvider securityProvider() {
            return new SecurityProvider();
        }
        
        @Bean
        SecurityEntryPoint securityEntryPoint() {
            return new SecurityEntryPoint();
        }
        
        @Bean
        LoginHandler loginHandler() {
            return new LoginHandler();
        }
        
        @Bean
        SecurityActorFinder securityActorFinder() {
            return new SecurityActorFinder();
        }
    }    
}
