package sample.usecase;

import java.util.*;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sample.context.orm.DefaultRepository;
import sample.model.master.*;
import sample.model.master.Holiday.RegHoliday;

/**
 * The use case processing for the master domain in the organization.
 */
@Service
public class MasterAdminService extends ServiceSupport {

    @Transactional(DefaultRepository.BeanNameTx)
    @Cacheable("MasterAdminService.getStaff")
    public Optional<Staff> getStaff(String id) {
        return Staff.get(rep(), id);
    }

    @Transactional(DefaultRepository.BeanNameTx)
    @Cacheable("MasterAdminService.findStaffAuthority")
    public List<StaffAuthority> findStaffAuthority(String staffId) {
        return StaffAuthority.find(rep(), staffId);
    }

    public void registerHoliday(final RegHoliday p) {
        audit().audit("Register holiday information.", () -> tx(() -> Holiday.register(rep(), p)));
    }

}
