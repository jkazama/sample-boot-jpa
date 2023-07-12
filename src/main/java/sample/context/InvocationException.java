package sample.context;

/**
 * Expresses an execution exception at processing.
 * <p>
 * Use for the purpose of wrapping unrecoverable system exceptions.
 */
public class InvocationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public InvocationException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvocationException(String message) {
        super(message);
    }

    public InvocationException(Throwable cause) {
        super(cause);
    }

    public static InvocationException of(String message, Throwable cause) {
        return new InvocationException(message, cause);
    }

    public static InvocationException of(String message) {
        return new InvocationException(message);
    }

    public static InvocationException of(Throwable cause) {
        return new InvocationException(cause);
    }

}
