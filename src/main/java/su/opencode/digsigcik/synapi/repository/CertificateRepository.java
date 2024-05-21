package su.opencode.digsigcik.synapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import su.opencode.digsigcik.synapi.model.Certificate;

@Repository
public interface CertificateRepository extends JpaRepository<Certificate, Long> {
    Certificate findCertificateById(Long id);
}
