package sample.util;

import java.util.Collection;

import lombok.Builder;

/** Represents a field scope business exception token. */
@Builder
public record Warn(
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

    public static Warn of(String message, String... messageArgs) {
        return new Warn(null, message, messageArgs);
    }

    public static Warn of(String message, Collection<String> messageArgs) {
        return new Warn(null, message, messageArgs.toArray(String[]::new));
    }

    public static Warn ofField(String field, String message, String... messageArgs) {
        return new Warn(field, message, messageArgs);
    }

    public static Warn ofField(String field, String message, Collection<String> messageArgs) {
        return new Warn(null, message, messageArgs.toArray(String[]::new));
    }
}
