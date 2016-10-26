package sample.usecase.mail;

import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import lombok.Setter;
import sample.context.mail.MailHandler;
import sample.context.orm.DefaultRepository;
import sample.model.asset.CashInOut;
import sample.usecase.ServiceUtils;

/**
 * Mail deliver of the application layer.
 * <p>Manage the transaction originally, please be careful not to call it in the transaction of the service.
 */
@Component
@Setter
public class ServiceMailDeliver {

    @Autowired
    private DefaultRepository rep;
    @Autowired
    @Qualifier(DefaultRepository.BeanNameTx)
    private PlatformTransactionManager tx;
    @Autowired
    private MailHandler mail;

    private <T> T tx(Supplier<T> callable) {
        return ServiceUtils.tx(tx, callable);
    }

    private void tx(Runnable command) {
        ServiceUtils.tx(tx, command);
    }

    public void sendWithdrawal(final CashInOut cio) {
        //low: It is nonimplement in being a sample.
    }

    public int callbackSample() {// for warning
        return tx(() -> {
            mail.hashCode();
            return rep.hashCode();
        });
    }

    public void commandSample() {// for warning
        tx(() -> {
            rep.hashCode();
        });
    }

}
