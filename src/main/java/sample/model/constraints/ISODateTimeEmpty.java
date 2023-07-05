package sample.model.constraints;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.*;

import jakarta.validation.*;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

/**
 * ISOフォーマットの日時を表現する制約注釈。
 * <p>
 * yyyy-MM-dd'T'HH:mm:ss.SSSZを想定します。
 */
@Documented
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
@Retention(RUNTIME)
@ReportAsSingleViolation
@DateTimeFormat(iso = ISO.DATE_TIME)
public @interface ISODateTimeEmpty {
    String message() default "{error.domain.ISODateTime}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    @Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
    @Retention(RUNTIME)
    @Documented
    public @interface List {
        ISODateTimeEmpty[] value();
    }
}
