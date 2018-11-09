package sample;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.health.*;
import org.springframework.boot.actuate.health.Health.Builder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.*;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;

import sample.context.*;
import sample.context.actor.ActorSession;
import sample.context.audit.AuditHandler;
import sample.context.audit.AuditHandler.AuditPersister;
import sample.context.lock.IdLockHandler;
import sample.context.mail.MailHandler;
import sample.context.orm.DefaultRepository;
import sample.context.report.ReportHandler;
import sample.model.*;
import sample.model.BusinessDayHandler.HolidayAccessor;

/**
 * アプリケーションにおけるBean定義を表現します。
 * <p>クラス側でコンポーネント定義していない時はこちらで明示的に記載してください。
 */
@Configuration
public class ApplicationConfig {

    /** インフラ層 ( context 配下) のコンポーネント定義を表現します */
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
    
    /** ドメイン層 ( domain 配下) のコンポーネント定義を表現します */
    static class DomainConfig {
        @Bean
        BusinessDayHandler businessDayHandler(Timestamper time, HolidayAccessor holiday) {
            return BusinessDayHandler.of(time, holiday);
        }
        @Bean
        HolidayAccessor holidayAccessor(
                DefaultRepository rep,
                @Qualifier(DefaultRepository.BeanNameTx)
                PlatformTransactionManager txm) {
            return new HolidayAccessor(txm, rep); //low: 定義側にスキーマ指定を委ねるときは外部から設定するアプローチで
        }
        
        @Bean
        @ConditionalOnProperty(prefix = "extension.datafixture", name = "enabled", matchIfMissing = false)
        DataFixtures dataFixtures() {
            return new DataFixtures();
        }
    }

    /** 拡張ヘルスチェック定義を表現します。 */
    @Configuration
    static class HealthCheckConfig {
        /** 営業日チェック */
        @Bean
        HealthIndicator dayIndicator(Timestamper time, BusinessDayHandler day) {
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
    
    @Configuration
    static class WebMVCConfig {

        /** HibernateのLazyLoading回避対応。  see JacksonAutoConfiguration */
        @Bean
        public Hibernate5Module jsonHibernate5Module() {
            return new Hibernate5Module();
        }

        /** BeanValidationメッセージのUTF-8に対応したValidator。 */
        @Bean
        public LocalValidatorFactoryBean defaultValidator(MessageSource message) {
            LocalValidatorFactoryBean factory = new LocalValidatorFactoryBean();
            factory.setValidationMessageSource(message);
            return factory;
        }
    }

}
