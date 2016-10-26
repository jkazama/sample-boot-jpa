package sample.usecase;

import java.util.List;

import org.springframework.stereotype.Service;

import sample.context.actor.Actor;
import sample.context.lock.IdLockHandler.LockType;
import sample.model.asset.CashInOut;
import sample.model.asset.CashInOut.RegCashOut;

/**
 * The customer use case processing for the asset domain.
 */
@Service
public class AssetService extends ServiceSupport {

    /** {@inheritDoc} */
    @Override
    protected Actor actor() {
        return ServiceUtils.actorUser(super.actor());
    }

    public List<CashInOut> findUnprocessedCashOut() {
        final String accId = actor().getId();
        return tx(accId, LockType.Read, () -> {
            return CashInOut.findUnprocessed(rep(), accId);
        });
    }

    public Long withdraw(final RegCashOut p) {
        return audit().audit("requesting a withdrawal.", () -> {
            p.setAccountId(actor().getId()); // The customer side overwrites in login users forcibly
            // low: Take account ID lock (WRITE) and transaction and handle transfer
            CashInOut cio = tx(actor().getId(), LockType.Write, () -> {
                return CashInOut.withdraw(rep(), businessDay(), p);
            });
            // low: this service e-mail it and notify user.
            mail().sendWithdrawal(cio);
            return cio.getId();
        });
    }

}
