package sample.usecase;

import java.util.Optional;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sample.context.orm.DefaultRepository;
import sample.model.account.*;

/**
 * The customer use case processing for the account domain.
 */
@Service
public class AccountService extends ServiceSupport {

    @Transactional(DefaultRepository.BeanNameTx)
    @Cacheable("AccountService.getLoginByLoginId")
    public Optional<Login> getLoginByLoginId(String loginId) {
        return Login.getByLoginId(rep(), loginId);
    }

    @Transactional(DefaultRepository.BeanNameTx)
    @Cacheable("AccountService.getAccount")
    public Optional<Account> getAccount(String id) {
        return Account.getValid(rep(), id);
    }

}
