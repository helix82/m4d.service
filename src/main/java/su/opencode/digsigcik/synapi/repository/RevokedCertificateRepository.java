package su.opencode.digsigcik.synapi.repository;

import org.springframework.stereotype.Repository;
import su.opencode.digsigcik.synapi.model.RevokedCertificate;

@Repository
public interface RevokedCertificateRepository {
    RevokedCertificate findRevokedCertificateById(Long id);
}
