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
	public void ログインIDを変更する() {
		tx(() -> {
			// 正常系
			fixtures.login("any").save(rep);
			assertThat(Login.load(rep, "any").change(rep, new ChgLoginId("testAny")), allOf(
				hasProperty("id", is("any")),
				hasProperty("loginId", is("testAny"))));
			
			// 自身に対する同名変更
			assertThat(Login.load(rep, "any").change(rep, new ChgLoginId("testAny")), allOf(
					hasProperty("id", is("any")),
					hasProperty("loginId", is("testAny"))));
			
			// 重複ID
			try {
				Login.load(rep, "any").change(rep, new ChgLoginId("test"));
				fail();
			} catch (ValidationException e) {
				assertThat(e.getMessage(), is(ErrorKeys.DuplicateId));
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
