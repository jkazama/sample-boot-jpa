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
import sample.context.orm.DefaultRepository.DefaultDataSourceProperties;
import sample.model.*;
import sample.support.*;

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
    }

    protected void setupRepository() {
        setupEntityManagerFactory();
        rep = new DefaultRepository();
        rep.setDh(SimpleObjectProvider.of(dh));
        rep.setInterceptor(SimpleObjectProvider.of(entityInterceptor()));
        rep.setEm(SharedEntityManagerCreator.createSharedEntityManager(emf));
    }

    protected void setupDataFixtures() {
        fixtures = new DataFixtures();
        fixtures.setTime(time);
        fixtures.setBusinessDay(businessDay);
        fixtures.setEncoder(encoder);
        fixtures.setRep(rep);
        fixtures.setTx(txm);
    }

    protected void setupEntityManagerFactory() {
        DataSource ds = EntityTestFactory.dataSource();
        DefaultDataSourceProperties props = new DefaultDataSourceProperties();
        props.getJpa().setShowSql(true);
        props.getJpa().getHibernate().setDdlAuto("create-drop");
        if (targetEntities.isEmpty()) {
            props.getJpa().setPackageToScan(packageToScan);
        } else {
            props.getJpa().setAnnotatedClasses(targetEntities.toArray(new Class[0]));
        }
        
        LocalContainerEntityManagerFactoryBean emfBean = props.entityManagerFactoryBean(ds);
        emfBean.afterPropertiesSet();
        emf = emfBean.getObject();
        txm = props.transactionManager(emf);
    }

    private OrmInterceptor entityInterceptor() {
        OrmInterceptor interceptor = new OrmInterceptor();
        interceptor.setTime(time);
        interceptor.setSession(session);
        return interceptor;
    }

    /** トランザクション処理を行います。 */
    protected <T> T tx(Supplier<T> callable) {
        return new TransactionTemplate(txm).execute((status) -> {
            T ret = callable.get();
            if (ret instanceof Entity) {
                ret.hashCode(); // for lazy loading
            }
            return ret;
        });
    }

    protected void tx(Runnable command) {
        tx(() -> {
            command.run();
            rep.flush();
            return true;
        });
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
            OrmDataSourceProperties ds = new OrmDataSourceProperties();
            ds.setUrl("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
            ds.setUsername("");
            ds.setPassword("");
            return ds.dataSource();
        }
    }
    
}
