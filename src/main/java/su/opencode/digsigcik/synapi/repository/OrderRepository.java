package su.opencode.digsigcik.synapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import su.opencode.digsigcik.synapi.model.AcmeOrder;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<AcmeOrder, Long> {
    AcmeOrder findOrderById(Long id);

    List<AcmeOrder> findByDomainId(Long domainId);

    List<AcmeOrder> findByProviderId(Long providerId);

    @Transactional
    void deleteByDomainId(long domainId);
}
