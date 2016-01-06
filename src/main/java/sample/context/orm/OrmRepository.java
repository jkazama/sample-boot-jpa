package sample.context.orm;

import java.io.Serializable;
import java.util.*;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.*;
import org.hibernate.boot.model.naming.*;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.orm.jpa.hibernate.SpringNamingStrategy;
import org.springframework.orm.hibernate5.*;

import lombok.*;
import sample.ValidationException;
import sample.ValidationException.ErrorKeys;
import sample.context.*;

/**
 * HibernateのRepository基底実装。
 * <p>本コンポーネントはRepositoryとEntityの1-n関係を実現するためにSpringDataの基盤を
 * 利用しない形で単純なORM実装を提供します。
 * <p>OrmRepositoryを継承して作成されるRepositoryの粒度はデータソース単位となります。
 */
@Setter
public abstract class OrmRepository implements Repository {

    @Autowired
    private DomainHelper dh;

    public abstract SessionFactory sf();

    @Override
    public DomainHelper dh() {
        return dh;
    }

    /**
     * ORM操作の簡易アクセサを生成します。
     * <p>OrmTemplateは呼出しの都度生成されます。
     */
    public OrmTemplate tmpl() {
        return new OrmTemplate(sf());
    }

    /** 指定したEntityクラスを軸にしたCriteriaを生成します。 */
    public <T extends Entity> OrmCriteria<T> criteria(Class<T> clazz) {
        return new OrmCriteria<T>(clazz);
    }

    /** 指定したEntityクラスにエイリアスを紐付けたCriteriaを生成します。 */
    public <T extends Entity> OrmCriteria<T> criteria(Class<T> clazz, String alias) {
        return new OrmCriteria<T>(clazz, alias);
    }

    @Override
    public <T extends Entity> Optional<T> get(Class<T> clazz, Serializable id) {
        return Optional.ofNullable(tmpl().origin().get(clazz, id));
    }

    @Override
    public <T extends Entity> T load(Class<T> clazz, Serializable id) {
        try {
            T m = tmpl().origin().load(clazz, id);
            m.hashCode(); // force loading
            return m;
        } catch (HibernateObjectRetrievalFailureException e) {
            throw new ValidationException(ErrorKeys.EntityNotFound);
        }
    }

    @Override
    public <T extends Entity> T loadForUpdate(Class<T> clazz, Serializable id) {
        try {
            T m = tmpl().origin().load(clazz, id, LockMode.UPGRADE_NOWAIT);
            m.hashCode(); // force loading
            return m;
        } catch (HibernateObjectRetrievalFailureException e) {
            throw new ValidationException(ErrorKeys.EntityNotFound);
        }
    }

    @Override
    public <T extends Entity> boolean exists(Class<T> clazz, Serializable id) {
        return get(clazz, id) != null;
    }

    @Override
    public <T extends Entity> List<T> findAll(Class<T> clazz) {
        return tmpl().origin().loadAll(clazz);
    }

    @Override
    public <T extends Entity> T save(T entity) {
        tmpl().origin().save(entity);
        return entity;
    }

    @Override
    public <T extends Entity> T saveOrUpdate(T entity) {
        return tmpl().origin().merge(entity);
    }

    @Override
    public <T extends Entity> T update(T entity) {
        return tmpl().origin().merge(entity);
    }

    @Override
    public <T extends Entity> T delete(T entity) {
        tmpl().origin().delete(entity);
        return entity;
    }

    /**
     * セッションキャッシュ中の永続化されていないエンティティを全てDBと同期(SQL発行)します。
     * <p>SQL発行タイミングを明確にしたい箇所で呼び出すようにしてください。バッチ処理などでセッションキャッシュが
     * メモリを逼迫するケースでは#flushAndClearを定期的に呼び出してセッションキャッシュの肥大化を防ぐようにしてください。
     */
    public OrmRepository flush() {
        tmpl().origin().flush();
        return this;
    }

    /**
     * セッションキャッシュ中の永続化されていないエンティティをDBと同期化した上でセッションキャッシュを初期化します。
     * <p>大量の更新が発生するバッチ処理などでは暗黙的に保持されるセッションキャッシュがメモリを逼迫して
     * 大きな問題を引き起こすケースが多々見られます。定期的に本処理を呼び出してセッションキャッシュの
     * サイズを定量に維持するようにしてください。
     */
    public OrmRepository flushAndClear() {
        tmpl().origin().flush();
        tmpl().origin().clear();
        return this;
    }

    /** Hibernateコンポーネントを生成するための設定情報を表現します。 */
    @Data
    public static class OrmRepositoryConfig {
        /** 接続するDBのDialect */
        private String dialect = "org.hibernate.dialect.H2Dialect";
        /** スキーマ紐付け対象とするパッケージ。(annotatedClassesとどちらかを設定) */
        private String packageToScan;
        /** SQLログを表示する時はtrue */
        private boolean showSql;
        /** DDLを自動生成して流す時はtrue */
        private boolean createDrop;
        /** Entityとして登録するクラス。(packageToScanとどちらかを設定) */
        private Class<?>[] annotatedClasses;
        /** クエリ置換設定 */
        private String substitutions = "true 1, false 0";

        public LocalSessionFactoryBean sessionFactory(final DataSource dataSource, final OrmInterceptor interceptor) {
            LocalSessionFactoryBean bean = new LocalSessionFactoryBean();
            bean.setDataSource(dataSource);
            bean.setAnnotatedClasses(annotatedClasses);
            bean.setPackagesToScan(packageToScan != null ? packageToScan.split(",") : null);
            bean.setHibernateProperties(hibernateProperties());
            bean.setPhysicalNamingStrategy(new OrmNamingStrategy());
            bean.setImplicitNamingStrategy(ImplicitNamingStrategyJpaCompliantImpl.INSTANCE);
            bean.setEntityInterceptor(interceptor);
            return bean;
        }

        public Properties hibernateProperties() {
            Properties prop = new Properties();
            prop.put("hibernate.dialect", dialect);
            prop.put("hibernate.show_sql", showSql);
            prop.put("hibernate.query.substitutions", substitutions);
            prop.put("hibernate.hbm2ddl.auto", createDrop ? "create-drop" : "validate");
            return prop;
        }

        public HibernateTransactionManager transactionManager(
                final SessionFactory sessionFactory) {
            return new HibernateTransactionManager(sessionFactory);
        }
    }

    /** Hibernate5用のPhysicalNamingStrategy。 */
    public static class OrmNamingStrategy implements PhysicalNamingStrategy {
        private SpringNamingStrategy strategy = new SpringNamingStrategy();

        @Override
        public Identifier toPhysicalCatalogName(Identifier identifier, JdbcEnvironment jdbcEnv) {
            return convert(identifier);
        }

        @Override
        public Identifier toPhysicalColumnName(Identifier identifier, JdbcEnvironment jdbcEnv) {
            return convert(identifier);
        }

        @Override
        public Identifier toPhysicalSchemaName(Identifier identifier, JdbcEnvironment jdbcEnv) {
            return convert(identifier);
        }

        @Override
        public Identifier toPhysicalSequenceName(Identifier identifier, JdbcEnvironment jdbcEnv) {
            return convert(identifier);
        }

        @Override
        public Identifier toPhysicalTableName(Identifier identifier, JdbcEnvironment jdbcEnv) {
            return convert(identifier);
        }

        private Identifier convert(Identifier identifier) {
            if (identifier == null || StringUtils.isBlank(identifier.getText())) {
                return identifier;
            }
            return Identifier.toIdentifier(strategy.tableName(identifier.getText()));
        }
    }

}
