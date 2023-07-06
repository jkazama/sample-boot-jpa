package sample.usecase.admin;

import java.util.List;
import java.util.Optional;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;

import lombok.RequiredArgsConstructor;
import sample.context.audit.AuditHandler;
import sample.context.orm.OrmRepository;
import sample.context.orm.TxTemplate;
import sample.model.master.Holiday;
import sample.model.master.Holiday.FindHoliday;
import sample.model.master.Holiday.RegHoliday;
import sample.model.master.Staff;
import sample.model.master.StaffAuthority;

/**
 * Internal use case processing for the service master domain.
 */
@Service
@RequiredArgsConstructor
public class MasterAdminService {
    private static final String CacheKeyPrefix = "MasterAdminService.";
    private final OrmRepository rep;
    private final PlatformTransactionManager txm;
    private final AuditHandler audit;

    /** Returns staff information. */
    @Cacheable(CacheKeyPrefix + "getStaff")
    public Optional<Staff> getStaff(String staffId) {
        return TxTemplate.of(txm).readOnly().tx(() -> {
            return Staff.get(rep, staffId);
        });
    }

    /** Returns staff authorities. */
    @Cacheable(CacheKeyPrefix + "findStaffAuthority")
    public List<StaffAuthority> findStaffAuthority(String staffId) {
        return TxTemplate.of(txm).readOnly().tx(() -> {
            return StaffAuthority.find(rep, staffId);
        });
    }

    /** Find holiday information. */
    public List<Holiday> findHoliday(final FindHoliday param) {
        return TxTemplate.of(txm).readOnly().tx(() -> {
            return Holiday.find(rep, param);
        });
    }

    /** Register holiday information. */
    public void registerHoliday(final RegHoliday param) {
        audit.audit("master", "registerHoliday", List.of(param.year()), () -> {
            TxTemplate.of(txm).tx(() -> Holiday.register(rep, param));
        });
    }

}
