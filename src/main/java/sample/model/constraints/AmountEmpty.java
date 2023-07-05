package sample.model.constraints;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.*;

import jakarta.validation.*;
import jakarta.validation.constraints.Digits;

/**
 * 金額を表現する制約注釈。
 */
@Documented
@Constraint(validatedBy = {})
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
@Retention(RUNTIME)
@ReportAsSingleViolation
@Digits(integer = 16, fraction = 4)
public @interface AmountEmpty {
    String message() default "{error.domain.amount}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    @OverridesAttribute(constraint = Digits.class, name = "integer")
    int integer() default 16;

    @OverridesAttribute(constraint = Digits.class, name = "fraction")
    int fraction() default 4;

    @Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
    @Retention(RUNTIME)
    @Documented
    public @interface List {
        AmountEmpty[] value();
    }
}
