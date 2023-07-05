package sample.support;

import java.time.Clock;
import java.util.HashMap;
import java.util.Map;

import sample.context.DomainHelper;
import sample.context.Timestamper;
import sample.context.actor.ActorSession;
import sample.context.support.AppSettingHandler;

/** モックテスト用のドメインヘルパー */
public class MockDomainHelper extends DomainHelper {

    private Map<String, String> settingMap = new HashMap<>();

    public MockDomainHelper() {
        this(Clock.systemDefaultZone());
    }

    public MockDomainHelper(final Clock mockClock) {
        setActorSession(new ActorSession());
        setTime(new Timestamper(mockClock));
        setSettingHandler(new AppSettingHandler(settingMap));
    }

    public MockDomainHelper setting(String id, String value) {
        settingMap.put(id, value);
        return this;
    }

}
