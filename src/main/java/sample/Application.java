package sample;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * アプリケーションプロセスの起動クラス。
 * <p>本クラスを実行する事でSpringBootが提供する組込Tomcatでのアプリケーション起動が行われます。
 */
@SpringBootApplication
@EnableCaching(proxyTargetClass = true)
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
