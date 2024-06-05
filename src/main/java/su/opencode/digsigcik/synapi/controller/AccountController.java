package su.opencode.digsigcik.synapi.controller;

import lombok.AllArgsConstructor;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import su.opencode.digsigcik.synapi.model.AcmeAccount;
import su.opencode.digsigcik.synapi.repository.AccountRepository;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@AllArgsConstructor
public class AccountController {
    @Autowired
    AccountRepository accountRepository;

    @GetMapping("/accounts")
    public ResponseEntity<List<AcmeAccount>> getAllAccounts() {
        List<AcmeAccount> accounts = new ArrayList<>(accountRepository.findAll());
        if (accounts.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(accounts, HttpStatus.OK);
    }

    @GetMapping("/accounts/{id}")
    public ResponseEntity<AcmeAccount> findByAccountId(@PathVariable Long id) {
        AcmeAccount account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Not found AcmeAccount with id = " + id));
        return new ResponseEntity<>(account, HttpStatus.OK);
    }

    @PostMapping("/accounts")
    public ResponseEntity<AcmeAccount> saveAccount(@RequestBody AcmeAccount acmeAccount) {
        AcmeAccount _acmeAccount = new AcmeAccount(acmeAccount.getName(), acmeAccount.getExternalKey());
        if(acmeAccount.getAccountUrl()!=null)_acmeAccount.setAccountUrl(acmeAccount.getAccountUrl());
        if(acmeAccount.getAccountPrivateKey()!=null)_acmeAccount.setAccountPrivateKey(acmeAccount.getAccountPrivateKey());
        if(acmeAccount.getDeleted()!=null)acmeAccount.setDeleted(acmeAccount.getDeleted());
        return new ResponseEntity<>(accountRepository.save(_acmeAccount), HttpStatus.CREATED);


    }

    @PutMapping("/accounts/{id}")
    public ResponseEntity<AcmeAccount> updateAccount(@PathVariable Long id, @RequestBody AcmeAccount acmeAccount) {
        AcmeAccount _acmeAccount = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Not found AcmeAccount with id = " + id));

        if(acmeAccount.getName()!=null)_acmeAccount.setName(acmeAccount.getName());;
        if(acmeAccount.getExternalKey()!=null)_acmeAccount.setExternalKey(acmeAccount.getExternalKey());
        if(acmeAccount.getAccountUrl()!=null)_acmeAccount.setAccountUrl(acmeAccount.getAccountUrl());
        if(acmeAccount.getAccountPrivateKey()!=null)_acmeAccount.setAccountPrivateKey(acmeAccount.getAccountPrivateKey());
        if(acmeAccount.getDeleted()!=null)acmeAccount.setDeleted(acmeAccount.getDeleted());

        return new ResponseEntity<>(accountRepository.save(_acmeAccount), HttpStatus.OK);



    }

    @DeleteMapping("/accounts/{id}")
    public ResponseEntity<HttpStatus> deleteAccount(@PathVariable Long id) {
        accountRepository.deleteById(id);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);}
}
