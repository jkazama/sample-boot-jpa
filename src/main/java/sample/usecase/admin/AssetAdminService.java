package sample.usecase.admin;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import sample.context.audit.AuditHandler;
import sample.context.lock.IdLockHandler;
import sample.context.lock.IdLockHandler.LockType;
import sample.context.orm.TxTemplate;
import sample.context.orm.repository.DefaultRepository;
import sample.model.asset.CashInOut;
import sample.model.asset.CashInOut.FindCashInOut;
import sample.model.asset.Cashflow;

/**
 * Internal use case processing for asset domains.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AssetAdminService {
    private final DefaultRepository rep;
    private final PlatformTransactionManager txm;
    private final AuditHandler audit;
    private final IdLockHandler idLock;

    /**
     * Search for a transfer deposit/withdrawal request.
     * low: do not apply a READ lock on the split because it is cross-accounting.
     */
    public List<CashInOut> findCashInOut(final FindCashInOut p) {
        return TxTemplate.of(txm).readOnly().tx(() -> {
            return CashInOut.find(rep, p);
        });
    }

    /**
     * Close the withdrawal request.
     */
    public void closingCashOut() {
        audit.audit("asset", "closingCashOut", () -> {
            TxTemplate.of(txm).tx(() -> {
                closingCashOutInTx();
            });
        });
    }

    private void closingCashOutInTx() {
        // low: It is preferable to perform subsequent processing after filter bundling
        // on an account-by-account.
        // low: When processing a large number of cases, the heap will die if it is done
        // as it is, so paging is divided by id sorting and executed in increments.
        CashInOut.findUnprocessed(rep).forEach(cio -> {
            // low: It is up to the implementation of IdLockHandler to ensure that the locks
            // in the TX work properly.
            // If it is difficult to adjust, it is simpler to create a business halt time
            // (only processes that need IdLock are deactivated) and process them all at
            // once without locking.
            idLock.call(cio.getAccountId(), LockType.WRITE, () -> {
                try {
                    cio.process(rep);
                    // low: SQL publishing collateral. There is no interdependence in the
                    // information handled, and the session cache tends to leak, so it should be
                    // deleted each time.
                    rep.flushAndClear();
                } catch (Exception e) {
                    log.error("The closing process for a transfer withdrawal request failed.["
                            + cio.getCashInOutId() + "]", e);
                    try {
                        cio.error(rep);
                        rep.flush();
                    } catch (Exception ex) {
                        // low: Only logger is mentioned since it is a double failure.
                        // (probably caused by DB).
                    }
                }
            });
        });
    }

    /**
     * Realize cash flow.
     * <p>
     * Cash flows with delivery dates are reflected in the balance.
     */
    public void realizeCashflow() {
        // low: Assume execution after day forward.
        LocalDate day = rep.dh().time().day();
        audit.audit("asset", "realizeCashflow", List.of(day), () -> {
            Map<String, List<Cashflow>> cashflowsByAccount = Cashflow.findDoRealize(rep, day).stream()
                    .collect(Collectors.groupingBy(Cashflow::getAccountId));
            cashflowsByAccount.forEach((accountId, cashflows) -> {
                TxTemplate.of(txm).writeIdLock(idLock, accountId).tx(() -> {
                    cashflows.forEach(cf -> {
                        try {
                            cf.realize(rep);
                            rep.flushAndClear();
                        } catch (Exception e) {
                            log.error("Failed to realize cash flow.[" + cf.getCashflowId() + "]", e);
                            try {
                                cf.error(rep);
                                rep.flush();
                            } catch (Exception ex) {
                                // nothing.
                            }
                        }
                    });
                });
            });
        });
    }

}
