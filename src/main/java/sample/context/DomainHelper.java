package sample.context;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.Setter;
import sample.context.actor.*;

/**
 * ドメイン処理を行う上で必要となるインフラ層コンポーネントへのアクセサを提供します。
 */
@Component
@Setter
public class DomainHelper {

	@Autowired
	private ActorSession actorSession;
	@Autowired
	private Timestamper time;
	@Autowired
	private AppSettingHandler settingHandler;
	
	/** ログイン中のユースケース利用者を取得します。 */
	public Actor actor() {
		return actorSession().actor();
	}

	/** スレッドローカルスコープの利用者セッションを取得します。 */
	public ActorSession actorSession() {
		return actorSession;
	}
	
	/** 日時ユーティリティを取得します。 */
	public Timestamper time() {
		return time;
	}

	/** アプリケーション設定情報を取得します。 */
	public AppSetting setting(String id) {
		return settingHandler.setting(id);
	}
	
	/** アプリケーション設定情報を設定します。 */
	public AppSetting settingSet(String id, String value) {
		return settingHandler.update(id, value);
	}	

}
