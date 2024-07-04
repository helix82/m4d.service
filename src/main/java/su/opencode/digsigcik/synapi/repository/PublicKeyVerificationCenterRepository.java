package su.opencode.digsigcik.synapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import su.opencode.digsigcik.synapi.model.PublicKeyVerificationCenter;

@Repository
public interface PublicKeyVerificationCenterRepository extends JpaRepository<PublicKeyVerificationCenter, Long> {
    PublicKeyVerificationCenter findPublicKeyVerificationCenterById(Long id);
}
