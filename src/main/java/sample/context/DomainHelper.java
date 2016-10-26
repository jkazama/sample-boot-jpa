package sample.context;

import org.springframework.beans.factory.annotation.Autowired;

import lombok.Setter;
import sample.context.actor.*;

/**
 * The access to the domain infrastructure layer component which is necessary in handling it.
 */
@Setter
public class DomainHelper {

    @Autowired
    private ActorSession actorSession;
    @Autowired
    private Timestamper time;
    @Autowired
    private AppSettingHandler settingHandler;

    /** Return a login user. */
    public Actor actor() {
        return actorSession().actor();
    }

    /** Return the user session of the thread local scope. */
    public ActorSession actorSession() {
        return actorSession;
    }

    public Timestamper time() {
        return time;
    }

    public AppSetting setting(String id) {
        return settingHandler.setting(id);
    }

    public AppSetting settingSet(String id, String value) {
        return settingHandler.update(id, value);
    }

}
