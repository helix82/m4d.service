package su.opencode.digsigcik.synapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import su.opencode.digsigcik.synapi.model.TrustedCertificate;

public interface TrustedCertificateRepository extends JpaRepository<TrustedCertificate, Long> {
    TrustedCertificate findTrustedCertificateById(Long id);
}
