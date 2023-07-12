package sample.util;

import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import sample.context.ValidationException;

/**
 * Bean Validation Utility.
 */
@RequiredArgsConstructor(staticName = "of")
public class BeanValidator {
    private final Validator validator;

    public void verify(Object bean) {
        var errors = this.validator.validate(bean);
        if (!errors.isEmpty()) {
            throw ValidationException.of(errors);
        }
    }
}
