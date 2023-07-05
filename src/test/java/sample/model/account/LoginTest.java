package sample.model.account;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

import sample.EntityTestSupport;
import sample.context.ErrorKeys;
import sample.context.ValidationException;
import sample.model.account.Login.ChgLoginId;
import sample.model.account.Login.ChgPassword;

public class LoginTest extends EntityTestSupport {
    @Override
    protected void setupPreset() {
        targetEntities(Login.class);
    }

    @Override
    protected void before() {
        tx(() -> fixtures.login("test").save(rep));
    }

    @Test
    public void ログインIDを変更する() {
        tx(() -> {
            // 正常系
            fixtures.login("any").save(rep);
            Login changed = Login.load(rep, "any").change(rep, new ChgLoginId("testAny"));
            assertEquals("any", changed.getId());
            assertEquals("testAny", changed.getLoginId());

            // 自身に対する同名変更
            changed = Login.load(rep, "any").change(rep, new ChgLoginId("testAny"));
            assertEquals("any", changed.getId());
            assertEquals("testAny", changed.getLoginId());

            // 重複ID
            try {
                Login.load(rep, "any").change(rep, new ChgLoginId("test"));
                fail();
            } catch (ValidationException e) {
                assertEquals(ErrorKeys.DuplicateId, e.getMessage());
            }
        });
    }

    @Test
    public void パスワードを変更する() {
        tx(() -> {
            Login login = Login.load(rep, "test").change(rep, encoder, new ChgPassword("changed"));
            assertTrue(encoder.matches("changed", login.getPassword()));
        });
    }

    @Test
    public void ログイン情報を取得する() {
        tx(() -> {
            Login m = Login.load(rep, "test");
            m.setLoginId("changed");
            m.update(rep);
            assertTrue(Login.getByLoginId(rep, "changed").isPresent());
            assertFalse(Login.getByLoginId(rep, "test").isPresent());
        });
    }

}
