package su.opencode.digsigcik.synapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import su.opencode.digsigcik.synapi.model.RootCertificate;

import java.util.List;

@Repository
public interface RootCertificateRepository extends JpaRepository<RootCertificate, Long> {
    RootCertificate findCertificateById(Long id);
}
