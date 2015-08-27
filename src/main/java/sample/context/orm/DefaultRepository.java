package sample.context.orm;

import javax.sql.DataSource;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.orm.hibernate5.*;

import lombok.Setter;

/** 標準スキーマのRepositoryを表現します。 */
@org.springframework.stereotype.Repository
@Setter
public class DefaultRepository extends OrmRepository {
	public static final String beanNameDs = "dataSource";
	public static final String beanNameSf = "sessionFactory";
	public static final String beanNameTx = "transactionManager";

	@Autowired
	@Qualifier(beanNameSf)
	private SessionFactory sessionFactory;

	@Override
	public SessionFactory sf() {
		return sessionFactory;
	}

	/** 標準スキーマのHibernateコンポーネントを生成します。 */
	@ConfigurationProperties(prefix = "extension.hibernate.default")
	public static class DefaultRepositoryConfig extends OrmRepositoryConfig {
		@Bean(name = beanNameSf)
		@Override
		public LocalSessionFactoryBean sessionFactory(
				@Qualifier(beanNameDs) final DataSource dataSource, final OrmInterceptor interceptor) {
			return super.sessionFactory(dataSource, interceptor);
		}

		@Bean(name = beanNameTx)
		@Override
		public HibernateTransactionManager transactionManager(
				@Qualifier(beanNameSf) final SessionFactory sessionFactory) {
			return super.transactionManager(sessionFactory);
		}
	}
	
	/** 標準スキーマのDataSourceを生成します。 */
	@ConfigurationProperties(prefix = "extension.datasource.default")
	public static class DefaultDataSourceConfig extends OrmDataSourceConfig {
		@Bean(name = beanNameDs, destroyMethod = "shutdown")
		public DataSource dataSource() {
			return super.dataSource();
		}
	}

}
