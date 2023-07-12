package sample.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;

import lombok.RequiredArgsConstructor;
import sample.context.orm.OrmRepository;
import sample.context.orm.TxTemplate;
import sample.model.account.Account;

/**
 * Customer use case processing for the account domain.
 */
@Service
@RequiredArgsConstructor
public class AccountService {
    private final OrmRepository rep;
    private final PlatformTransactionManager txm;

    /** Returns valid account information. */
    public Account loadAccount() {
        var accountId = rep.dh().actor().id();
        return TxTemplate.of(txm).readOnly().tx(() -> {
            return Account.loadValid(rep, accountId);
        });
    }

}
