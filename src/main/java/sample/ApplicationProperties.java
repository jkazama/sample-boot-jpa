package sample;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * Represents the property concept of the application.
 */
@ConfigurationProperties("sample")
@Data
public class ApplicationProperties {
    private boolean cors;
    private MailProps mail;

    @Data
    public static class MailProps {
        private boolean enabled;
    }

}
