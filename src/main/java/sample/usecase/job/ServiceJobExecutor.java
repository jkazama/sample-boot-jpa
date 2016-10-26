package sample.usecase.job;

import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import lombok.Setter;
import sample.context.orm.DefaultRepository;
import sample.usecase.ServiceUtils;

/**
 * Job executor of the application layer.
 * <p>Manage the transaction originally, please be careful not to call it in the transaction of the service.
 */
@Component
@Setter
public class ServiceJobExecutor {

    @Autowired
    private DefaultRepository rep;
    @Autowired
    @Qualifier(DefaultRepository.BeanNameTx)
    private PlatformTransactionManager tx;

    private <T> T tx(Supplier<T> callable) {
        return ServiceUtils.tx(tx, callable);
    }

    private void tx(Runnable command) {
        ServiceUtils.tx(tx, command);
    }

    public int callbackSample() {// for warning
        return tx(() -> {
            return rep.hashCode();
        });
    }

    public void commandSample() {// for warning
        tx(() -> {
            rep.hashCode();
        });
    }

}
