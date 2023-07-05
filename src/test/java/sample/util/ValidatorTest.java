package sample.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;

import org.junit.jupiter.api.Test;

import sample.context.ValidationException;
import sample.context.ValidationException.Warn;

public class ValidatorTest {

    @Test
    public void ラムダ式ベースの検証() {
        AppValidator.validate((v) -> {
            boolean anyCheck = true;
            v.checkField(anyCheck, "name", "error.name");
        });

        // フィールドレベルのチェック
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
            assertEquals("name", warns.get(0).getField());
            assertEquals("error.name", warns.get(0).getMessage());
            assertEquals("day", warns.get(1).getField());
            assertEquals("error.day", warns.get(1).getMessage());
        }

        // グローバルチェック
        try {
            AppValidator.validate((v) -> {
                boolean anyCheck = false;
                v.check(anyCheck, "error.global");
            });
            fail();
        } catch (ValidationException e) {
            List<Warn> warns = e.list();
            assertEquals(1, warns.size());
            assertNull(warns.get(0).getField());
            assertEquals("error.global", warns.get(0).getMessage());
        }
    }

    @Test
    public void 手続きベースの検証() {
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
