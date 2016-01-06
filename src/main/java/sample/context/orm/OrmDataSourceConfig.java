package sample.context.orm;

import java.util.Properties;

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import lombok.Data;

/**
 * DataSource生成用の設定クラス。
 * <p>継承先で@ConfigurationProperties定義を行ってapplication.ymlと紐付してください。
 * <p>ベース実装にHikariCPを利用しています。必要に応じて設定可能フィールドを増やすようにしてください。
 */
@Data
public class OrmDataSourceConfig {

    private String driverClassName = "org.h2.Driver";
    private String url;
    private String username;
    private String password;
    private Properties props = new Properties();

    /** 最低接続プーリング数 */
    private int minIdle = 2;
    /** 最大接続プーリング数 */
    private int maxPoolSize = 20;

    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName(driverClassName);
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.setMinimumIdle(minIdle);
        config.setMaximumPoolSize(maxPoolSize);
        config.setDataSourceProperties(props);
        return new HikariDataSource(config);
    }

}
