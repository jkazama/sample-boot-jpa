package sample.usecase;

import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;

import sample.context.audit.AuditActor;
import sample.context.audit.AuditActor.FindAuditActor;
import sample.context.audit.AuditEvent;
import sample.context.audit.AuditEvent.FindAuditEvent;
import sample.context.audit.AuditHandler;
import sample.context.orm.TxTemplate;
import sample.context.orm.repository.SystemRepository;
import sample.context.support.AppSetting;
import sample.context.support.AppSetting.FindAppSetting;
import sample.model.BusinessDayHandler;

/**
 * システムドメインに対する社内ユースケース処理。
 */
@Service
public class SystemAdminService {

    private final SystemRepository rep;
    private final PlatformTransactionManager txm;
    private final AuditHandler audit;
    private final BusinessDayHandler businessDay;

    public SystemAdminService(
            SystemRepository rep,
            @Qualifier(SystemRepository.BeanNameTx) PlatformTransactionManager txm,
            AuditHandler audit,
            BusinessDayHandler businessDay) {
        this.rep = rep;
        this.txm = txm;
        this.audit = audit;
        this.businessDay = businessDay;
    }

    /** 利用者監査ログを検索します。 */
    public PagingList<AuditActor> findAuditActor(FindAuditActor p) {
        return TxTemplate.of(txm).readOnly().tx(
                () -> AuditActor.find(rep, p));
    }

    /** イベント監査ログを検索します。 */
    public PagingList<AuditEvent> findAuditEvent(FindAuditEvent p) {
        return TxTemplate.of(txm).readOnly().tx(
                () -> AuditEvent.find(rep, p));
    }

    /** アプリケーション設定一覧を検索します。 */
    public List<AppSetting> findAppSetting(FindAppSetting p) {
        return TxTemplate.of(txm).readOnly().tx(
                () -> AppSetting.find(rep, p));
    }

    public void changeAppSetting(String id, String value) {
        audit.audit("アプリケーション設定情報を変更する", () -> rep.dh().settingSet(id, value));
    }

    public void processDay() {
        audit.audit("営業日を進める", () -> rep.dh().time().proceedDay(businessDay.day(1)));
    }

}
