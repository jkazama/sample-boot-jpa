package sample.usecase;

import java.util.function.Supplier;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import sample.InvocationException;

/**
 * Serviceで利用されるユーティリティ処理。
 */
public abstract class ServiceUtils {

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
	
	public static void tx(PlatformTransactionManager tx, Runnable callable) {
		@SuppressWarnings("unused")
		boolean ret = tx(tx, () -> {
			callable.run();
			return true;
		});
	}

}
