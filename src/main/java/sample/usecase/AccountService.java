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

	/** ログイン情報を取得します。 */
	@Transactional(DefaultRepository.beanNameTx)
	@Cacheable("AccountService.getLoginByLoginId")
	public Optional<Login> getLoginByLoginId(String loginId) {
		return Login.getByLoginId(rep(), loginId);
	}
	
	/** 口座情報を取得します。 */
	@Transactional(DefaultRepository.beanNameTx)
	@Cacheable("AccountService.getAccount")
	public Optional<Account> getAccount(String id) {
		return Account.getValid(rep(), id);
	}
	
}
