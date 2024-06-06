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
import su.opencode.digsigcik.synapi.model.AcmeDomain;
import su.opencode.digsigcik.synapi.repository.AccountRepository;
import su.opencode.digsigcik.synapi.repository.DomainRepository;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@AllArgsConstructor
public class DomainController {
    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private DomainRepository domainRepository;

    @GetMapping("/accounts/{accountId}/domains")
    public ResponseEntity<List<AcmeDomain>> getAllDomainsByAccountId(@PathVariable(value = "accountId") Long accountId) {
        if (!accountRepository.existsById(accountId)) {
            throw new ResourceNotFoundException("Not found AcmeAccount with id = " + accountId);
        }
        List<AcmeDomain> domains = domainRepository.findByAccountId(accountId);
        return new ResponseEntity<>(domains, HttpStatus.OK);
    }

    @GetMapping("/domains/{id}")
    public ResponseEntity<AcmeDomain> getDomainBylId(@PathVariable(value = "id") Long id) {
        AcmeDomain domain = domainRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Not found AcmeDomain with id = " + id));

        return new ResponseEntity<>(domain, HttpStatus.OK);
    }

    @PostMapping("/accounts/{accountId}/domains")
    public ResponseEntity<AcmeDomain> createDomain(@PathVariable(value = "accountId") Long accountId,
                                                 @RequestBody AcmeDomain acmeDomain) {
        AcmeDomain domain = accountRepository.findById(accountId).map(account -> {
            AcmeDomain _acmeDomain = new AcmeDomain(account, acmeDomain.getDomainUrl());
            if(acmeDomain.getDomainPrivateKey()!=null)_acmeDomain.setDomainPrivateKey(acmeDomain.getDomainPrivateKey());
            if(acmeDomain.getDeleted()!=null)_acmeDomain.setDeleted(acmeDomain.getDeleted());

            return domainRepository.save(_acmeDomain);
        }).orElseThrow(() -> new ResourceNotFoundException("Not found AcmeAccount with id = " + accountId));

        return new ResponseEntity<>(domain, HttpStatus.CREATED);
    }
    @PutMapping("/domains/{id}")
    public ResponseEntity<AcmeDomain> updateDomain(@PathVariable("id") long id, @RequestBody AcmeDomain acmeDomain) {
        AcmeDomain _acmeDomain = domainRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Not found AcmeDomain with id = " + id));

        AcmeAccount acmeAccount = acmeDomain.getAccount();
        if(acmeAccount != null) {
            Long accountId = acmeAccount.getId();
            AcmeAccount _acmeAccount = accountRepository.findById(accountId)
                    .orElseThrow(() -> new ResourceNotFoundException("Not found AcmeAccount with id = " + id));
            _acmeDomain.setAccount(_acmeAccount);
        }
        if(acmeDomain.getDomainUrl()!=null)_acmeDomain.setDomainUrl(acmeDomain.getDomainUrl());
        if(acmeDomain.getDomainPrivateKey()!=null)_acmeDomain.setDomainPrivateKey(acmeDomain.getDomainPrivateKey());
        if(acmeDomain.getDeleted()!=null)_acmeDomain.setDeleted(acmeDomain.getDeleted());

        return new ResponseEntity<>(domainRepository.save(_acmeDomain), HttpStatus.OK);
    }

    @DeleteMapping("/domains/{id}")
    public ResponseEntity<HttpStatus> deleteDomain(@PathVariable("id") long id) {
        domainRepository.deleteById(id);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/accounts/{accountId}/domains")
    public ResponseEntity<List<AcmeDomain>> deleteAllDomainsOfAccount(@PathVariable(value = "accountId") Long accountId) {
        if (!accountRepository.existsById(accountId)) {
            throw new ResourceNotFoundException("Not found Account with id = " + accountId);
        }

        domainRepository.deleteByAccountId(accountId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
