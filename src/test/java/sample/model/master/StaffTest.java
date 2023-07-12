package sample.model.master;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import sample.context.ValidationException;
import sample.context.actor.type.ActorRoleType;
import sample.model.DataFixtures;
import sample.model.DomainErrorKeys;
import sample.model.DomainTester;
import sample.model.DomainTester.DomainTesterBuilder;
import sample.model.master.Login.LoginId;
import sample.model.master.Staff.ChgStaff;
import sample.model.master.Staff.FindStaff;
import sample.model.master.Staff.RegStaff;

public class StaffTest {
    private DomainTester tester;
    private final PasswordEncoder encoder = new BCryptPasswordEncoder();

    @BeforeEach
    public void before() {
        tester = DomainTesterBuilder.from(Staff.class, Login.class).build();
        tester.txInitializeData(rep -> {
            rep.save(DataFixtures.staff("sample"));
        });
    }

    @AfterEach
    public void after() {
        tester.close();
    }

    @Test
    public void regiser() {
        tester.tx(rep -> {
            // normal case
            var staff = Staff.register(rep, encoder, RegStaff.builder()
                    .staffId("new")
                    .name("newName")
                    .plainPassword("password")
                    .build());
            assertEquals("new", staff.getStaffId());
            assertEquals("newName", staff.getName());
            assertTrue(encoder.matches("password", Login.load(rep,
                    LoginId.of(staff.getStaffId(), ActorRoleType.INTERNAL)).getPassword()));

            // duplicated ID
            try {
                Staff.register(rep, encoder, RegStaff.builder()
                        .staffId("sample")
                        .name("newName")
                        .plainPassword("password")
                        .build());
                fail();
            } catch (ValidationException e) {
                assertEquals(DomainErrorKeys.DuplicateId, e.getMessage());
            }
        });
    }

    @Test
    public void change() {
        tester.tx(rep -> {
            assertEquals("changed", Staff.load(rep, "sample")
                    .change(rep, ChgStaff.builder().name("changed").build()).getName());
        });
    }

    @Test
    public void find() {
        tester.tx(rep -> {
            assertFalse(Staff.find(rep, new FindStaff("amp")).isEmpty());
            assertTrue(Staff.find(rep, new FindStaff("amq")).isEmpty());
        });
    }

}
