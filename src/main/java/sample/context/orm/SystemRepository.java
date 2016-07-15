package sample.context.orm;

import javax.persistence.*;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.orm.jpa.*;

import lombok.*;

/** システムスキーマのRepositoryを表現します。 */
@org.springframework.stereotype.Repository
@Setter
public class SystemRepository extends OrmRepository {

    public static final String BeanNameDs = "systemDataSource";
    public static final String BeanNameEmf = "systemEntityManagerFactory";
    public static final String BeanNameTx = "systemTransactionManager";

    @PersistenceContext(unitName = BeanNameEmf)
    private EntityManager em;

    @Override
    public EntityManager em() {
        return em;
    }

    /** システムスキーマのDataSourceを生成します。 */
    @ConfigurationProperties(prefix = "extension.datasource.system")
    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class SystemDataSourceConfig extends OrmDataSourceConfig {
        
        private OrmRepositoryConfig jpa = new OrmRepositoryConfig();
        
        @Bean(name = BeanNameDs, destroyMethod = "shutdown")
        public DataSource systemDataSource() {
            return super.dataSource();
        }
        
        @Bean(name = BeanNameEmf)
        public LocalContainerEntityManagerFactoryBean systemEntityManagerFactoryBean(
                @Qualifier(BeanNameDs) final DataSource dataSource) {
            return jpa.entityManagerFactoryBean(BeanNameEmf, dataSource);
        }

        @Bean(name = BeanNameTx)
        public JpaTransactionManager systemTransactionManager(
                @Qualifier(BeanNameEmf) final EntityManagerFactory emf) {
            return jpa.transactionManager(emf);
        }
    }

}
