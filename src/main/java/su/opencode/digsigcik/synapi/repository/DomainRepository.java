package su.opencode.digsigcik.synapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import su.opencode.digsigcik.synapi.model.AcmeDomain;

import java.util.List;

@Repository
public interface DomainRepository extends JpaRepository<AcmeDomain, Long> {
    AcmeDomain findDomainById(Long id);

    AcmeDomain findDomainByDomainUrl(String domainUrl);

    List<AcmeDomain> findByAccountId(Long accountId);

    @Transactional
    void deleteByAccountId(long accountId);
}
