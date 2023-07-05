package sample;

import java.net.URI;
import java.time.Duration;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import jakarta.servlet.Filter;
import sample.context.actor.Actor;
import sample.context.actor.ActorSession;

/**
 * Represents the security definition of the application.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class ApplicationSecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(4);
    }

    /** Define Spring Security settings (authentication/authorization). */
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, ApplicationProperties props)
            throws Exception {
        http.csrf(csrf -> csrf.disable());
        http.authorizeHttpRequests(authz -> {
            authz
                    .requestMatchers("/api/**").authenticated()
                    .anyRequest().permitAll();
        });
        http.exceptionHandling(exception -> {
            exception.authenticationEntryPoint(authenticationEntryPoint());
        });
        http.addFilterAfter(actorSessionFilter(), UsernamePasswordAuthenticationFilter.class);
        if (props.isCors()) {
            http.addFilterAt(corsWebFilter(), CorsFilter.class);
        }

        // login/logout
        http.oauth2Login(login -> {
            login.successHandler(successHandler(props.getLogin().getSuccessUri()))
                    .failureHandler(failureHandler(props.getLogin().getFailureUri()));
        });
        return http.build();
    }

    /** Actor to thread local */
    private Filter actorSessionFilter() {
        return (req, res, chain) -> {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null) {
                if (auth.getPrincipal() instanceof MyOidcUser oidcUser) {
                    ActorSession.bind(oidcUser.getActor());
                } else if (auth instanceof UsernamePasswordAuthenticationToken) {
                    ActorSession.bind((Actor) auth.getPrincipal());
                } else {
                    throw new IllegalStateException("Not Support Authentication type. ["
                            + auth.getClass().getCanonicalName() + "]");
                }
                try {
                    chain.doFilter(req, res);
                } finally {
                    ActorSession.unbind();
                }
            } else {
                ActorSession.unbind();
                chain.doFilter(req, res);
            }
        };
    }

    /** CORS Support Filter */
    private CorsFilter corsWebFilter() {
        var cors = new CorsConfiguration();
        cors.setAllowedOriginPatterns(List.of("http://localhost*"));
        cors.setAllowCredentials(true);
        cors.setAllowedHeaders(List.of("*"));
        cors.setAllowedMethods(List.of("*"));
        cors.setMaxAge(Duration.ofSeconds(3600));

        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cors);
        return new CorsFilter(source);
    }

    private AuthenticationEntryPoint authenticationEntryPoint() {
        return (req, res, ex) -> {
            var status = HttpStatus.UNAUTHORIZED;
            if (ex instanceof AuthenticationCredentialsNotFoundException) {
                status = HttpStatus.FORBIDDEN;
            }
            res.setStatus(status.value());
        };
    }

    private AuthenticationSuccessHandler successHandler(URI uri) {
        return (req, res, auth) -> {
            var user = (MyOidcUser) auth.getPrincipal();
            log.info("Success Authentication. [{}]", user.getActor().id());
            res.setStatus(HttpStatus.MOVED_PERMANENTLY.value());
            res.setHeader(HttpHeaders.LOCATION, uri.toString());
        };
    }

    private AuthenticationFailureHandler failureHandler(URI uri) {
        return (req, res, ex) -> {
            log.warn("Failure Authentication. cause={}", ex.getMessage());
            res.setStatus(HttpStatus.MOVED_PERMANENTLY.value());
            res.setHeader(HttpHeaders.LOCATION, uri.toString());
        };
    }

    @Bean
    SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

}
