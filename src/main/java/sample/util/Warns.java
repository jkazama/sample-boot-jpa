package sample.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import lombok.Builder;

/** Business Exception Information. */
@Builder
public record Warns(List<Warn> list) {

    public Warns add(String message, String... messageArgs) {
        list.add(Warn.of(message, messageArgs));
        return this;
    }

    public Warns add(String message, Collection<String> messageArgs) {
        list.add(Warn.of(message, messageArgs));
        return this;
    }

    public Warns addField(String field, String message, String... messageArgs) {
        list.add(Warn.ofField(field, message, messageArgs));
        return this;
    }

    public Warns addField(String field, String message, Collection<String> messageArgs) {
        list.add(Warn.ofField(field, message, messageArgs));
        return this;
    }

    public Optional<Warn> head() {
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public boolean nonEmpty() {
        return !list.isEmpty();
    }

    public static Warns of() {
        return Warns.builder().list(new ArrayList<Warn>()).build();
    }

    public static Warns of(String message, String... messageArgs) {
        return of().add(message, messageArgs);
    }

    public static Warns of(String message, Collection<String> messageArgs) {
        return of().add(message, messageArgs);
    }

    public static Warns ofField(String field, String message, String... messageArgs) {
        return of().addField(field, message, messageArgs);
    }

    public static Warns ofField(String field, String message, Collection<String> messageArgs) {
        return of().addField(field, message, messageArgs);
    }

}
