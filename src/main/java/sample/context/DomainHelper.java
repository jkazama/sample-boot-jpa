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
	
	/**
	 * @return ログイン中のユースケース利用者
	 */
	public Actor actor() {
		return actorSession().actor();
	}

	/**
	 * @return スレッドローカルスコープの利用者セッション
	 */
	public ActorSession actorSession() {
		return actorSession;
	}
	
	/**
	 * @return 日時ユーティリティ
	 */
	public Timestamper time() {
		return time;
	}

	/**
	 * @return アプリケーション設定情報
	 */
	public AppSetting setting(String id) {
		return settingHandler.setting(id);
	}

}
