package sample.usecase;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;

import sample.context.orm.*;
import sample.model.account.*;

/**
 * 口座ドメインに対する顧客ユースケース処理。
 */
@Service
public class AccountService {
    private final DefaultRepository rep;
    private final PlatformTransactionManager txm;

    public AccountService(
            DefaultRepository rep,
            @Qualifier(DefaultRepository.BeanNameTx) PlatformTransactionManager txm) {
        this.rep = rep;
        this.txm = txm;
    }

    /** ログイン情報を取得します。 */
    @Cacheable("AccountService.getLoginByLoginId")
    public Optional<Login> getLoginByLoginId(String loginId) {
        return TxTemplate.of(txm).readOnly().tx(
                () -> Login.getByLoginId(rep, loginId));
    }

    /** 有効な口座情報を取得します。 */
    @Cacheable("AccountService.getAccount")
    public Optional<Account> getAccount(String id) {
        return TxTemplate.of(txm).readOnly().tx(
                () -> Account.getValid(rep, id));
    }

}
