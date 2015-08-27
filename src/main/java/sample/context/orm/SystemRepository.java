package sample.context.orm;

import javax.sql.DataSource;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;

import lombok.Setter;

/** システムスキーマのRepositoryを表現します。 */
@org.springframework.stereotype.Repository
@Setter
public class SystemRepository extends OrmRepository {

	public static final String beanNameDs = "systemDataSource";
	public static final String beanNameSf = "systemSessionFactory";
	public static final String beanNameTx = "systemTransactionManager";
	
	@Autowired
	@Qualifier(beanNameSf)
	private SessionFactory sessionFactory;

	@Override
	public SessionFactory sf() {
		return sessionFactory;
	}

	/** システムスキーマのHibernateコンポーネントを生成します。 */
	@ConfigurationProperties(prefix = "extension.hibernate.system")
	public static class SystemRepositoryConfig extends OrmRepositoryConfig {
		@Bean(name = beanNameSf)
		public LocalSessionFactoryBean systemSessionFactory(
				@Qualifier(beanNameDs) final DataSource dataSource, final OrmInterceptor interceptor) {
			return super.sessionFactory(dataSource, interceptor);
		}

		@Bean(name = beanNameTx)
		public HibernateTransactionManager systemTransactionManager(
				@Qualifier(beanNameSf) final SessionFactory sessionFactory) {
			return super.transactionManager(sessionFactory);
		}
	}

	/** システムスキーマのDataSourceを生成します。 */
	@ConfigurationProperties(prefix = "extension.datasource.system")
	public static class SystemDataSourceConfig extends OrmDataSourceConfig {
		@Bean(name = beanNameDs, destroyMethod = "shutdown")
		public DataSource systemDataSource() {
			return super.dataSource();
		}
	}

}
