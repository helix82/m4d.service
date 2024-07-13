package su.opencode.digsigcik.synapi.service;

import su.opencode.digsigcik.synapi.model.RevokedCertificate;

import java.util.List;

public interface RevokedCertificateService {
    List<RevokedCertificate> findAllRevokedCertificate();
    RevokedCertificate saveRevokedCertificate(RevokedCertificate revokedCertificate);
    RevokedCertificate findByRevokedCertificateId(Long id);
    RevokedCertificate updateRevokedCertificate(RevokedCertificate revokedCertificate);
    void deleteRevokedCertificate(Long id);
    Integer countRevokedCertificateByImprint(String imprint);
}
