package sample.model.master;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

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
            assertEquals("new", staff.getId());
            assertEquals("newName", staff.getName());
            assertTrue(encoder.matches("password", staff.getPassword()));

            // 重複ID
            try {
                Staff.register(rep, encoder, new RegStaff("sample", "newName", "password"));
                fail();
            } catch (ValidationException e) {
                assertEquals(ErrorKeys.DuplicateId, e.getMessage());
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
            assertEquals(
                    "changed", Staff.load(rep, "sample").change(rep, new ChgStaff("changed")).getName());
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
