package sample.model.master;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

import sample.*;
import sample.ValidationException.ErrorKeys;

public class SelfFiAccountTest extends EntityTestSupport {

	@Override
	protected void setupPreset() {
		targetEntities(SelfFiAccount.class);
	}

	@Override
	protected void before() {
		tx(() -> fixtures.selfFiAcc("sample", "JPY").save(rep));
	}
	
	@Test
	public void 自社金融機関口座を取得する() {
		tx(() -> {
			assertThat(SelfFiAccount.load(rep, "sample", "JPY"), allOf(
				hasProperty("category", is("sample")),
				hasProperty("currency", is("JPY")),
				hasProperty("fiCode", is("sample-JPY")),
				hasProperty("fiAccountId", is("xxxxxx"))));
			try {
				SelfFiAccount.load(rep, "sample", "USD");
				fail();
			} catch (ValidationException e) {
				assertThat(e.getMessage(), is(ErrorKeys.EntityNotFound));
			}
		});
	}

	
}
