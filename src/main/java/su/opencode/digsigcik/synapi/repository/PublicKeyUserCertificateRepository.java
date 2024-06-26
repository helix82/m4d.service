package su.opencode.digsigcik.synapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import su.opencode.digsigcik.synapi.model.PublicKeyUserCertificate;

@Repository
public interface PublicKeyUserCertificateRepository extends JpaRepository<PublicKeyUserCertificate, Long> {
    PublicKeyUserCertificate findPublicKeyUserCertificateById(Long id);
}
