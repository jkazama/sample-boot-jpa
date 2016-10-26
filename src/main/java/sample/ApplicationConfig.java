package sample;

import org.springframework.boot.actuate.health.*;
import org.springframework.boot.actuate.health.Health.Builder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.*;

import sample.context.*;
import sample.context.actor.ActorSession;
import sample.context.audit.AuditHandler;
import sample.context.audit.AuditHandler.AuditPersister;
import sample.context.lock.IdLockHandler;
import sample.context.mail.MailHandler;
import sample.context.report.ReportHandler;
import sample.model.BusinessDayHandler;

@Configuration
public class ApplicationConfig {

    /** component definition of the infrastructure layer. (package sample.context) */
    @Configuration
    static class PlainConfig {
        @Bean
        Timestamper timestamper() {
            return new Timestamper();
        }
        @Bean
        ActorSession actorSession() {
            return new ActorSession();
        }
        @Bean
        ResourceBundleHandler resourceBundleHandler() {
            return new ResourceBundleHandler();
        }
        @Bean
        AppSettingHandler appSettingHandler() {
            return new AppSettingHandler();
        }
        @Bean
        AuditHandler auditHandler() {
            return new AuditHandler();
        }
        @Bean
        AuditPersister auditPersister() {
            return new AuditPersister();
        }
        @Bean
        IdLockHandler idLockHandler() {
            return new IdLockHandler();
        }
        @Bean
        MailHandler mailHandler() {
            return new MailHandler();
        }
        @Bean
        ReportHandler reportHandler() {
            return new ReportHandler();
        }
        @Bean
        DomainHelper domainHelper() {
            return new DomainHelper();
        }
    }

    @Configuration
    static class HealthCheckConfig {
        @Bean
        @ConditionalOnBean(BusinessDayHandler.class)
        HealthIndicator dayIndicator(final Timestamper time, final BusinessDayHandler day) {
            return new AbstractHealthIndicator() {
                @Override
                protected void doHealthCheck(Builder builder) throws Exception {
                    builder.up();
                    builder.withDetail("day", day.day())
                            .withDetail("dayMinus1", day.day(-1))
                            .withDetail("dayPlus1", day.day(1))
                            .withDetail("dayPlus2", day.day(2))
                            .withDetail("dayPlus3", day.day(3));
                }
            };
        }
    }

}
