package su.opencode.digsigcik.synapi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import su.opencode.digsigcik.synapi.model.Account;
import su.opencode.digsigcik.synapi.repository.AccountRepository;

import java.util.List;

@Service
@Primary
public class AccountServiceImpl implements AccountService{
    private final AccountRepository repository;

    @Autowired
    public AccountServiceImpl(AccountRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Account> findAllAccounts() {
        return this.repository.findAll();
    }

    @Override
    public Account saveAccount(Account account) {
        return this.repository.save(account);
    }

    @Override
    public Account findByAccountId(Long id) {
        return this.repository.findAccountById(id);
    }

    @Override
    public Account findByExternalKey(String externalKey) {
        return this.repository.findAccountByExternalKey(externalKey);
    }

    @Override
    public Account updateAccount(Account account) {
        return this.repository.save(account);
    }

    @Override
    public void deleteAccount(Long id) {
        this.repository.deleteById(id);
    }
}
