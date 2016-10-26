package sample.usecase;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
import sample.context.lock.IdLockHandler.LockType;
import sample.context.orm.DefaultRepository;
import sample.model.asset.*;
import sample.model.asset.CashInOut.FindCashInOut;

/**
 * The use case processing for the asset domain in the organization.
 */
@Service
@Slf4j
public class AssetAdminService extends ServiceSupport {

    @Transactional(DefaultRepository.BeanNameTx)
    public List<CashInOut> findCashInOut(final FindCashInOut p) {
        return CashInOut.find(rep(), p);
    }

    public void closingCashOut() {
        audit().audit("Closing cash out.", () -> tx(() -> closingCashOutInTx()));
    }

    private void closingCashOutInTx() {
        //low: It is desirable to handle it to an account unit in a mass.
        //low: Divide paging by id sort and carry it out for a difference
        // because heaps overflow when just do it in large quantities.
        CashInOut.findUnprocessed(rep()).forEach(cio -> {
            idLock().call(cio.getAccountId(), LockType.Write, () -> {
                try {
                    cio.process(rep());
                    //low: Guarantee that SQL is carried out.
                    rep().flushAndClear();
                } catch (Exception e) {
                    log.error("[" + cio.getId() + "] Failure closing cash out.", e);
                    try {
                        cio.error(rep());
                        rep().flush();
                    } catch (Exception ex) {
                        //low: Keep it for a mention only for logger which is a double obstacle. (probably DB is caused)
                    }
                }
            });
        });
    }

    /**
     * <p>Reflect the cashflow that reached an account day in the balance.
     */
    public void realizeCashflow() {
        audit().audit("Realize cashflow.", () -> tx(() -> realizeCashflowInTx()));
    }

    private void realizeCashflowInTx() {
        //low: Expect the practice after the rollover day.
        LocalDate day = dh().time().day();
        for (final Cashflow cf : Cashflow.findDoRealize(rep(), day)) {
            idLock().call(cf.getAccountId(), LockType.Write, () -> {
                try {
                    cf.realize(rep());
                    rep().flushAndClear();
                } catch (Exception e) {
                    log.error("[" + cf.getId() + "] Failure realize cashflow.", e);
                    try {
                        cf.error(rep());
                        rep().flush();
                    } catch (Exception ex) {
                    }
                }
            });
        }
    }

}
