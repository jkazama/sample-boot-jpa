package sample.model.constraints;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.*;

import jakarta.validation.*;
import jakarta.validation.constraints.*;

/**
 * メールアドレス(必須)を表現する制約注釈。
 * low: とりあえずHibernateのEmailValidatorを利用しますが、恐らく最終的に
 * 固有のConstraintValidatorを作らされる事になると思います。
 */
@Documented
@Constraint(validatedBy = {})
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
@Retention(RUNTIME)
@ReportAsSingleViolation
@NotBlank
@Size
@Pattern(regexp = "")
public @interface Email {
    String message() default "{error.domain.email}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    @OverridesAttribute(constraint = Size.class, name = "max")
    int max() default 256;

    @OverridesAttribute(constraint = Pattern.class, name = "regexp")
    String regexp() default ".*";

    @OverridesAttribute(constraint = Pattern.class, name = "flags")
    Pattern.Flag[] flags() default {};

    @Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
    @Retention(RUNTIME)
    @Documented
    public @interface List {
        Email[] value();
    }
}
