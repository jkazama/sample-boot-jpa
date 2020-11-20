package sample.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

import sample.ValidationException;
import sample.ValidationException.Warn;

public class ValidatorTest {

    @Test
    public void ラムダ式ベースの検証() {
        Validator.validate((v) -> {
            boolean anyCheck = true;
            v.checkField(anyCheck, "name", "error.name");
        });

        // フィールドレベルのチェック
        try {
            Validator.validate((v) -> {
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
            Validator.validate((v) -> {
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
        Validator v = new Validator();
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
