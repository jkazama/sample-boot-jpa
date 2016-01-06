package sample.util;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

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
            assertThat(warns.size(), is(2));
            assertThat(warns.get(0).getField(), is("name"));
            assertThat(warns.get(0).getMessage(), is("error.name"));
            assertThat(warns.get(1).getField(), is("day"));
            assertThat(warns.get(1).getMessage(), is("error.day"));
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
            assertThat(warns.size(), is(1));
            assertNull(warns.get(0).getField());
            assertThat(warns.get(0).getMessage(), is("error.global"));
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
            assertThat(e.getMessage(), is("error.name"));
        }
    }

}
