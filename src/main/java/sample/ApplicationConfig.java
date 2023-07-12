package sample;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.fasterxml.jackson.datatype.hibernate6.Hibernate6Module;

/**
 * Represents a bean definition in an application.
 * <p>
 * If the component is not defined on the class side, please explicitly state it
 * here.
 */
@Configuration
@EnableCaching
@EnableScheduling
@EnableConfigurationProperties(ApplicationProperties.class)
public class ApplicationConfig {

    @Bean
    Hibernate6Module hibernate6Module() {
        return new Hibernate6Module();
    }

}
