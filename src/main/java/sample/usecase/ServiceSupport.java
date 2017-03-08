package sample.usecase;

import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.Assert;

import lombok.Setter;
import sample.context.DomainHelper;
import sample.context.actor.Actor;
import sample.context.audit.AuditHandler;
import sample.context.lock.IdLockHandler;
import sample.context.lock.IdLockHandler.LockType;
import sample.context.orm.DefaultRepository;
import sample.model.BusinessDayHandler;
import sample.usecase.mail.ServiceMailDeliver;
import sample.usecase.report.ServiceReportExporter;

/**
 * The base class of the use case service.
 */
@Setter
public abstract class ServiceSupport {

    @Autowired
    private MessageSource msg;

    @Autowired
    private DomainHelper dh;
    @Autowired
    private DefaultRepository rep;
    @Autowired
    @Qualifier(DefaultRepository.BeanNameTx)
    private PlatformTransactionManager tx;
    @Autowired
    private IdLockHandler idLock;

    @Autowired
    private AuditHandler audit;
    @Autowired(required = false)
    private BusinessDayHandler businessDay;

    @Autowired(required = false)
    private ServiceMailDeliver mail;
    @Autowired(required = false)
    private ServiceReportExporter report;

    /** Execute transaction processing. */
    protected <T> T tx(Supplier<T> callable) {
        return ServiceUtils.tx(tx, callable);
    }

    /** Execute transaction processing. */
    protected void tx(Runnable command) {
        ServiceUtils.tx(tx, command);
    }

    /** Execute transaction processing with account lock. */
    protected <T> T tx(String accountId, LockType lockType, final Supplier<T> callable) {
        return idLock.call(accountId, lockType, () -> {
            return tx(callable);
        });
    }

    /** Execute transaction processing with account lock. */
    protected void tx(String accountId, LockType lockType, final Runnable callable) {
        idLock.call(accountId, lockType, () -> {
            tx(callable);
            return true;
        });
    }

    protected DomainHelper dh() {
        return dh;
    }

    protected DefaultRepository rep() {
        return rep;
    }

    protected IdLockHandler idLock() {
        return idLock;
    }

    protected AuditHandler audit() {
        return audit;
    }

    protected ServiceMailDeliver mail() {
        Assert.notNull(mail, "mail is not setup.");
        return mail;
    }

    protected ServiceReportExporter report() {
        Assert.notNull(report, "report is not setup.");
        return report;
    }

    protected String msg(String message) {
        return msg.getMessage(message, null, message, actor().getLocale());
    }

    protected Actor actor() {
        return dh.actor();
    }

    protected BusinessDayHandler businessDay() {
        Assert.notNull(businessDay, "businessDay is not setup.");
        return businessDay;
    }

}
