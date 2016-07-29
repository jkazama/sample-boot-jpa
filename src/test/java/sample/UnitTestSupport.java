package sample;

import java.util.function.Supplier;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import sample.context.*;
import sample.context.actor.Actor;
import sample.context.actor.Actor.ActorRoleType;
import sample.context.orm.*;
import sample.model.DataFixtures;

/**
 * Springコンテナを用いたフルセットの検証用途に利用してください。
 * <p>主な利用用途としてはアプリケーション層の単体検証を想定しています。
 */
//low: メソッド毎にコンテナ初期化を望む時はDirtiesContextでClassMode.AFTER_EACH_TEST_METHODを利用
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@ActiveProfiles("test")
@Transactional
public abstract class UnitTestSupport {

    @Autowired
    protected DefaultRepository rep;
    @Autowired
    @Qualifier(DefaultRepository.BeanNameTx)
    protected PlatformTransactionManager txm;
    @Autowired
    protected SystemRepository repSystem;
    @Autowired
    @Qualifier(SystemRepository.BeanNameTx)
    protected PlatformTransactionManager txmSystem;

    @Autowired
    protected DataFixtures fixtures;
    @Autowired
    protected Timestamper time;

    /** 利用者として擬似ログインします */
    protected void loginUser(String id) {
        rep.dh().actorSession().bind(new Actor(id, ActorRoleType.User));
    }
    
    /** 社内利用者として擬似ログインします */
    protected void loginInternal(String id) {
        rep.dh().actorSession().bind(new Actor(id, ActorRoleType.Internal));
    }
    
    /** システム利用者として擬似ログインします */
    protected void loginSystem() {
        rep.dh().actorSession().bind(Actor.System);
    }
    
    /** トランザクション処理を行います。 */
    protected <T> T tx(PlatformTransactionManager txm, Supplier<T> callable) {
        return new TransactionTemplate(txm).execute((status) -> {
            T ret = callable.get();
            if (ret instanceof Entity) {
                ret.hashCode(); // for lazy loading
            }
            return ret;
        });
    }

    protected void tx(PlatformTransactionManager txm, Runnable command) {
        tx(txm, () -> {
            command.run();
            rep.flush();
            return true;
        });
    }
    protected <T> T tx(Supplier<T> callable) {
        return tx(txm, callable);
    }
    protected void tx(Runnable command) {
        tx(txm, command);
    }
    protected <T> T txSystem(Supplier<T> callable) {
        return tx(txmSystem, callable);
    }
    protected void txSystem(Runnable command) {
        tx(txmSystem, command);
    }

}
