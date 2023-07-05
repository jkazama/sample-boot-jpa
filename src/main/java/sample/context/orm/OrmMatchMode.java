package sample.context.orm;

import java.util.function.Function;

/** MatchMode for Like clause */
public enum OrmMatchMode {
    START(v -> v + "%"),
    END(v -> "%" + v),
    ANYWHERE(v -> "%" + v + "%");

    private final Function<String, String> parser;

    private OrmMatchMode(Function<String, String> parser) {
        this.parser = parser;
    }

    public String toMatchString(String pattern) {
        return this.parser.apply(pattern);
    }
}
