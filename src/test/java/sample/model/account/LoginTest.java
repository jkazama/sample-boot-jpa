package sample.model.account;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

import sample.*;
import sample.ValidationException.ErrorKeys;
import sample.model.account.Login.*;

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
    public void change() {
        tx(() -> {
            fixtures.login("any").save(rep);
            assertThat(Login.load(rep, "any").change(rep, new ChgLoginId("testAny")), allOf(
                    hasProperty("id", is("any")),
                    hasProperty("loginId", is("testAny"))));

            assertThat(Login.load(rep, "any").change(rep, new ChgLoginId("testAny")), allOf(
                    hasProperty("id", is("any")),
                    hasProperty("loginId", is("testAny"))));

            try {
                Login.load(rep, "any").change(rep, new ChgLoginId("test"));
                fail();
            } catch (ValidationException e) {
                assertThat(e.getMessage(), is(ErrorKeys.DuplicateId));
            }
        });
    }

    @Test
    public void changePassword() {
        tx(() -> {
            Login login = Login.load(rep, "test").change(rep, encoder, new ChgPassword("changed"));
            assertTrue(encoder.matches("changed", login.getPassword()));
        });
    }

    @Test
    public void load() {
        tx(() -> {
            Login m = Login.load(rep, "test");
            m.setLoginId("changed");
            m.update(rep);
            assertTrue(Login.getByLoginId(rep, "changed").isPresent());
            assertFalse(Login.getByLoginId(rep, "test").isPresent());
        });
    }

}
