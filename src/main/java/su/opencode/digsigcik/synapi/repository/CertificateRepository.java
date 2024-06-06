package su.opencode.digsigcik.synapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import su.opencode.digsigcik.synapi.model.AcmeCertificate;

import java.util.List;

@Repository
public interface CertificateRepository extends JpaRepository<AcmeCertificate, Long> {
    AcmeCertificate findCertificateById(Long id);

    List<AcmeCertificate> findByDomainId(Long accountId);

    @Transactional
    void deleteByDomainId(long domainId);
}
