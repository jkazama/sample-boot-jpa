package sample.model.master;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import sample.*;
import sample.context.ValidationException;
import sample.context.ValidationException.ErrorKeys;

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
            SelfFiAccount selfFi = SelfFiAccount.load(rep, "sample", "JPY");
            assertEquals("sample", selfFi.getCategory());
            assertEquals("JPY", selfFi.getCurrency());
            assertEquals("sample-JPY", selfFi.getFiCode());
            assertEquals("xxxxxx", selfFi.getFiAccountId());
            try {
                SelfFiAccount.load(rep, "sample", "USD");
                fail();
            } catch (ValidationException e) {
                assertEquals(ErrorKeys.EntityNotFound, e.getMessage());
            }
        });
    }

}
