package su.opencode.digsigcik.synapi.service;

import su.opencode.digsigcik.synapi.model.TrustedCertificate;

import java.util.List;

public interface TrustedCertificateService {
    List<TrustedCertificate> findAllTrustedCertificate();
    TrustedCertificate saveTrustedCertificate(TrustedCertificate trustedCertificate);
    TrustedCertificate findByTrustedCertificateId(Long id);
    TrustedCertificate updateTrustedCertificate(TrustedCertificate trustedCertificate);
    void deleteTrustedCertificate(Long id);
}
