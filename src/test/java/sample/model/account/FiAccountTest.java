package sample.model.account;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

import sample.*;
import sample.ValidationException.ErrorKeys;

public class FiAccountTest extends EntityTestSupport {

	@Override
	protected void setupPreset() {
		targetEntities(FiAccount.class);
	}
	
	@Override
	protected void before() {
		tx(() -> fixtures.fiAcc("normal", "sample", "JPY").save(rep));
	}
	
	@Test
	public void 金融機関口座を取得する() {
		tx(() -> {
			assertThat(FiAccount.load(rep, "normal", "sample", "JPY"), allOf(
				hasProperty("accountId", is("normal")),
				hasProperty("category", is("sample")),
				hasProperty("currency", is("JPY")),
				hasProperty("fiCode", is("sample-JPY")),
				hasProperty("fiAccountId", is("FInormal"))));
			try {
				FiAccount.load(rep, "normal", "sample", "USD");
				fail();
			} catch (ValidationException e) {
				assertThat(e.getMessage(), is(ErrorKeys.EntityNotFound));
			}
		});
	}
	
}
