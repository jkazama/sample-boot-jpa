package sample.context.orm;

import java.util.Properties;

import javax.sql.DataSource;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.jdbc.DatabaseDriver;
import org.springframework.util.StringUtils;

import com.zaxxer.hikari.HikariDataSource;

import lombok.Data;

/**
 * Configuration class for DataSource generation.
 * <p>
 * Please define @ConfigurationProperties in your inheritance and tie it to
 * application.yml.
 * <p>
 * HikariCP is used for the base implementation. Please increase the number of
 * configurable fields as needed.
 */
@Data
public class OrmDataSourceProperties {

    /** Driver class name (automatically registered from url if not set) */
    private String driverClassName;
    private String url;
    private String username;
    private String password;
    private Properties props = new Properties();

    /** Minimum number of connection poolings */
    private int minIdle = 1;
    /** Maximum number of connection pooling */
    private int maxPoolSize = 20;

    /** true when checking connection status */
    private boolean validation = true;
    /**
     * Connection status check query (automatically set if not set and Database
     * supports it)
     */
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
