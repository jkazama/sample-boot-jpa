package sample.context.orm;

import javax.persistence.*;
import javax.sql.DataSource;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.orm.jpa.*;

import lombok.*;

/** 標準スキーマのRepositoryを表現します。 */
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
    public static class DefaultDataSourceProperties extends OrmDataSourceProperties {        
        private OrmRepositoryProperties jpa = new OrmRepositoryProperties();
        
        public DataSource dataSource() {
            return super.dataSource();
        }
        
        public LocalContainerEntityManagerFactoryBean entityManagerFactoryBean(
                final DataSource dataSource) {
            return jpa.entityManagerFactoryBean(BeanNameEmf, dataSource);
        }

        public JpaTransactionManager transactionManager(final EntityManagerFactory emf) {
            return jpa.transactionManager(emf);
        }
    }
    
}
