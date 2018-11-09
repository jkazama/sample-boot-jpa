package sample.usecase.mail;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import sample.context.mail.MailHandler;
import sample.model.asset.CashInOut;
import sample.usecase.event.AppMailEvent;

/**
 * アプリケーション層のサービスメール送信を行います。
 * <p>AppMailEvent に応じたメール配信をおこないます。
 */
@Component
@Slf4j
public class ServiceMailDeliver {
    @SuppressWarnings("unused")
    private final MailHandler mail;

    public ServiceMailDeliver(MailHandler mail) {
        this.mail = mail;
    }

    /** メール配信要求を受け付けます。 */
    @EventListener(AppMailEvent.class)
    public void handleEvent(AppMailEvent<?> event) {
        switch (event.getMailType()) {
        case FinishRequestWithdraw:
            sendFinishRequestWithdraw((CashInOut)event.getValue());
            break;
        default:
            throw new IllegalStateException("サポートされないメール種別です。 [" + event + "]");
        }
    }

    private void sendFinishRequestWithdraw(CashInOut cio) {
        //low: この例ではログのみ出力
        log.info("メール送信が行われました。 [" + cio + "]");
    }

}
