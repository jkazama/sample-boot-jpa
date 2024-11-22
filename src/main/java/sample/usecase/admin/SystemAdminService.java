package sample.usecase.admin;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;

import lombok.RequiredArgsConstructor;
import sample.context.audit.AuditActor;
import sample.context.audit.AuditActor.FindAuditActor;
import sample.context.audit.AuditEvent;
import sample.context.audit.AuditEvent.FindAuditEvent;
import sample.context.audit.AuditHandler;
import sample.context.orm.OrmRepository;
import sample.context.orm.TxTemplate;
import sample.context.support.AppSetting;
import sample.context.support.AppSetting.FindAppSetting;
import sample.model.BusinessDayHandler;

/**
 * Internal use case processing for the system domain.
 */
@Service
@RequiredArgsConstructor
public class SystemAdminService {
    private final OrmRepository rep;
    private final PlatformTransactionManager txm;
    private final AuditHandler audit;
    private final BusinessDayHandler businessDay;

    /** Search actor audit logs. */
    public Page<AuditActor> findAuditActor(final FindAuditActor param) {
        return TxTemplate.of(txm).readOnly().tx(() -> {
            return AuditActor.find(rep, param);
        });
    }

    /** Search system event audit logs. */
    public Page<AuditEvent> findAuditEvent(final FindAuditEvent param) {
        return TxTemplate.of(txm).readOnly().tx(() -> {
            return AuditEvent.find(rep, param);
        });
    }

    /** Search the list of application settings. */
    public List<AppSetting> findAppSetting(final FindAppSetting param) {
        return TxTemplate.of(txm).readOnly().tx(() -> {
            return AppSetting.find(rep, param);
        });
    }

    /** Change application configuration information. */
    public void changeAppSetting(String id, String value) {
        audit.audit("system", "changeAppSetting", List.of(id), () -> {
            rep.dh().setting().change(id, value);
        });
    }

    /** Move forward with the business day. */
    public void forwardDay() {
        LocalDate currentDay = businessDay.day();
        LocalDate nextDay = businessDay.day(1);
        audit.audit("system", "forwardDay", List.of(currentDay, nextDay), () -> {
            rep.dh().time().forwardDay(nextDay);
        });
    }

}
