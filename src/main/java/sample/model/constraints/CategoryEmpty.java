package sample.model.constraints;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.*;

import jakarta.validation.*;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import sample.util.Regex;

/**
 * 各種カテゴリ/区分(必須)を表現する制約注釈。
 */
@Documented
@Constraint(validatedBy = {})
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
@Retention(RUNTIME)
@ReportAsSingleViolation
@Size
@Pattern(regexp = "")
public @interface CategoryEmpty {
    String message() default "{error.domain.category}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    @OverridesAttribute(constraint = Size.class, name = "max")
    int max() default 30;

    @OverridesAttribute(constraint = Pattern.class, name = "regexp")
    String regexp() default Regex.rAscii;

    @OverridesAttribute(constraint = Pattern.class, name = "flags")
    Pattern.Flag[] flags() default {};

    @Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
    @Retention(RUNTIME)
    @Documented
    public @interface List {
        CategoryEmpty[] value();
    }

}
