package sample.context;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import sample.util.Warn;
import sample.util.Warns;

/**
 * Expresses a business exception.
 * <p>
 * ValidationException is a recoverable business exception, such as an input
 * exception or a state transition exception.
 * Due to its nature, it is output at the WARN level (not ERROR) in the log.
 * <p>
 * Multiple review exceptions can be held in global/field scope. When handling
 * multiple exceptions Warns should be used to initialize them.
 */
public class ValidationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final Warns warns;

    /**
     * Use in cases where you want to notify a global business exception that is not
     * dependent on a field.
     */
    public ValidationException(String message) {
        super(message);
        warns = Warns.of(message);
    }

    /**
     * Use in cases where you want to notify a global business exception that is not
     * dependent on a field.
     */
    public ValidationException(String message, Collection<String> messageArgs) {
        super(message);
        warns = Warns.of(message);
    }

    /**
     * Use this in cases where you want to notify a business exception that is
     * subordinate to a field.
     */
    public ValidationException(String field, String message) {
        super(message);
        warns = Warns.ofField(field, message);
    }

    /**
     * Use this in cases where you want to notify a business exception that is
     * subordinate to a field.
     */
    public ValidationException(String field, String message, Collection<String> messageArgs) {
        super(message);
        warns = Warns.ofField(field, message, messageArgs);
    }

    /**
     * Use this function in cases where multiple cases are to be notified of a
     * business exception.
     */
    public ValidationException(final Warns warns) {
        super(warns.head().map((v) -> v.message()).orElse(ErrorKeys.Exception));
        this.warns = warns;
    }

    /** BeanValidaton Use in cases where you want to notify a business exception. */
    public ValidationException(final Set<ConstraintViolation<Object>> errors) {
        super(errors.stream().findFirst().map(v -> v.getMessage()).orElse(ErrorKeys.Exception));
        this.warns = Warns.of();
        errors.forEach((v) -> warns.addField(v.getPropertyPath().toString(), v.getMessage()));
    }

    /** Returns a list of business exceptions that have occurred. */
    public List<Warn> list() {
        return warns.list();
    }

    @Override
    public String getMessage() {
        return warns.head().map((v) -> v.message()).orElse(ErrorKeys.Exception);
    }

    public String[] getMessageArgs() {
        return warns.head().map((v) -> v.messageArgs()).orElse(new String[0]);
    }

    public static ValidationException of(String message, String... messageArgs) {
        if (messageArgs == null) {
            return new ValidationException(null, message);
        } else {
            return new ValidationException(null, message, Arrays.asList(messageArgs));
        }
    }

    public static ValidationException of(String message, Collection<String> messageArgs) {
        return new ValidationException(null, message, messageArgs);
    }

    public static ValidationException ofField(String field, String message, String... messageArgs) {
        if (messageArgs == null) {
            return new ValidationException(field, message);
        } else {
            return new ValidationException(field, message, Arrays.asList(messageArgs));
        }
    }

    public static ValidationException ofField(String field, String message, Collection<String> messageArgs) {
        return new ValidationException(field, message, messageArgs);
    }

    public static ValidationException of(Warns warns) {
        return new ValidationException(warns);
    }

    public static ValidationException of(Set<ConstraintViolation<Object>> errors) {
        return new ValidationException(errors);
    }

}
