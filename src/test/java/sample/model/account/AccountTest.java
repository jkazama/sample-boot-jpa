package sample.model.account;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

import sample.EntityTestSupport;
import sample.context.ErrorKeys;
import sample.context.ValidationException;
import sample.model.account.Account.ChgAccount;
import sample.model.account.Account.RegAccount;
import sample.model.account.type.AccountStatusType;
import sample.model.master.Login;

public class AccountTest extends EntityTestSupport {

    @Override
    protected void setupPreset() {
        targetEntities(Account.class, Login.class);
    }

    @Override
    protected void before() {
        tx(() -> {
            fixtures.acc("normal").save(rep);
        });
    }

    @Test
    public void 口座情報を登録する() {
        tx(() -> {
            // 通常登録
            assertFalse(Account.get(rep, "new").isPresent());
            Account.register(rep, encoder, new RegAccount("new", "name", "new@example.com", "password"));
            Account created = Account.load(rep, "new");
            assertEquals("name", created.getName());
            assertEquals("new@example.com", created.getMail());
            Login login = Login.load(rep, "new");
            assertTrue(encoder.matches("password", login.getPassword()));
            // 同一ID重複
            try {
                Account.register(rep, encoder, new RegAccount("normal", "name", "new@example.com", "password"));
                fail();
            } catch (ValidationException e) {
                assertEquals(ErrorKeys.DuplicateId, e.getMessage());
            }
        });
    }

    @Test
    public void 口座情報を変更する() {
        tx(() -> {
            Account changed = Account.load(rep, "normal")
                    .change(rep, new ChgAccount("changed", "changed@example.com"));
            assertEquals("changed", changed.getName());
            assertEquals("changed@example.com", changed.getMail());
        });
    }

    @Test
    public void 有効口座を取得する() {
        tx(() -> {
            // 通常時取得
            Account valid = Account.loadValid(rep, "normal");
            assertEquals("normal", valid.getId());
            assertEquals(AccountStatusType.NORMAL, valid.getStatusType());

            // 退会時取得
            Account withdrawal = fixtures.acc("withdrawal");
            withdrawal.setStatusType(AccountStatusType.WITHDRAWAL);
            withdrawal.save(rep);
            try {
                Account.loadValid(rep, "withdrawal");
                fail();
            } catch (ValidationException e) {
                assertEquals("error.Account.loadValid", e.getMessage());
            }
        });
    }
}
