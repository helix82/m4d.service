package su.opencode.digsigcik.synapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import su.opencode.digsigcik.synapi.model.ca.CertificateData;

@Repository
public interface CertificateDataRepository extends JpaRepository<CertificateData, Long> {
    CertificateData findCertificateDataById(Long id);

    @Query("SELECT count(cd.id) from CertificateData cd where cd.imprint=?1")
    Integer countCertificateDataByImprint(String imprint);

    @Query("SELECT cd from CertificateData cd where UPPER(cd.keyId)=UPPER(?1)")
    CertificateData findCertificateDataByKeyId(String keyId);
}
