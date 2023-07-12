package sample.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;

import org.junit.jupiter.api.Test;

import sample.context.ValidationException;

public class ValidatorTest {

    @Test
    public void checkLambda() {
        AppValidator.validate((v) -> {
            boolean anyCheck = true;
            v.checkField(anyCheck, "name", "error.name");
        });

        // for Field
        try {
            AppValidator.validate((v) -> {
                boolean anyCheck = false;
                v.checkField(anyCheck, "name", "error.name");
                v.checkField(anyCheck, "day", "error.day");
                v.checkField(true, "description", "error.description");
            });
            fail();
        } catch (ValidationException e) {
            List<Warn> warns = e.list();
            assertEquals(2, warns.size());
            assertEquals("name", warns.get(0).field());
            assertEquals("error.name", warns.get(0).message());
            assertEquals("day", warns.get(1).field());
            assertEquals("error.day", warns.get(1).message());
        }

        // for Global
        try {
            AppValidator.validate((v) -> {
                boolean anyCheck = false;
                v.check(anyCheck, "error.global");
            });
            fail();
        } catch (ValidationException e) {
            List<Warn> warns = e.list();
            assertEquals(1, warns.size());
            assertNull(warns.get(0).field());
            assertEquals("error.global", warns.get(0).message());
        }
    }

    @Test
    public void checkSequence() {
        AppValidator v = new AppValidator();
        boolean anyCheck = false;
        v.checkField(anyCheck, "name", "error.name");
        try {
            v.verify();
            fail();
        } catch (ValidationException e) {
            assertEquals("error.name", e.getMessage());
        }
    }

}
