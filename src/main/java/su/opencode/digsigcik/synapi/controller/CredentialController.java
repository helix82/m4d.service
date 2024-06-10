package su.opencode.digsigcik.synapi.controller;

import lombok.AllArgsConstructor;
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
import su.opencode.digsigcik.synapi.annotation.MchdExceptionHandler;
import su.opencode.digsigcik.synapi.exception.BadRequestException;
import su.opencode.digsigcik.synapi.model.Credential;
import su.opencode.digsigcik.synapi.service.CredentialService;

import javax.validation.ConstraintViolationException;
import java.util.List;

import static su.opencode.digsigcik.synapi.util.Constant.ID_IS_NULL;
import static su.opencode.digsigcik.synapi.util.Constant.NULL;

@RestController
@MchdExceptionHandler
@RequestMapping("/api/v1/credential")
@AllArgsConstructor
public class CredentialController {
    private final String CREDENTIAL_IS_NULL = String.format("Credential is %s!", NULL);

    private final CredentialService service;

    @GetMapping()
    public List<Credential> findAllCredential() {
        return service.findAllCredential();
    }

    @PostMapping("save")
    public ResponseEntity<?> saveCredential(@RequestBody Credential credential) {
        Credential response;
        try {
            response = service.saveCredential(credential);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(CREDENTIAL_IS_NULL);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public Credential findByCredentialId(@PathVariable Long id) {
        return service.findByCredentialId(id);
    }

    @PutMapping("update")
    public ResponseEntity<?> updateCredential(@RequestBody Credential credential) {
        Credential response;
        try {
            response = service.updateCredential(credential);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(CREDENTIAL_IS_NULL);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("delete/{id}")
    public ResponseEntity<?> deleteCredential(@PathVariable Long id) {
        try {
            service.deleteCredential(id);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(ID_IS_NULL);
        }
        return ResponseEntity.ok().build();
    }
}
