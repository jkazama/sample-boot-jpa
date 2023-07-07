package sample.usecase;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import sample.context.Dto;
import sample.context.audit.AuditHandler;
import sample.context.lock.IdLockHandler;
import sample.context.orm.OrmRepository;
import sample.context.orm.TxTemplate;
import sample.model.BusinessDayHandler;
import sample.model.asset.CashInOut;
import sample.model.asset.CashInOut.RegCashOut;
import sample.model.constraints.AbsAmount;
import sample.model.constraints.Currency;
import sample.usecase.event.NotificationEvent;
import sample.usecase.event.type.NotificationType;

/**
 * Customer use case processing for the asset domain.
 */
@Service
@RequiredArgsConstructor
public class AssetService {
    private final OrmRepository rep;
    private final PlatformTransactionManager txm;
    private final AuditHandler audit;
    private final IdLockHandler idLock;
    private final BusinessDayHandler businessDay;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Searches for unprocessed transfer request information.
     * low: For reference system, @Transactional is sufficient for cases where
     * account locking is not required.
     * low: CashInOut is information overload, but it is sometimes difficult to
     * identify the target of disclosure at the application layer, The final
     * decision is left to the UI layer.
     */
    public List<CashInOut> findUnprocessedCashOut() {
        String accountId = rep.dh().actor().id();
        return TxTemplate.of(txm).readIdLock(idLock, accountId).tx(() -> {
            return CashInOut.findUnprocessed(rep, accountId);
        });
    }

    /**
     * Transfer Withdrawal Request.
     * low: conscious of not returning more information than necessary to the UI
     * layer due to the risk of disclosure.
     * low: Audit logging is only performed for update use cases where the state can
     * be changed.
     * low: Clearly separate transaction boundaries so that emails are not skipped
     * when rollbacks occur.
     *
     * @return cashInOutId
     */
    public String withdraw(final UserRegCashOut param) {
        String accountId = rep.dh().actor().id();
        return audit.audit("asset", "withdraw", List.of(accountId), () -> {
            // low: Account ID lock (WRITE) and transaction to process transfers
            CashInOut cio = TxTemplate.of(txm).writeIdLock(idLock, accountId).tx(() -> {
                return CashInOut.withdraw(rep, businessDay, param.to(accountId));
            });
            // low: After the transaction is finalized, you will be notified by e-mail that
            // your withdrawal request has been accepted.
            eventPublisher.publishEvent(
                    NotificationEvent.of(NotificationType.FINISH_REQUEST_WITHDRAW, cio));
            return cio.getCashInOutId();
        });
    }

    @Builder
    public static record UserRegCashOut(
            @Currency String currency,
            @AbsAmount BigDecimal absAmount) implements Dto {
        public RegCashOut to(String accountId) {
            return RegCashOut.builder()
                    .accountId(accountId)
                    .currency(this.currency)
                    .absAmount(this.absAmount)
                    .build();
        }
    }

}
