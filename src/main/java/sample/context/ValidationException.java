package sample.context;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import jakarta.validation.ConstraintViolation;

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
        warns = Warns.init(message);
    }

    /**
     * Use this in cases where you want to notify a business exception that is
     * subordinate to a field.
     */
    public ValidationException(String field, String message) {
        super(message);
        warns = Warns.init(field, message);
    }

    /**
     * Use this in cases where you want to notify a business exception that is
     * subordinate to a field.
     */
    public ValidationException(String field, String message, String[] messageArgs) {
        super(message);
        warns = Warns.init(field, message, messageArgs);
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
        this.warns = Warns.init();
        errors.forEach((v) -> warns.add(v.getPropertyPath().toString(), v.getMessage()));
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

    public static ValidationException of(String message) {
        return new ValidationException(message);
    }

    public static ValidationException of(String message, String[] messageArgs) {
        return new ValidationException(null, message, messageArgs);
    }

    public static ValidationException of(String field, String message) {
        return new ValidationException(field, message);
    }

    public static ValidationException of(String field, String message, String[] messageArgs) {
        return new ValidationException(field, message, messageArgs);
    }

    public static ValidationException of(Warns warns) {
        return new ValidationException(warns);
    }

    public static ValidationException of(Set<ConstraintViolation<Object>> errors) {
        return new ValidationException(errors);
    }

    /** Business Exception Information. */
    public static class Warns implements Serializable {
        private static final long serialVersionUID = 1L;
        private List<Warn> list = new ArrayList<>();

        private Warns() {
        }

        public Warns add(String message) {
            list.add(new Warn(null, message, null));
            return this;
        }

        public Warns add(String field, String message) {
            list.add(new Warn(field, message, null));
            return this;
        }

        public Warns add(String field, String message, String[] messageArgs) {
            list.add(new Warn(field, message, messageArgs));
            return this;
        }

        public Optional<Warn> head() {
            return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
        }

        public List<Warn> list() {
            return list;
        }

        public boolean nonEmpty() {
            return !list.isEmpty();
        }

        public static Warns init() {
            return new Warns();
        }

        public static Warns init(String message) {
            return init().add(message);
        }

        public static Warns init(String field, String message) {
            return init().add(field, message);
        }

        public static Warns init(String field, String message, String[] messageArgs) {
            return init().add(field, message, messageArgs);
        }

    }

    /** Represents a field scope business exception token. */
    public static record Warn(
            /** business exception field key */
            String field,
            /** business exception message */
            String message,
            /** business exception message arguments */
            String[] messageArgs) {

        /** true on global exceptions not dependent on field */
        public boolean global() {
            return field == null;
        }
    }

}
