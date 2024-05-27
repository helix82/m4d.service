package su.opencode.digsigcik.synapi.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import su.opencode.digsigcik.synapi.model.Certificate;
import su.opencode.digsigcik.synapi.service.CertificateService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/certificate")
@AllArgsConstructor
public class CertificateController {
    private final CertificateService service;

    @GetMapping()
    public List<Certificate> findAllCertificate() {
        return service.findAllCertificate();
    }

    @PostMapping("save")
    public Certificate saveCertificate(@RequestBody Certificate certificate) {
        return service.saveCertificate(certificate);
    }

    @GetMapping("/{id}")
    public Certificate findByCertificateId(@PathVariable Long id) {
        return service.findCertificateById(id);
    }

    @PutMapping("update")
    public Certificate updateCertificate(@RequestBody Certificate certificate) {
        return service.updateCertificate(certificate);
    }

    @DeleteMapping("delete/{id}")
    public void deleteCertificate(@PathVariable Long id) {
        service.deleteCertificate(id);
    }

}
