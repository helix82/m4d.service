package su.opencode.digsigcik.synapi.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import su.opencode.digsigcik.synapi.model.Account;
import su.opencode.digsigcik.synapi.service.AccountService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/account")
@AllArgsConstructor
public class AccountController {
    private final AccountService service;

    @GetMapping()
    public List<Account> findAllAccounts() {
        return service.findAllAccounts();
    }

    @PostMapping("save")
    public Account saveCredential(@RequestBody Account account) {
        return service.saveAccount(account);
    }

    @GetMapping("/{id}")
    public Account findByCredentialId(@PathVariable Long id) {
        return service.findByAccountId(id);
    }

    @PutMapping("update")
    public Account updateCredential(@RequestBody Account account) {
        return service.updateAccount(account);
    }

    @DeleteMapping("delete/{id}")
    public void deleteCredential(@PathVariable Long id) {
        service.deleteAccount(id);
    }
}
