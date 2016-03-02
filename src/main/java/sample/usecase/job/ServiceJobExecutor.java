package sample.usecase.job;

import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import lombok.Setter;
import sample.context.orm.DefaultRepository;
import sample.usecase.ServiceUtils;

/**
 * アプリケーション層のジョブ実行を行います。
 * <p>独自にトランザクションを管理するので、サービスのトランザクション内で 呼び出さないように注意してください。
 */
@Component
@Setter
public class ServiceJobExecutor {

    @Autowired
    private DefaultRepository rep;
    @Autowired
    @Qualifier(DefaultRepository.BeanNameTx)
    private PlatformTransactionManager tx;

    /** トランザクション処理を実行します。 */
    private <T> T tx(Supplier<T> callable) {
        return ServiceUtils.tx(tx, callable);
    }

    /** トランザクション処理を実行します。 */
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
