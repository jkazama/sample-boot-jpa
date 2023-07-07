package sample.model.account;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import lombok.Builder;
import sample.context.ErrorKeys;
import sample.context.ValidationException;
import sample.model.DataFixtures;
import sample.model.DomainTester;
import sample.model.DomainTester.DomainTesterBuilder;

public class FiAccountTest {

    private DomainTester tester;

    @BeforeEach
    public void before() {
        tester = DomainTesterBuilder.from(FiAccount.class, Account.class).build();
        tester.txInitializeData(rep -> {
            rep.save(DataFixtures.fiAcc("normal", "sample", "JPY"));
        });
    }

    @AfterEach
    public void after() {
        tester.close();
    }

    @Test
    public void load() {
        tester.tx(rep -> {
            var normal = FiAccount.load(rep, "normal", "sample", "JPY");
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
    public void checkAdHocJoin() {
        tester.tx(rep -> {
            rep.save(DataFixtures.fiAcc("sample", "join", "JPY"));
            rep.save(DataFixtures.account("sample"));

            var jpql = """
                    SELECT fa,a FROM FiAccount fa LEFT JOIN Account a ON fa.accountId = a.id WHERE fa.accountId = ?1
                    """;
            List<FiAccountJoin> list = rep.tmpl().find(jpql, "sample").stream()
                    .map(FiAccountTest::mapJoin)
                    .toList();
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
        return FiAccountJoin.builder()
                .accountId(fa.getAccountId())
                .name(a.getName())
                .fiCode(fa.getFiCode())
                .fiAcountId(fa.getFiAccountId())
                .build();
    }

    @Builder
    private static record FiAccountJoin(
            String accountId,
            String name,
            String fiCode,
            String fiAcountId) {
    }
}
