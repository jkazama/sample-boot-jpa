package sample;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;
import sample.context.orm.repository.DefaultRepository.DefaultDataSourceProperties;
import sample.context.orm.repository.SystemRepository.SystemDataSourceProperties;

/**
 * Represents the property concept of the application.
 */
@ConfigurationProperties("sample")
@Data
public class ApplicationProperties {
    private boolean cors;
    private DatasourceProps datasource = new DatasourceProps();
    private MailProps mail;

    @Data
    public static class DatasourceProps {
        private DefaultDataSourceProperties app = new DefaultDataSourceProperties();
        private SystemDataSourceProperties system = new SystemDataSourceProperties();
    }

    @Data
    public static class MailProps {
        private boolean enabled;
    }

}
