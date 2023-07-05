package sample;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import jakarta.persistence.EntityManagerFactory;
import sample.context.DomainHelper;
import sample.context.orm.OrmInterceptor;
import sample.context.orm.OrmRepository;
import sample.context.orm.repository.DefaultRepository;
import sample.context.orm.repository.DefaultRepository.DefaultDataSourceProperties;
import sample.context.orm.repository.SystemRepository;
import sample.context.orm.repository.SystemRepository.SystemDataSourceProperties;

/**
 * Represents a database connection definition for an application.
 */
@Configuration
public class ApplicationDbConfig {

    /** Represents a connection definition to a standard schema. */
    @Configuration
    static class DefaultDbConfig {

        @Bean
        @Primary
        OrmRepository defaultRepository(DomainHelper dh, OrmInterceptor interceptor) {
            return DefaultRepository.of(dh, interceptor);
        }

        @Bean(name = DefaultRepository.BeanNameDs, destroyMethod = "close")
        @Primary
        DataSource dataSource(DefaultDataSourceProperties props) {
            return props.dataSource();
        }

        @Bean(name = DefaultRepository.BeanNameEmf)
        @Primary
        LocalContainerEntityManagerFactoryBean entityManagerFactoryBean(
                DefaultDataSourceProperties props,
                @Qualifier(DefaultRepository.BeanNameDs) final DataSource dataSource) {
            return props.entityManagerFactoryBean(dataSource);
        }

        @Bean(name = DefaultRepository.BeanNameTx)
        @Primary
        JpaTransactionManager transactionManager(
                DefaultDataSourceProperties props,
                @Qualifier(DefaultRepository.BeanNameEmf) final EntityManagerFactory emf) {
            return props.transactionManager(emf);
        }

    }

    /** Represents a connection definition to the system schema. */
    @Configuration
    static class SystemDbConfig {

        @Bean
        SystemRepository systemRepository(DomainHelper dh, OrmInterceptor interceptor) {
            return SystemRepository.of(dh, interceptor);
        }

        @Bean(name = SystemRepository.BeanNameDs, destroyMethod = "close")
        DataSource systemDataSource(SystemDataSourceProperties props) {
            return props.dataSource();
        }

        @Bean(name = SystemRepository.BeanNameEmf)
        LocalContainerEntityManagerFactoryBean systemEntityManagerFactoryBean(
                SystemDataSourceProperties props,
                @Qualifier(SystemRepository.BeanNameDs) final DataSource dataSource) {
            return props.entityManagerFactoryBean(dataSource);
        }

        @Bean(name = SystemRepository.BeanNameTx)
        JpaTransactionManager systemTransactionManager(
                SystemDataSourceProperties props,
                @Qualifier(SystemRepository.BeanNameEmf) final EntityManagerFactory emf) {
            return props.transactionManager(emf);
        }

    }

}
