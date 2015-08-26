package sample.model.constraints;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.*;

import javax.validation.*;
import javax.validation.constraints.*;

import org.hibernate.validator.constraints.NotBlank;

import sample.util.Regex;

/**
 * 概要(必須)を表現する制約注釈。
 */
@Documented
@Constraint(validatedBy = {})
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
@Retention(RUNTIME)
@ReportAsSingleViolation
@NotBlank
@Size
@Pattern(regexp = "")
public @interface Outline {
	String message() default "{error.domain.outline}";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

	@OverridesAttribute(constraint = Size.class, name = "max")
	int max() default 200;

	@OverridesAttribute(constraint = Pattern.class, name = "regexp")
	String regexp() default Regex.rWord;

	@OverridesAttribute(constraint = Pattern.class, name = "flags")
	Pattern.Flag[] flags() default {};

	@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
	@Retention(RUNTIME)
	@Documented
	public @interface List {
		Outline[] value();
	}
}
