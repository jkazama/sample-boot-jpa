package sample.util;

import java.util.function.Consumer;

import sample.context.ValidationException;
import sample.context.ValidationException.Warns;

/**
 * Construction concept of the examination exception.
 */
public final class AppValidator {
    private Warns warns = Warns.init();

    public static void validate(Consumer<AppValidator> proc) {
        var validator = new AppValidator();
        proc.accept(validator);
        validator.verify();
    }

    /** An global exception stacks inside if valid is false. */
    public AppValidator check(boolean valid, String message) {
        if (!valid) {
            warns.add(message);
        }
        return this;
    }

    /** An field exception stacks inside if valid is false. */
    public AppValidator checkField(boolean valid, String field, String message) {
        if (!valid) {
            warns.add(field, message);
        }
        return this;
    }

    /** An global exception occurs if valid is false. */
    public AppValidator verify(boolean valid, String message) {
        return check(valid, message).verify();
    }

    /** An field exception occurs if valid is false. */
    public AppValidator verifyField(boolean valid, String field, String message) {
        return checkField(valid, field, message).verify();
    }

    public AppValidator verify() {
        if (hasWarn()) {
            throw new ValidationException(warns);
        }
        return clear();
    }

    public boolean hasWarn() {
        return warns.nonEmpty();
    }

    public AppValidator clear() {
        warns.list().clear();
        return this;
    }

}
