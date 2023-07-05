package sample.context.orm;

import java.io.Serializable;
import java.util.Optional;
import java.util.function.Supplier;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.Assert;

import sample.context.lock.IdLockHandler;
import sample.context.lock.IdLockHandler.IdLockPair;
import sample.context.lock.IdLockHandler.LockType;

/**
 * This is a simple utility for transactions.
 * <p>
 * Assumes support builder-like use of TransactionTemplate.
 * Please be sure to generate and use it for each transaction, rather than using
 * it over and over again.
 */
public class TxTemplate {
    private Optional<IdLockHandler> idLock = Optional.empty();
    private Optional<IdLockPair> IdLockPair = Optional.empty();
    private final TransactionTemplate tmpl;

    public TxTemplate(PlatformTransactionManager txm) {
        this.tmpl = new TransactionTemplate(txm);
    }

    public TransactionTemplate origin() {
        return tmpl;
    }

    /** Make it a reference-only transaction. */
    public TxTemplate readOnly() {
        this.tmpl.setReadOnly(true);
        return this;
    }

    /** Set the transaction type. */
    public TxTemplate propagation(Propagation propagation) {
        this.tmpl.setPropagationBehavior(propagation.value());
        return this;
    }

    /** Sets the transaction isolation level. */
    public TxTemplate isolation(Isolation isolation) {
        this.tmpl.setIsolationLevel(isolation.value());
        return this;
    }

    /** Set the transaction timeout (sec). */
    public TxTemplate timeout(int timeout) {
        this.tmpl.setTimeout(timeout);
        return this;
    }

    /** Sets the reference lock for the specified ID at the time of transaction */
    public TxTemplate readIdLock(IdLockHandler idLock, Serializable id) {
        Assert.notNull(id, "id is required.");
        this.idLock = Optional.ofNullable(idLock);
        this.IdLockPair = Optional.of(new IdLockPair(id, LockType.READ));
        return this;
    }

    /** Sets the write lock for the specified ID at the time of transaction */
    public TxTemplate writeIdLock(IdLockHandler idLock, Serializable id) {
        Assert.notNull(id, "id is required.");
        this.idLock = Optional.ofNullable(idLock);
        this.IdLockPair = Optional.of(new IdLockPair(id, LockType.WRITE));
        return this;
    }

    /** Performs transaction processing. */
    public void tx(Runnable runnable) {
        if (this.idLock.isPresent()) {
            this.idLock.get().call(this.IdLockPair.get().id(), this.IdLockPair.get().lockType(), () -> {
                tmpl.execute(status -> {
                    runnable.run();
                    return null;
                });
            });
        } else {
            tmpl.execute(status -> {
                runnable.run();
                return null;
            });
        }
    }

    /** Performs transaction processing. */
    public <T> T tx(Supplier<T> supplier) {
        if (this.idLock.isPresent()) {
            return this.idLock.get().call(this.IdLockPair.get().id(), this.IdLockPair.get().lockType(),
                    () -> tmpl.execute(status -> supplier.get()));
        } else {
            return tmpl.execute(status -> supplier.get());
        }
    }

    public static TxTemplate of(PlatformTransactionManager txm) {
        return new TxTemplate(txm);
    }

}
