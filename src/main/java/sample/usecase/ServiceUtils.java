package sample.usecase;

import java.util.function.Supplier;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import sample.*;
import sample.ValidationException.ErrorKeys;
import sample.context.actor.Actor;

/**
 * Serviceで利用されるユーティリティ処理。
 */
public abstract class ServiceUtils {

    /** 戻り値を必要とするトランザクション処理を行います。 */
    public static <T> T tx(PlatformTransactionManager tx, Supplier<T> callable) {
        return new TransactionTemplate(tx).execute((status) -> {
            try {
                return callable.get();
            } catch (RuntimeException e) {
                throw (RuntimeException) e;
            } catch (Exception e) {
                throw new InvocationException("error.Exception", e);
            }
        });
    }

    /** 戻り値を必要としないトランザクション処理を行います。 */
    public static void tx(PlatformTransactionManager tx, Runnable callable) {
        @SuppressWarnings("unused")
        boolean ret = tx(tx, () -> {
            callable.run();
            return true;
        });
    }

    /** 匿名以外の利用者情報を返します。 */
    public static Actor actorUser(Actor actor) {
        if (actor.getRoleType().isAnonymous()) {
            throw new ValidationException(ErrorKeys.Authentication);
        }
        return actor;
    }

}
