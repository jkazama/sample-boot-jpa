package sample.support;

import java.util.*;

import sample.context.*;
import sample.context.actor.ActorSession;
import sample.util.TimePoint;

/** モックテスト用のドメインヘルパー */
public class MockDomainHelper extends DomainHelper {

	private Map<String, String> settingMap = new HashMap<>();
	
	public MockDomainHelper() {
		this(new TimePoint());
	}
	
	public MockDomainHelper(final TimePoint mockDay) {
		setActorSession(new ActorSession());
		setTime(new Timestamper(mockDay));
		setSettingHandler(new AppSettingHandler(settingMap));
	}
	
	public MockDomainHelper setting(String id, String value) {
		settingMap.put(id, value);
		return this;
	}
	
}
