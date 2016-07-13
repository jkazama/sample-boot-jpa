package sample.context.orm;

import java.util.Properties;

import javax.sql.DataSource;

import org.springframework.boot.jdbc.DatabaseDriver;
import org.springframework.util.StringUtils;

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

    /** ドライバクラス名称 ( 未設定時は url から自動登録 ) */
    private String driverClassName;
    private String url;
    private String username;
    private String password;
    private Properties props = new Properties();

    /** 最低接続プーリング数 */
    private int minIdle = 1;
    /** 最大接続プーリング数 */
    private int maxPoolSize = 20;
    
    /** コネクション状態を確認する時は true */
    private boolean validation = true;
    /** コネクション状態確認クエリ ( 未設定時かつ Database が対応している時は自動設定 ) */
    private String validationQuery;

    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName(driverClassName());
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.setMinimumIdle(minIdle);
        config.setMaximumPoolSize(maxPoolSize);
        if (validation) {
            config.setConnectionTestQuery(validationQuery());
        }
        config.setDataSourceProperties(props);
        return new HikariDataSource(config);
    }

    private String driverClassName() {
        if (StringUtils.hasText(driverClassName)) {
            return driverClassName;
        }
        return DatabaseDriver.fromJdbcUrl(url).getDriverClassName();
    }
    
    private String validationQuery() {
        if (StringUtils.hasText(validationQuery)) {
            return validationQuery;
        }
        return DatabaseDriver.fromJdbcUrl(url).getValidationQuery();
    }

}
