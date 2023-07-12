package sample.usecase.notification;

import java.util.Map;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import lombok.RequiredArgsConstructor;
import sample.context.mail.MailHandler;
import sample.context.mail.MailHandler.SendMail;
import sample.context.orm.OrmRepository;
import sample.context.orm.TxTemplate;
import sample.model.account.Account;
import sample.model.asset.CashInOut;
import sample.usecase.event.NotificationEvent;

/**
 * Processes event notification distribution at the application layer.
 * <p>
 * Notifications are sent according to NotificationEvent.
 */
@Component
@RequiredArgsConstructor
public class NotificationDeliver {
    private final OrmRepository rep;
    private final PlatformTransactionManager txm;
    private final MailHandler mail;

    /** Accepts notification requests. */
    @EventListener(NotificationEvent.class)
    public void handleEvent(NotificationEvent<?> event) {
        switch (event.notificationType()) {
            case FINISH_REQUEST_WITHDRAW:
                sendFinishRequestWithdraw((CashInOut) event.value());
                break;
            default:
                throw new IllegalArgumentException(
                        "Unsupported notification types. [" + event.notificationType() + "]");
        }
    }

    private void sendFinishRequestWithdraw(CashInOut cio) {
        // low: In the case of sending an e-mail, use a template for the body, etc.,
        // taking i18n into consideration.
        var account = account(cio.getAccountId());
        var bodyArgs = Map.of(
                "accountId", account.getAccountId(),
                "name", account.getName(),
                "cashInOutId", cio.getCashInOutId(),
                "amount", cio.getAbsAmount().toString());
        this.mail.send(SendMail.builder()
                .address(account.getMailAddress())
                .subject("foo")
                .body("bar")
                .bodyArgs(bodyArgs)
                .build());
    }

    private Account account(String accountId) {
        return TxTemplate.of(txm).readOnly().tx(() -> {
            return Account.load(rep, accountId);
        });
    }

}
