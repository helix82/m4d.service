package su.opencode.digsigcik.synapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import su.opencode.digsigcik.synapi.model.Credential;

@Repository
public interface CredentialRepository extends JpaRepository<Credential, Long> {
    Credential findCredentialById(Long id);
}
