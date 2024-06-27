package su.opencode.digsigcik.synapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import su.opencode.digsigcik.synapi.model.TrustedCertificate;

public interface TrustedCertificateRepository extends JpaRepository<TrustedCertificate, Long> {
    TrustedCertificate findTrustedCertificateById(Long id);

    @Query("SELECT count(tc.id) from TrustedCertificate tc where tc.imprint=?1")
    Integer countTrustedCertificateByImprint(String imprint);
}
