package su.opencode.digsigcik.synapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import su.opencode.digsigcik.synapi.model.PublicKeyStatement;

import java.util.List;
import java.util.Optional;

@Repository
public interface PublicKeyStatementRepository extends JpaRepository<PublicKeyStatement, Long> {
    PublicKeyStatement findPublicKeyStatementById(Long id);
    List<PublicKeyStatement> findAllByLogin(String login);
    Optional<PublicKeyStatement> findByIdAndLogin(Long id,String login);
}
