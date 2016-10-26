package sample.context.orm;

import javax.persistence.*;
import javax.sql.DataSource;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.orm.jpa.*;

import lombok.*;

/** Repository of the system schema. */
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

    /** Create DataSource of the system schema. */
    @ConfigurationProperties(prefix = "extension.datasource.system")
    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class SystemDataSourceProperties extends OrmDataSourceProperties {
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
