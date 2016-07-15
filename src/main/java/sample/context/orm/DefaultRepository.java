package sample.context.orm;

import javax.persistence.*;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.orm.jpa.*;

import lombok.*;

/** 標準スキーマのRepositoryを表現します。 */
@org.springframework.stereotype.Repository
@Setter
public class DefaultRepository extends OrmRepository {
    public static final String BeanNameDs = "dataSource";
    public static final String BeanNameEmf = "entityManagerFactory";
    public static final String BeanNameTx = "transactionManager";

    @PersistenceContext(unitName = BeanNameEmf)
    private EntityManager em;

    @Override
    public EntityManager em() {
        return em;
    }

    /** 標準スキーマのDataSourceを生成します。 */
    @ConfigurationProperties(prefix = "extension.datasource.default")
    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class DefaultDataSourceConfig extends OrmDataSourceConfig {
        
        private OrmRepositoryConfig jpa = new OrmRepositoryConfig();
        
        @Bean(name = BeanNameDs, destroyMethod = "shutdown")
        public DataSource dataSource() {
            return super.dataSource();
        }
        
        @Bean(name = BeanNameEmf)
        public LocalContainerEntityManagerFactoryBean entityManagerFactoryBean(
                @Qualifier(BeanNameDs) final DataSource dataSource) {
            return jpa.entityManagerFactoryBean(BeanNameEmf, dataSource);
        }

        @Bean(name = BeanNameTx)
        public JpaTransactionManager transactionManager(
                @Qualifier(BeanNameEmf) final EntityManagerFactory emf) {
            return jpa.transactionManager(emf);
        }

    }
    
}
