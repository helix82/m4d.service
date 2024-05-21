package su.opencode.digsigcik.synapi.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import su.opencode.digsigcik.synapi.model.Organisation;
import su.opencode.digsigcik.synapi.service.OrganisationService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/organisation")
@AllArgsConstructor
public class OrganisationController {
    private final OrganisationService service;

    @GetMapping()
    public List<Organisation> findAllCredential() {
        return service.findAllOrganisation();
    }

    @PostMapping("save")
    public Organisation saveCredential(@RequestBody Organisation organisation) {
        return service.saveOrganisation(organisation);
    }

    @GetMapping("/{id}")
    public Organisation findByCredentialId(@PathVariable Long id) {
        return service.findByOrganisationId(id);
    }

    @PutMapping("update")
    public Organisation updateCredential(@RequestBody Organisation organisation) {
        return service.updateOrganisation(organisation);
    }

    @DeleteMapping("delete/{id}")
    public void deleteCredential(@PathVariable Long id) {
        service.deleteOrganisation(id);
    }
}
