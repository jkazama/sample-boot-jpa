package sample.model.account;

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
    public void 金融機関口座を取得する() {
        tx(() -> {
            FiAccount normal = FiAccount.load(rep, "normal", "sample", "JPY");
            assertEquals("normal", normal.getAccountId());
            assertEquals("sample", normal.getCategory());
            assertEquals("JPY", normal.getCurrency());
            assertEquals("sample-JPY", normal.getFiCode());
            assertEquals("FInormal", normal.getFiAccountId());
            try {
                FiAccount.load(rep, "normal", "sample", "USD");
                fail();
            } catch (ValidationException e) {
                assertEquals(ErrorKeys.EntityNotFound, e.getMessage());
            }
        });
    }

    @Test
    public void Hibernate5_1で追加されたアドホックなJoin検証() {
        tx(() -> {
            fixtures.fiAcc("sample", "join", "JPY").save(rep);
            fixtures.acc("sample").save(rep);

            List<FiAccountJoin> list = rep.tmpl()
                    .find("from FiAccount fa left join Account a on fa.accountId = a.id where fa.accountId = ?1",
                            "sample")
                    .stream().map(FiAccountTest::mapJoin).collect(Collectors.toList());

            assertFalse(list.isEmpty());
            FiAccountJoin m = list.get(0);
            assertEquals("sample", m.accountId);
            assertEquals("sample", m.name);
            assertEquals("join-JPY", m.fiCode);
        });
    }

    private static FiAccountJoin mapJoin(Object v) {
        Object[] values = (Object[]) v;
        FiAccount fa = (FiAccount) values[0];
        Account a = (Account) values[1];
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
