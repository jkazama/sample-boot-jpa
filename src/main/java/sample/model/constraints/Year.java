package sample.model.constraints;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.*;

import javax.validation.*;
import javax.validation.constraints.*;

import org.hibernate.validator.constraints.NotBlank;

/**
 * 年(必須)を表現する制約注釈。
 */
@Documented
@Constraint(validatedBy = {})
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
@Retention(RUNTIME)
@ReportAsSingleViolation
@NotBlank
@Size
@Pattern(regexp = "")
public @interface Year {
	String message() default "{error.domain.year}";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

	@OverridesAttribute(constraint = Size.class, name = "max")
	int max() default 4;

	@OverridesAttribute(constraint = Pattern.class, name = "regexp")
	String regexp() default "^\\d{4}$";

	@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
	@Retention(RUNTIME)
	@Documented
	public @interface List {
		Year[] value();
	}
}
