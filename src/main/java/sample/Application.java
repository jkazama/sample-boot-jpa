package sample;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.*;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;

/**
 * The start class of the application.
 */
@SpringBootApplication
@EnableCaching(proxyTargetClass = true)
@Import({ApplicationConfig.class, ApplicationDbConfig.class, ApplicationSeucrityConfig.class})
public class Application {
    
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
    
    @Configuration
    public static class WebMvcConfig extends WebMvcConfigurerAdapter {
        @Autowired
        private MessageSource message;

        /** Invalidate Hibernate lazy loading. see JacksonAutoConfiguration */
        @Bean
        public Hibernate5Module jsonHibernate5Module() {
            return new Hibernate5Module();
        }

        /** UTF8 to JSR303 message file. */
        @Bean
        public LocalValidatorFactoryBean validator() {
            LocalValidatorFactoryBean factory = new LocalValidatorFactoryBean();
            factory.setValidationMessageSource(message);
            return factory;
        }

        @Override
        public org.springframework.validation.Validator getValidator() {
            return validator();
        }
    }

}
