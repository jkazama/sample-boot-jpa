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
 * アプリケーション層のサービスメール送信を行います。
 * <p>独自にトランザクションを管理するので、サービスのトランザクション内で
 * 呼び出さないように注意してください。
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

    /** トランザクション処理を実行します。 */
    private <T> T tx(Supplier<T> callable) {
        return ServiceUtils.tx(tx, callable);
    }

    /** トランザクション処理を実行します。 */
    private void tx(Runnable command) {
        ServiceUtils.tx(tx, command);
    }

    /** 出金依頼受付メールを送信します。 */
    public void sendWithdrawal(final CashInOut cio) {
        //low: サンプルなので未実装。実際は独自にトランザクションを貼って処理を行う
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
