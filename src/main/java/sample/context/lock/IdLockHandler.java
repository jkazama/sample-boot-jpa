package sample.context.lock;

import java.io.Serializable;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

import lombok.Value;
import sample.InvocationException;

/**
 * ID単位のロックを表現します。
 * low: ここではシンプルに口座単位のIDロックのみをターゲットにします。
 * low: 通常はDBのロックテーブルに"for update"要求で悲観的ロックをとったりしますが、サンプルなのでメモリロックにしてます。
 */
public class IdLockHandler {

    private ConcurrentMap<Serializable, ReentrantReadWriteLock> lockMap = new ConcurrentHashMap<>();

    /** IDロック上で処理を実行します。 */
    public void call(Serializable id, LockType lockType, final Runnable command) {
        call(id, lockType, () -> {
            command.run();
            return true;
        });
    }

    public <T> T call(Serializable id, LockType lockType, final Supplier<T> callable) {
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
            throw new InvocationException("error.Exception", e);
        } finally {
            unlock(id);
        }
    }

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

    /**
     * ロック種別を表現するEnum。
     */
    public static enum LockType {
        /** 読み取り専用ロック */
        Read,
        /** 読み書き専用ロック */
        Write;

        public boolean isRead() {
            return !isWrite();
        }

        public boolean isWrite() {
            return this == Write;
        }
    }

    /** IdLock の対象と種別のペアを表現します。 */
    @Value
    public static class IdLockPair {
        private Serializable id;
        private LockType lockType;
    }
    
}
