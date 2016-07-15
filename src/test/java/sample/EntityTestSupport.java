package sample;

import java.time.Clock;
import java.util.*;
import java.util.function.Supplier;

import javax.persistence.*;
import javax.sql.DataSource;

import org.junit.*;
import org.springframework.orm.jpa.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import sample.context.*;
import sample.context.Entity;
import sample.context.actor.ActorSession;
import sample.context.orm.*;
import sample.context.orm.DefaultRepository.DefaultDataSourceConfig;
import sample.context.orm.SystemRepository.SystemDataSourceConfig;
import sample.model.*;
import sample.support.MockDomainHelper;

/**
 * Spring コンテナを用いない JPA のみに特化した検証用途。
 * <p>model パッケージでのみ利用してください。
 */
public class EntityTestSupport {
    protected Clock clock = Clock.systemDefaultZone();
    protected Timestamper time;
    protected BusinessDayHandler businessDay;
    protected PasswordEncoder encoder;
    protected ActorSession session;
    protected MockDomainHelper dh;
    protected EntityManagerFactory emf;
    protected DefaultRepository rep;
    protected PlatformTransactionManager txm;
    protected EntityManagerFactory emfSystem;
    protected SystemRepository repSystem;
    protected PlatformTransactionManager txmSystem;
    protected DataFixtures fixtures;

    /** テスト対象とするパッケージパス(通常はtargetEntitiesの定義を推奨) */
    private String packageToScan = "sample";
    /** テスト対象とするEntityクラス一覧 */
    private List<Class<?>> targetEntities = new ArrayList<>();

    @Before
    public final void setup() {
        setupPreset();
        dh = new MockDomainHelper(clock);
        time = dh.time();
        session = dh.actorSession();
        businessDay = new BusinessDayHandler();
        businessDay.setTime(time);
        encoder = new BCryptPasswordEncoder();
        setupRepository();
        setupDataFixtures();
        before();
    }

    /** 設定事前処理。repインスタンス生成前 */
    protected void setupPreset() {
        // 各Entity検証で上書きしてください
    }

    /** 事前処理。repインスタンス生成後 */
    protected void before() {
        // 各Entity検証で上書きしてください
    }

    /**
     * {@link #setupPreset()}内で対象Entityを指定してください。
     * (targetEntitiesといずれかを設定する必要があります)
     */
    protected void targetPackage(String packageToScan) {
        this.packageToScan = packageToScan;
    }

    /**
     * {@link #setupPreset()}内で対象Entityを指定してください。
     * (targetPackageといずれかを設定する必要があります)
     */
    protected void targetEntities(Class<?>... list) {
        if (list != null) {
            this.targetEntities = Arrays.asList(list);
        }
    }

    /**
     * {@link #setupPreset()}内で利用したいClockを指定してください。
     */
    protected void clock(Clock clock) {
        this.clock = clock;
    }

    /**
     * {@link #before()}内でモック設定値を指定してください。
     */
    protected void setting(String id, String value) {
        dh.setting(id, value);
    }

    @After
    public void cleanup() {
        emf.close();
        emfSystem.close();
    }

    protected void setupRepository() {
        EntityManagerFactory emf = createEntityManagerFactory();
        rep = new DefaultRepository();
        rep.setDh(dh);
        rep.setEm(SharedEntityManagerCreator.createSharedEntityManager(emf));
        repSystem = new SystemRepository();
        repSystem.setDh(dh);
        repSystem.setEm(SharedEntityManagerCreator.createSharedEntityManager(emfSystem));
    }

    protected void setupDataFixtures() {
        fixtures = new DataFixtures();
        fixtures.setTime(time);
        fixtures.setBusinessDay(businessDay);
        fixtures.setEncoder(encoder);
        fixtures.setRep(rep);
        fixtures.setTx(txm);
        fixtures.setRepSystem(repSystem);
        fixtures.setTxSystem(txmSystem);
    }

    private EntityManagerFactory createEntityManagerFactory() {
        DataSource ds = EntityTestFactory.dataSource();
        DefaultDataSourceConfig config = new DefaultDataSourceConfig();
        config.getJpa().setShowSql(true);
        config.getJpa().getHibernate().setDdlAuto("create-drop");
        if (targetEntities.isEmpty()) {
            config.getJpa().setPackageToScan(packageToScan);
        } else {
            config.getJpa().setAnnotatedClasses(targetEntities.toArray(new Class[0]));
        }
        
        //TODO: はめ込み
        OrmInterceptor interceptor = new OrmInterceptor();
        interceptor.setTime(time);
        interceptor.setSession(session);
        
        LocalContainerEntityManagerFactoryBean emfBean = config.entityManagerFactoryBean(ds);
        emfBean.afterPropertiesSet();
        emf = emfBean.getObject();
        txm = config.transactionManager(emf);
        
        SystemDataSourceConfig configSystem = new SystemDataSourceConfig();
        configSystem.getJpa().setShowSql(true);
        configSystem.getJpa().getHibernate().setDdlAuto("create-drop");
        if (targetEntities.isEmpty()) {
            configSystem.getJpa().setPackageToScan(packageToScan);
        } else {
            configSystem.getJpa().setAnnotatedClasses(targetEntities.toArray(new Class[0]));
        }
        LocalContainerEntityManagerFactoryBean emfSystemBean = configSystem.systemEntityManagerFactoryBean(ds);
        emfSystemBean.afterPropertiesSet();
        emfSystem = emfSystemBean.getObject();
        txmSystem = configSystem.systemTransactionManager(emfSystem);
        return emf;
    }

    // 簡易コンポーネントFactory
    public static class EntityTestFactory {
        private static Optional<DataSource> ds = Optional.empty();

        static synchronized DataSource dataSource() {
            return ds.orElseGet(() -> {
                ds = Optional.of(createDataSource());
                return ds.get();
            });
        }

        private static DataSource createDataSource() {
            OrmDataSourceConfig ds = new OrmDataSourceConfig();
            ds.setUrl("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
            ds.setUsername("");
            ds.setPassword("");
            return ds.dataSource();
        }
    }

    /** トランザクション処理を行います。 */
    public <T> T tx(Supplier<T> callable) {
        return new TransactionTemplate(txm).execute((status) -> {
            T ret = callable.get();
            if (ret instanceof Entity) {
                ret.hashCode(); // for lazy loading
            }
            return ret;
        });
    }

    public void tx(Runnable command) {
        tx(() -> {
            command.run();
            rep.flush();
            return true;
        });
    }

    /** トランザクション処理を行います。(System) */
    public <T> T txSystem(Supplier<T> callable) {
        return new TransactionTemplate(txmSystem).execute((status) -> {
            T ret = callable.get();
            if (ret instanceof Entity) {
                ret.hashCode(); // for lazy loading
            }
            return ret;
        });
    }

    public void txSystem(Runnable command) {
        txSystem(() -> {
            command.run();
            return true;
        });
    }
}
