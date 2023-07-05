package sample.model.constraints;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.*;

import jakarta.validation.*;
import jakarta.validation.constraints.*;

/**
 * 料率を表現する制約注釈。
 */
@Documented
@Constraint(validatedBy = {})
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
@Retention(RUNTIME)
@ReportAsSingleViolation
@Digits(integer = 1, fraction = 2)
@DecimalMin("0.00")
public @interface RatioEmpty {
    String message() default "{error.domain.ratio}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    @OverridesAttribute(constraint = Digits.class, name = "integer")
    int integer() default 1;

    @OverridesAttribute(constraint = Digits.class, name = "fraction")
    int fraction() default 2;

    @OverridesAttribute(constraint = DecimalMin.class, name = "value")
    String min() default "0.00";

    @Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
    @Retention(RUNTIME)
    @Documented
    public @interface List {
        RatioEmpty[] value();
    }
}
