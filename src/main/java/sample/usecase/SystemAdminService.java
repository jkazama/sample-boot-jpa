package sample.usecase;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sample.context.AppSetting;
import sample.context.AppSetting.FindAppSetting;
import sample.context.audit.*;
import sample.context.audit.AuditActor.FindAuditActor;
import sample.context.audit.AuditEvent.FindAuditEvent;
import sample.context.orm.*;

/**
 * The use case processing for the system domain in the organization.
 */
@Service
public class SystemAdminService extends ServiceSupport {

    @Autowired
    private SystemRepository rep;

    @Transactional(SystemRepository.BeanNameTx)
    public PagingList<AuditActor> findAuditActor(FindAuditActor p) {
        return AuditActor.find(rep, p);
    }

    @Transactional(SystemRepository.BeanNameTx)
    public PagingList<AuditEvent> findAuditEvent(FindAuditEvent p) {
        return AuditEvent.find(rep, p);
    }

    @Transactional(SystemRepository.BeanNameTx)
    public List<AppSetting> findAppSetting(FindAppSetting p) {
        return AppSetting.find(rep, p);
    }

    public void changeAppSetting(String id, String value) {
        audit().audit("Change application setting information.", () -> dh().settingSet(id, value));
    }

    public void processDay() {
        audit().audit("Porward day.", () -> dh().time().proceedDay(businessDay().day(1)));
    }

}
