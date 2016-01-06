package sample.model.master;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

import sample.*;
import sample.ValidationException.ErrorKeys;
import sample.model.master.Staff.*;

public class StaffTest extends EntityTestSupport {

    @Override
    protected void setupPreset() {
        targetEntities(Staff.class);
    }

    @Override
    protected void before() {
        tx(() -> fixtures.staff("sample").save(rep));
    }

    @Test
    public void 社員情報を登録する() {
        tx(() -> {
            // 正常登録
            Staff staff = Staff.register(rep, encoder, new RegStaff("new", "newName", "password"));
            assertThat(staff, allOf(
                    hasProperty("id", is("new")),
                    hasProperty("name", is("newName"))));
            assertTrue(encoder.matches("password", staff.getPassword()));

            // 重複ID
            try {
                Staff.register(rep, encoder, new RegStaff("sample", "newName", "password"));
                fail();
            } catch (ValidationException e) {
                assertThat(e.getMessage(), is(ErrorKeys.DuplicateId));
            }
        });
    }

    @Test
    public void 社員パスワードを変更する() {
        tx(() -> {
            Staff changed = Staff.load(rep, "sample").change(rep, encoder, new ChgPassword("changed"));
            assertTrue(encoder.matches("changed", changed.getPassword()));
        });
    }

    @Test
    public void 社員情報を変更する() {
        tx(() -> {
            assertThat(
                    Staff.load(rep, "sample").change(rep, new ChgStaff("changed")).getName(), is("changed"));
        });
    }

    @Test
    public void 社員を検索する() {
        tx(() -> {
            assertFalse(Staff.find(rep, new FindStaff("amp")).isEmpty());
            assertTrue(Staff.find(rep, new FindStaff("amq")).isEmpty());
        });
    }

}
