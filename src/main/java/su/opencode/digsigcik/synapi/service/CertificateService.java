package su.opencode.digsigcik.synapi.service;

import su.opencode.digsigcik.synapi.model.Certificate;

import java.util.List;

public interface CertificateService {
    List<Certificate> findAllCertificate();
    Certificate saveCertificate(Certificate certificate);
    Certificate findCertificateById(Long id);
    Certificate updateCertificate(Certificate certificate);
    void deleteCertificate(Long id);
}
