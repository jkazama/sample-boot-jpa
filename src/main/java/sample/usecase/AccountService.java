package sample.usecase;

import java.util.Optional;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sample.context.orm.DefaultRepository;
import sample.model.account.*;

/**
 * 口座ドメインに対する顧客ユースケース処理。
 */
@Service
public class AccountService extends ServiceSupport {

	@Transactional(DefaultRepository.beanNameTx)
	@Cacheable("AccountService.getAccount")
	public Optional<Account> getAccount(String id) {
		return Account.getValid(rep(), id);
	}
	
	@Transactional(DefaultRepository.beanNameTx)
	@Cacheable("AccountService.getLogin")
	public Login loadLogin(String id) {
		return Login.load(rep(), id);
	}
	
}
