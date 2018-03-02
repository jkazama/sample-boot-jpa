package sample.context.orm;

import java.util.Properties;

import javax.sql.DataSource;

import org.springframework.boot.jdbc.*;
import org.springframework.util.StringUtils;

import com.zaxxer.hikari.HikariDataSource;

import lombok.Data;

/**
 * A setting class for the DataSource creation.
 * <p>You perform {@literal @}ConfigurationProperties definition in succession,
 *   and please related it with application.yml.
 * <p>Use HikariCP for base implementation. Please increase setable fields as needed.
 */
@Data
public class OrmDataSourceProperties {

    /** Driver class name (Register in the no-setting automatically from url) */
    private String driverClassName;
    private String url;
    private String username;
    private String password;
    private Properties props = new Properties();

    private int minIdle = 1;
    private int maxPoolSize = 20;

    private boolean validation = true;
    private String validationQuery;

    public String name() {
        return this.getClass().getSimpleName().replaceAll("Properties", "");
    }

    public DataSource dataSource() {
        HikariDataSource dataSource = (HikariDataSource) DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .driverClassName(this.driverClassName()).url(this.url)
                .username(this.username).password(this.password)
                .build();
        dataSource.setMinimumIdle(minIdle);
        dataSource.setMaximumPoolSize(maxPoolSize);
        if (validation) {
            dataSource.setConnectionTestQuery(validationQuery());
        }
        dataSource.setPoolName(name());
        dataSource.setDataSourceProperties(props);
        return dataSource;
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
