package su.opencode.digsigcik.synapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import su.opencode.digsigcik.synapi.model.PublicKeyStatement;

@Repository
public interface PublicKeyStatementRepository extends JpaRepository<PublicKeyStatement, Long> {
    PublicKeyStatement findPublicKeyStatementById(Long id);
}
