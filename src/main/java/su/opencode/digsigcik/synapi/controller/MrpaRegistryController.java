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
import su.opencode.digsigcik.synapi.dto.MrpaRegistryFullDTO;
import su.opencode.digsigcik.synapi.exception.BadRequestException;
import su.opencode.digsigcik.synapi.model.MrpaRegistry;
import su.opencode.digsigcik.synapi.model.MrpaRegistryCredential;
import su.opencode.digsigcik.synapi.service.MrpaRegistryCredentialService;
import su.opencode.digsigcik.synapi.service.MrpaRegistryService;

import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.Map;

import static su.opencode.digsigcik.synapi.util.Constant.ID_IS_NULL;
import static su.opencode.digsigcik.synapi.util.Constant.NULL;

@RestController
@MchdExceptionHandler
@RequestMapping("/api/v1/mrpa_registry")
@AllArgsConstructor
public class MrpaRegistryController {
    private final String MRPA_REGISTRY_IS_NULL = String.format("Mrpa registry is %s!", NULL);
    private final MrpaRegistryService service;
    private final MrpaRegistryCredentialService mrpaRegistryCredentialService;

    @GetMapping()
    public List<MrpaRegistry> findAllMrpaRegistry() {
        return service.findAllMrpaRegistry();
    }

    @PostMapping("save")
    public ResponseEntity<?> saveMrpaRegistry(@RequestBody MrpaRegistry mrpaRegistry) {
        MrpaRegistry response;
        try {
            response = service.saveMrpaRegistry(mrpaRegistry);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(MRPA_REGISTRY_IS_NULL);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
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
    public ResponseEntity<?> updateMrpaRegistry(@RequestBody MrpaRegistry mrpaRegistry) {
        MrpaRegistry response;
        try {
            response = service.updateMrpaRegistry(mrpaRegistry);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(MRPA_REGISTRY_IS_NULL);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("delete/{id}")
    public ResponseEntity<?> deleteMrpaRegistry(@PathVariable Long id) {
        try {
            service.deleteMrpaRegistry(id);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(ID_IS_NULL);
        }
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{mrpaId}/credential/{credentialId}")
    public MrpaRegistryCredential assignCredentialToMrpaRegistry(@PathVariable Long mrpaId, @PathVariable Long credentialId) {
        MrpaRegistryCredential mrpaRegistryCredential = new MrpaRegistryCredential();
        mrpaRegistryCredential.setMrpaRegistryId(mrpaId);
        mrpaRegistryCredential.setCredentialId(credentialId);
        return mrpaRegistryCredentialService.saveMrpaRegistryCredential(mrpaRegistryCredential);
    }
}
