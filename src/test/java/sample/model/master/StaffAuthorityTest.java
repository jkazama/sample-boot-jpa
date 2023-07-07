package sample.model.master;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sample.model.DataFixtures;
import sample.model.DomainTester;
import sample.model.DomainTester.DomainTesterBuilder;

public class StaffAuthorityTest {
    private DomainTester tester;

    @BeforeEach
    public void before() {
        tester = DomainTesterBuilder.from(StaffAuthority.class).build();
        tester.txInitializeData(rep -> {
            DataFixtures.staffAuth("staffA", "ID000001", "ID000002", "ID000003").forEach((auth) -> rep.save(auth));
            DataFixtures.staffAuth("staffB", "ID000001", "ID000002").forEach((auth) -> rep.save(auth));
        });
    }

    @AfterEach
    public void after() {
        tester.close();
    }

    @Test
    public void find() {
        tester.tx(rep -> {
            assertEquals(3, StaffAuthority.find(rep, "staffA").size());
            assertEquals(2, StaffAuthority.find(rep, "staffB").size());
        });
    }

}
