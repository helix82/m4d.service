package su.opencode.digsigcik.synapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import su.opencode.digsigcik.synapi.model.Credential;

import java.util.List;

@Repository
public interface CredentialRepository extends JpaRepository<Credential, Long> {
    Credential findCredentialById(Long id);

    @Query(value = "SELECT c.id, c.code, c.name FROM credential c left join mrpa_registry_credential mrc on mrc.credential_id=c.id where mrc.mrpa_registry_id=:mrpaId", nativeQuery = true)
    List<Credential> findCredentialByMrpaId(Long mrpaId);

    List<Credential> findCredentialByCodeContainingIgnoreCase(String code);
    List<Credential> findCredentialByNameContainingIgnoreCase(String name);
}
