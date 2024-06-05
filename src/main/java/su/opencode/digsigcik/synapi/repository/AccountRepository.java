package su.opencode.digsigcik.synapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import su.opencode.digsigcik.synapi.model.AcmeAccount;

@Repository
public interface AccountRepository extends JpaRepository<AcmeAccount, Long> {
    AcmeAccount findAccountById(Long id);
    AcmeAccount findAccountByExternalKey(String externalKey);
}
