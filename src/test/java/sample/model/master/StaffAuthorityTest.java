package sample.model.master;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import sample.EntityTestSupport;

public class StaffAuthorityTest extends EntityTestSupport {

    @Override
    protected void setupPreset() {
        targetEntities(StaffAuthority.class);
    }

    @Override
    protected void before() {
        tx(() -> {
            fixtures.staffAuth("staffA", "ID000001", "ID000002", "ID000003").forEach((auth) -> auth.save(rep));
            fixtures.staffAuth("staffB", "ID000001", "ID000002").forEach((auth) -> auth.save(rep));
        });
    }

    @Test
    public void 権限一覧を検索する() {
        tx(() -> {
            assertEquals(3, StaffAuthority.find(rep, "staffA").size());
            assertEquals(2, StaffAuthority.find(rep, "staffB").size());
        });
    }

}
