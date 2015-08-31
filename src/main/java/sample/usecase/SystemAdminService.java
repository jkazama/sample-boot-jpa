package sample.usecase;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sample.context.AppSetting;
import sample.context.AppSetting.FindAppSetting;
import sample.context.audit.*;
import sample.context.audit.AuditActor.FindAuditActor;
import sample.context.audit.AuditEvent.FindAuditEvent;
import sample.context.orm.*;

/**
 * システムドメインに対する社内ユースケース処理。
 */
@Service
public class SystemAdminService extends ServiceSupport {

	private SystemRepository rep;
	
	/** 利用者監査ログを検索します。 */
	@Transactional(SystemRepository.beanNameTx)
	public PagingList<AuditActor> findAuditActor(FindAuditActor p) {
		return AuditActor.find(rep, p);
	}

	/** イベント監査ログを検索します。 */
	@Transactional(SystemRepository.beanNameTx)
	public PagingList<AuditEvent> findAuditEvent(FindAuditEvent p) {
		return AuditEvent.find(rep, p);
	}
	
	/** アプリケーション設定一覧を検索します。 */
	@Transactional(SystemRepository.beanNameTx)
	public List<AppSetting> findAppSetting(FindAppSetting p) {
		return AppSetting.find(rep, p);
	}
	
	public void changeAppSetting(String id, String value) {
		audit().audit("アプリケーション設定情報を変更する", () ->
			dh().settingSet(id, value));
	}
	
	public void processDay() {
		audit().audit("営業日を進める", () ->
			dh().time().proceedDay(businessDay().day(1)));
	}
	
}
