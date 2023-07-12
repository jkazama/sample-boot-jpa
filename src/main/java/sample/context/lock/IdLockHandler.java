package sample.context.lock;

import java.io.Serializable;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

import org.springframework.stereotype.Component;

import sample.context.ErrorKeys;
import sample.context.InvocationException;

/**
 * Represents an ID lock.
 * low: Here we will simply target only account-by-account ID locks.
 * low: Normally, a pessimistic lock is obtained by a "for update" request to
 * the DB lock table, but since this is a sample, a memory lock is used.
 */
public interface IdLockHandler {

    /** Execute the process on the ID lock. */
    default void call(Serializable id, LockType lockType, final Runnable command) {
        call(id, lockType, () -> {
            command.run();
            return true;
        });
    }

    default <T> T call(Serializable id, LockType lockType, final Supplier<T> callable) {
        if (lockType.isWrite()) {
            writeLock(id);
        } else {
            readLock(id);
        }
        try {
            return callable.get();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw InvocationException.of(ErrorKeys.Exception, e);
        } finally {
            unlock(id);
        }
    }

    void writeLock(Serializable id);

    void readLock(Serializable id);

    void unlock(final Serializable id);

    public static enum LockType {
        READ,
        WRITE;

        public boolean isRead() {
            return !isWrite();
        }

        public boolean isWrite() {
            return this == WRITE;
        }
    }

    /** Represents a pair of IdLock targets and types. */
    public static record IdLockPair(
            Serializable id, LockType lockType) {
    }

    @Component
    public static class IdLockHandlerImpl implements IdLockHandler {
        private final ConcurrentMap<Serializable, ReentrantReadWriteLock> lockMap = new ConcurrentHashMap<>();

        public void writeLock(final Serializable id) {
            Optional.of(id).ifPresent((v) -> {
                idLock(v).writeLock().lock();
            });
        }

        private ReentrantReadWriteLock idLock(final Serializable id) {
            return lockMap.computeIfAbsent(id, v -> new ReentrantReadWriteLock());
        }

        public void readLock(final Serializable id) {
            Optional.of(id).ifPresent((v) -> {
                idLock(v).readLock().lock();
            });
        }

        public void unlock(final Serializable id) {
            Optional.of(id).ifPresent((v) -> {
                ReentrantReadWriteLock idLock = idLock(v);
                if (idLock.isWriteLockedByCurrentThread()) {
                    idLock.writeLock().unlock();
                } else {
                    idLock.readLock().unlock();
                }
            });
        }

    }
}
