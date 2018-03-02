package sample;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.orm.jpa.*;

import sample.context.orm.*;
import sample.context.orm.DefaultRepository.DefaultDataSourceProperties;
import sample.context.orm.SystemRepository.SystemDataSourceProperties;

@Configuration
@EnableConfigurationProperties({ DefaultDataSourceProperties.class, SystemDataSourceProperties.class })
public class ApplicationDbConfig {

    @Bean
    OrmInterceptor ormInterceptor() {
        return new OrmInterceptor();
    }

    /** Connection definition to a standard schema. */
    @Configuration
    static class DefaultDbConfig {
        @Bean
        DefaultRepository defaultRepository() {
            return new DefaultRepository();
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

    /** Connection definition to a system schema. */
    @Configuration
    static class SystemDbConfig {

        @Bean
        SystemRepository systemRepository() {
            return new SystemRepository();
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
