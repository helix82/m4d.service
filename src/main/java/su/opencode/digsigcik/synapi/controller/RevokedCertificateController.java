package su.opencode.digsigcik.synapi.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import su.opencode.digsigcik.synapi.model.RevokedCertificate;
import su.opencode.digsigcik.synapi.service.RevokedCertificateService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/revoked_certificate")
@AllArgsConstructor
public class RevokedCertificateController {
    private final RevokedCertificateService service;

    @GetMapping()
    public List<RevokedCertificate> findAllRevokedCertificate() {
        return service.findAllRevokedCertificate();
    }

    @PostMapping("save")
    public RevokedCertificate saveRevokedCertificate(@RequestBody RevokedCertificate revokedCertificate) {
        return service.saveRevokedCertificate(revokedCertificate);
    }

    @GetMapping("/{id}")
    public RevokedCertificate findByRevokedCertificateId(@PathVariable Long id) {
        return service.findRevokedCertificateById(id);
    }

    @PutMapping("update")
    public RevokedCertificate updateRevokedCertificate(@RequestBody RevokedCertificate revokedCertificate) {
        return service.updateRevokedCertificate(revokedCertificate);
    }

    @DeleteMapping("delete/{id}")
    public void deleteRevokedCertificate(@PathVariable Long id) {
        service.deleteRevokedCertificate(id);
    }

}
