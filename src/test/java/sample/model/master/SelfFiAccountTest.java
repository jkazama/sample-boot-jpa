package sample.model.master;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sample.context.ErrorKeys;
import sample.context.ValidationException;
import sample.model.DataFixtures;
import sample.model.DomainTester;
import sample.model.DomainTester.DomainTesterBuilder;

public class SelfFiAccountTest {
    private DomainTester tester;

    @BeforeEach
    public void before() {
        tester = DomainTesterBuilder.from(SelfFiAccount.class).build();
        tester.txInitializeData(rep -> {
            rep.save(DataFixtures.selfFiAcc("sample", "JPY"));
        });
    }

    @AfterEach
    public void after() {
        tester.close();
    }

    @Test
    public void load() {
        tester.tx(rep -> {
            var selfFi = SelfFiAccount.load(rep, "sample", "JPY");
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
