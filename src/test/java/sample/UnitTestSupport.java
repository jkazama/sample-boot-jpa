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
 * The component unit test support class using the Spring container.
 * <p>The application layer test for a main use case.
 */
//low: When you expect container initialization every method, use ClassMode.AFTER_EACH_TEST_METHOD in DirtiesContext
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

    /** dummy-login as a user */
    protected void loginUser(String id) {
        rep.dh().actorSession().bind(new Actor(id, ActorRoleType.User));
    }
    
    /** dummy-login as a internal user */
    protected void loginInternal(String id) {
        rep.dh().actorSession().bind(new Actor(id, ActorRoleType.Internal));
    }
    
    /** dummy-login as a system user */
    protected void loginSystem() {
        rep.dh().actorSession().bind(Actor.System);
    }
    
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
