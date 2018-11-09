package sample;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Import;

import sample.controller.AccountController;
import sample.usecase.AccountService;

/**
 * アプリケーションプロセスの起動クラス。
 * <p>本クラスを実行する事でSpringBootが提供する組込Tomcatでのアプリケーション起動が行われます。
 */
@SpringBootApplication(scanBasePackageClasses = {
        AccountController.class, AccountService.class
})
@EnableCaching(proxyTargetClass = true)
@Import({ ApplicationConfig.class, ApplicationDbConfig.class, ApplicationSecurityConfig.class })
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
