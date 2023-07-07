package sample.model.account;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import sample.context.ErrorKeys;
import sample.context.ValidationException;
import sample.context.actor.type.ActorRoleType;
import sample.model.DataFixtures;
import sample.model.DomainErrorKeys;
import sample.model.DomainTester;
import sample.model.DomainTester.DomainTesterBuilder;
import sample.model.account.Account.ChgAccount;
import sample.model.account.Account.RegAccount;
import sample.model.account.type.AccountStatusType;
import sample.model.master.Login;
import sample.model.master.Login.LoginId;

public class AccountTest {
    private DomainTester tester;
    private final PasswordEncoder encoder = new BCryptPasswordEncoder();

    @BeforeEach
    public void before() {
        tester = DomainTesterBuilder.from(Account.class, Login.class).build();
        tester.txInitializeData(rep -> {
            rep.save(DataFixtures.account("normal"));
        });
    }

    @AfterEach
    public void after() {
        tester.close();
    }

    @Test
    public void register() {
        tester.tx(rep -> {
            // Normal registration
            assertFalse(Account.get(rep, "new").isPresent());
            Account.register(rep, encoder, new RegAccount("new", "name", "new@example.com", "password"));
            Account created = Account.load(rep, "new");
            assertEquals("name", created.getName());
            assertEquals("new@example.com", created.getMailAddress());
            Login login = Login.load(rep, LoginId.of("new", ActorRoleType.USER));
            assertTrue(encoder.matches("password", login.getPassword()));
            // Duplicate same ID
            try {
                Account.register(rep, encoder, new RegAccount("normal", "name", "new@example.com", "password"));
                fail();
            } catch (ValidationException e) {
                assertEquals(DomainErrorKeys.DuplicateId, e.getMessage());
            }
        });
    }

    @Test
    public void change() {
        tester.tx(rep -> {
            Account changed = Account
                    .load(rep, "normal")
                    .change(rep, ChgAccount.builder()
                            .accountId("normal")
                            .name("changed")
                            .mailAddress("changed@example.com")
                            .build());
            assertEquals("changed", changed.getName());
            assertEquals("changed@example.com", changed.getMailAddress());
        });
    }

    @Test
    public void loadValid() {
        tester.tx(rep -> {
            // for valid
            Account valid = Account.loadValid(rep, "normal");
            assertEquals("normal", valid.getAccountId());
            assertEquals(AccountStatusType.NORMAL, valid.getStatusType());

            // for withdrawal
            Account withdrawal = DataFixtures.account("withdrawal");
            withdrawal.setStatusType(AccountStatusType.WITHDRAWAL);
            rep.save(withdrawal);
            try {
                Account.loadValid(rep, "withdrawal");
                fail();
            } catch (ValidationException e) {
                assertEquals(ErrorKeys.EntityNotFound, e.getMessage());
            }
        });
    }
}
