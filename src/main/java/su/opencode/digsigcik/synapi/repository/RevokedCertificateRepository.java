package su.opencode.digsigcik.synapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import su.opencode.digsigcik.synapi.model.RevokedCertificate;

@Repository
public interface RevokedCertificateRepository extends JpaRepository<RevokedCertificate, Long> {
    RevokedCertificate findRevokedCertificateById(Long id);

    @Query("SELECT count(rc.id) from RevokedCertificate rc where rc.imprint=?1")
    Integer countRevokedCertificateByImprint(String imprint);
}
