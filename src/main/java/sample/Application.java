package sample;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Import;

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
    
}
