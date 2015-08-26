package sample.model.account;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import sample.*;
import sample.model.account.type.AccountStatusType;

public class AccountTest extends EntityTestSupport {

	@Override
	protected void setupPreset() {
		targetEntities(Account.class, Login.class);
	}
	
	@Test
	public void loadActive() {
		tx(() -> {
			// 通常時取得検証
			fixtures.acc("normal").save(rep);
			assertThat(Account.loadValid(rep, "normal"), allOf(
				hasProperty("id", is("normal")),
				hasProperty("statusType", is(AccountStatusType.NORMAL))));
			
			// 退会時取得検証
			Account withdrawal = fixtures.acc("withdrawal");
			withdrawal.setStatusType(AccountStatusType.WITHDRAWAL);
			withdrawal.save(rep);
			try {
				Account.loadValid(rep, "withdrawal");
			} catch (ValidationException e) {
				assertThat(e.getMessage(), is("error.Account.loadValid"));
			}
		});
	}
}
