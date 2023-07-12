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

import sample.context.ValidationException;
import sample.context.actor.type.ActorRoleType;
import sample.model.DataFixtures;
import sample.model.DomainErrorKeys;
import sample.model.DomainTester;
import sample.model.DomainTester.DomainTesterBuilder;
import sample.model.master.Login;
import sample.model.master.Login.LoginId;

public class LoginTest {
    private DomainTester tester;
    private final PasswordEncoder encoder = new BCryptPasswordEncoder();

    @BeforeEach
    public void before() {
        tester = DomainTesterBuilder.from(Login.class).build();
        tester.txInitializeData(rep -> {
            rep.save(DataFixtures.login(encoder, "test", ActorRoleType.USER));
        });
    }

    @AfterEach
    public void after() {
        tester.close();
    }

    @Test
    public void changeLoginId() {
        tester.tx(rep -> {
            // Normal case
            rep.save(DataFixtures.login(encoder, "any", ActorRoleType.USER));

            var changed = Login.load(rep, LoginId.of("any", ActorRoleType.USER))
                    .changeLoginId(rep, "testAny");
            assertEquals("any", changed.getActorId());
            assertEquals("testAny", changed.getLoginId());

            // Change case for myself
            changed = Login.load(rep, LoginId.of("any", ActorRoleType.USER))
                    .changeLoginId(rep, "testAny");
            assertEquals("any", changed.getActorId());
            assertEquals("testAny", changed.getLoginId());

            // Duplicate ID case
            try {
                Login.load(rep, LoginId.of("any", ActorRoleType.USER))
                        .changeLoginId(rep, "test");
                fail();
            } catch (ValidationException e) {
                assertEquals(DomainErrorKeys.DuplicateId, e.getMessage());
            }
        });
    }

    @Test
    public void changePassword() {
        tester.tx(rep -> {
            var login = Login.load(rep, LoginId.of("test", ActorRoleType.USER))
                    .changePassword(rep, encoder, "changed");
            assertTrue(encoder.matches("changed", login.getPassword()));
        });
    }

    @Test
    public void getByLoginId() {
        tester.tx(rep -> {
            var m = Login.load(rep, LoginId.of("test", ActorRoleType.USER));
            m.setLoginId("changed");
            rep.update(m);
            assertTrue(Login.getByLoginId(rep, ActorRoleType.USER, "changed").isPresent());
            assertFalse(Login.getByLoginId(rep, ActorRoleType.USER, "test").isPresent());
        });
    }

}
