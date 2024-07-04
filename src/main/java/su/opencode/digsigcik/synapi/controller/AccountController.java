package su.opencode.digsigcik.synapi.controller;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
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
import org.springframework.web.server.ResponseStatusException;
import su.opencode.digsigcik.synapi.model.AcmeAccount;
import su.opencode.digsigcik.synapi.repository.AccountRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1")
@AllArgsConstructor
public class AccountController {
    @Autowired
    AccountRepository accountRepository;

    @Operation(summary = "Получить список ACME-аккаунтов",
            description = "Возвращает список ACME-аккаунтов" )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно получено" ),
            @ApiResponse(code = 404, message = "Not found" )
    })
    @GetMapping("/accounts")
    public ResponseEntity<List<AcmeAccount>> getAllAccounts() {
        List<AcmeAccount> accounts = new ArrayList<>(accountRepository.findAll());
        if (accounts.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(accounts, HttpStatus.OK);
    }

    @Operation(summary = "Получить указанный ACME-аккаунт",
            description = "Возвращает указанный ACME-аккаунт" )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно получено" ),
            @ApiResponse(code = 404, message = "Not found" )
    })
    @GetMapping("/accounts/{id}")
    public ResponseEntity<AcmeAccount> findByAccountId(@PathVariable Long id) {
        AcmeAccount account = accountRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found AcmeAccount with id = " + id));
        return new ResponseEntity<>(account, HttpStatus.OK);
    }

    @Operation(summary = "Создать ACME-аккаунт",
            description = "Создаёт ACME-аккаунт" )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно создан" ),
            @ApiResponse(code = 404, message = "Not found" )
    })
    @PostMapping("/accounts")
    public ResponseEntity<AcmeAccount> saveAccount(@RequestBody AcmeAccount acmeAccount) {
        AcmeAccount _acmeAccount = new AcmeAccount(acmeAccount.getName(), acmeAccount.getExternalKey());
        if(acmeAccount.getEmail()!=null)_acmeAccount.setEmail(acmeAccount.getEmail());;
        if(acmeAccount.getAccountUrl()!=null)_acmeAccount.setAccountUrl(acmeAccount.getAccountUrl());
        if(acmeAccount.getAccountPrivateKey()!=null)_acmeAccount.setAccountPrivateKey(acmeAccount.getAccountPrivateKey());
        if(acmeAccount.getDeleted()!=null)_acmeAccount.setDeleted(acmeAccount.getDeleted());
        return new ResponseEntity<>(accountRepository.save(_acmeAccount), HttpStatus.CREATED);


    }

    @Operation(summary = "Обновить ACME-аккаунт",
            description = "Обновляет ACME-аккаунт" )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно обновлён" ),
            @ApiResponse(code = 404, message = "Not found" )
    })
    @PutMapping("/accounts/{id}")
    public ResponseEntity<AcmeAccount> updateAccount(@PathVariable Long id, @RequestBody AcmeAccount acmeAccount) {
        AcmeAccount _acmeAccount = accountRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found AcmeAccount with id = " + id));

        if(acmeAccount.getName()!=null)_acmeAccount.setName(acmeAccount.getName());;
        if(acmeAccount.getEmail()!=null)_acmeAccount.setEmail(acmeAccount.getEmail());;
        if(acmeAccount.getExternalKey()!=null)_acmeAccount.setExternalKey(acmeAccount.getExternalKey());
        if(acmeAccount.getAccountUrl()!=null)_acmeAccount.setAccountUrl(acmeAccount.getAccountUrl());
        if(acmeAccount.getAccountPrivateKey()!=null)_acmeAccount.setAccountPrivateKey(acmeAccount.getAccountPrivateKey());
        if(acmeAccount.getDeleted()!=null)_acmeAccount.setDeleted(acmeAccount.getDeleted());

        return new ResponseEntity<>(accountRepository.save(_acmeAccount), HttpStatus.OK);
    }

    @Operation(summary = "Удалить ACME-аккаунт",
            description = "Удаляет ACME-аккаунт" )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно удалён" ),
            @ApiResponse(code = 404, message = "Not found" )
    })
    @DeleteMapping("/accounts/{id}")
    public ResponseEntity<HttpStatus> deleteAccount(@PathVariable Long id) {
        accountRepository.deleteById(id);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);}
}
