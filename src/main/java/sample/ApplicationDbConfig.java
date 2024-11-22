package sample;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import sample.context.DomainHelper;
import sample.context.orm.OrmInterceptor;
import sample.context.orm.OrmRepository;

/**
 * Represents a database connection definition for an application.
 */
@Configuration
public class ApplicationDbConfig {

    @Bean
    @Primary
    OrmRepository defaultRepository(DomainHelper dh, OrmInterceptor interceptor) {
        return OrmRepository.of(dh, interceptor);
    }

}
