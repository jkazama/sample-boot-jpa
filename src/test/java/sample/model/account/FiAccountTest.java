package sample.model.account;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import lombok.Value;
import sample.*;
import sample.ValidationException.ErrorKeys;

public class FiAccountTest extends EntityTestSupport {

    @Override
    protected void setupPreset() {
        targetEntities(FiAccount.class, Account.class);
    }

    @Override
    protected void before() {
        tx(() -> fixtures.fiAcc("normal", "sample", "JPY").save(rep));
    }

    @Test
    public void load() {
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

    @Test
    public void Hibernate5_1_adhocJoinCheck() {
        tx(() -> {
            fixtures.fiAcc("sample", "join", "JPY").save(rep);
            fixtures.acc("sample").save(rep);
            
            List<FiAccountJoin> list = rep.tmpl()
                .find("from FiAccount fa left join Account a on fa.accountId = a.id where fa.accountId = ?1", "sample")
                .stream().map(FiAccountTest::mapJoin).collect(Collectors.toList());
            
            assertFalse(list.isEmpty());
            FiAccountJoin m = list.get(0);
            assertThat(m.accountId, is("sample"));
            assertThat(m.name, is("sample"));
            assertThat(m.fiCode, is("join-JPY"));
        });
    }
    
    private static FiAccountJoin mapJoin(Object v) {
        Object[] values = (Object[])v;
        FiAccount fa = (FiAccount)values[0];
        Account a = (Account)values[1];
        return new FiAccountJoin(fa.getAccountId(), a.getName(), fa.getFiCode(), fa.getFiAccountId());
    }

    @Value
    private static class FiAccountJoin {
        private String accountId;
        private String name;
        private String fiCode;
        private String fiAcountId;
    }
}
