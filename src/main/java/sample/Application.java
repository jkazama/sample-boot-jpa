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
 * アプリケーションプロセスの起動クラス。
 * <p>本クラスを実行する事でSpringBootが提供する組込Tomcatでのアプリケーション起動が行われます。
 */
@SpringBootApplication
@EnableCaching(proxyTargetClass = true)
@Import({ApplicationConfig.class, ApplicationDbConfig.class, ApplicationSeucrityConfig.class})
public class Application {
    
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
    
    /** SpringMvcの拡張コンフィギュレーション */
    @Configuration
    public static class WebMvcConfig extends WebMvcConfigurerAdapter {
        @Autowired
        private MessageSource message;

        /** HibernateのLazyLoading回避対応。  see JacksonAutoConfiguration */
        @Bean
        public Hibernate5Module jsonHibernate5Module() {
            return new Hibernate5Module();
        }

        /** BeanValidationメッセージのUTF-8に対応したValidator。 */
        @Bean
        public LocalValidatorFactoryBean validator() {
            LocalValidatorFactoryBean factory = new LocalValidatorFactoryBean();
            factory.setValidationMessageSource(message);
            return factory;
        }

        /** 標準Validatorの差し替えをします。 */
        @Override
        public org.springframework.validation.Validator getValidator() {
            return validator();
        }
    }

}
