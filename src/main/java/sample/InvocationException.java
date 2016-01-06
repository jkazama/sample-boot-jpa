package sample;

/**
 * 処理時の実行例外を表現します。
 * <p>復旧不可能なシステム例外をラップする目的で利用してください。
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

}
