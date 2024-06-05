package su.opencode.digsigcik.synapi.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import su.opencode.digsigcik.synapi.dto.MrpaRegistryFullDTO;
import su.opencode.digsigcik.synapi.model.MrpaRegistry;
import su.opencode.digsigcik.synapi.model.MrpaRegistryCredential;
import su.opencode.digsigcik.synapi.service.MrpaRegistryCredentialService;
import su.opencode.digsigcik.synapi.service.MrpaRegistryService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/mrpa_registry")
@AllArgsConstructor
public class MrpaRegistryController {
    private final MrpaRegistryService service;
    private final MrpaRegistryCredentialService mrpaRegistryCredentialService;

    @GetMapping()
    public List<MrpaRegistry> findAllMrpaRegistry() {
        return service.findAllMrpaRegistry();
    }

    @PostMapping("save")
    public MrpaRegistry saveMrpaRegistry(@RequestBody MrpaRegistry mrpaRegistry) {
        return service.saveMrpaRegistry(mrpaRegistry);
    }

    @GetMapping("/{id}")
    public List<Map<String, Object>> findByMrpaRegistryId(@PathVariable Long id) {
        return service.findByMrpaRegistryId(id);
    }

    @GetMapping("user_id/{id}")
    public List<MrpaRegistryFullDTO> findByMrpaRegistryUserId(@PathVariable Long id) {
        return service.findByMrpaRegistryUserId(id);
    }

    @PutMapping("update")
    public MrpaRegistry updateMrpaRegistry(@RequestBody MrpaRegistry mrpaRegistry) {
        return service.updateMrpaRegistry(mrpaRegistry);
    }

    @DeleteMapping("delete/{id}")
    public void deleteMrpaRegistry(@PathVariable Long id) {
        service.deleteMrpaRegistry(id);
    }

    @PutMapping("/{mrpaId}/credential/{credentialId}")
    public MrpaRegistryCredential assignCredentialToMrpaRegistry(@PathVariable Long mrpaId, @PathVariable Long credentialId) {
        MrpaRegistryCredential mrpaRegistryCredential = new MrpaRegistryCredential();
        mrpaRegistryCredential.setMrpaRegistryId(mrpaId);
        mrpaRegistryCredential.setCredentialId(credentialId);
        return mrpaRegistryCredentialService.saveMrpaRegistryCredential(mrpaRegistryCredential);
    }

}
