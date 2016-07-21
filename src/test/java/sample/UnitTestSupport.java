package sample;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import sample.context.Timestamper;
import sample.context.actor.Actor;
import sample.context.actor.Actor.ActorRoleType;
import sample.context.orm.DefaultRepository;
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
    
}
