package sample.usecase;

import java.util.*;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sample.context.orm.DefaultRepository;
import sample.model.master.*;
import sample.model.master.Holiday.RegHoliday;

/**
 * サービスマスタドメインに対する社内ユースケース処理。
 */
@Service
public class MasterAdminService extends ServiceSupport {

    /** 社員を取得します。 */
    @Transactional(DefaultRepository.beanNameTx)
    @Cacheable("MasterAdminService.getStaff")
    public Optional<Staff> getStaff(String id) {
        return Staff.get(rep(), id);
    }

    /** 社員権限を取得します。 */
    @Transactional(DefaultRepository.beanNameTx)
    @Cacheable("MasterAdminService.findStaffAuthority")
    public List<StaffAuthority> findStaffAuthority(String staffId) {
        return StaffAuthority.find(rep(), staffId);
    }

    public void registerHoliday(final RegHoliday p) {
        audit().audit("休日情報を登録する", () -> tx(() -> Holiday.register(rep(), p)));
    }

}
