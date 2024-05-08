package su.opencode.digsigcik.synapi.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import su.opencode.digsigcik.synapi.model.Credential;
import su.opencode.digsigcik.synapi.service.CredentialService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/credential")
@AllArgsConstructor
public class CredentialController {
    private final CredentialService service;

    @GetMapping()
    public List<Credential> findAllCredential() {
        return service.findAllCredential();
    }

    @PostMapping("save")
    public Credential saveCredential(@RequestBody Credential credential) {
        return service.saveCredential(credential);
    }

    @GetMapping("/{id}")
    public Credential findByCredentialId(@PathVariable Long id) {
        return service.findByCredentialId(id);
    }

    @PutMapping("update")
    public Credential updateCredential(@RequestBody Credential credential) {
        return service.updateCredential(credential);
    }

    @DeleteMapping("delete/{id}")
    public void deleteCredential(@PathVariable Long id) {
        service.deleteCredential(id);
    }
}
