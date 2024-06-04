package su.opencode.digsigcik.synapi.service;

import su.opencode.digsigcik.synapi.model.Account;

import java.util.List;

public interface AccountService {
    List<Account> findAllAccounts();
    Account saveAccount(Account account);
    Account findByAccountId(Long id);
    Account findByExternalKey(String externalKey);
    Account updateAccount(Account account);
    void deleteAccount(Long id);
}
