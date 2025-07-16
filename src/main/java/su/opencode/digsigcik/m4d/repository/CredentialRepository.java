package su.opencode.digsigcik.m4d.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import su.opencode.digsigcik.m4d.model.Credential;
import su.opencode.digsigcik.specification.repository.ISpecificationRepository;

import java.util.List;

@Repository
public interface CredentialRepository extends ISpecificationRepository<Credential> {
    Credential findCredentialById(Long id);
    List<Credential> findCredentialByCodeIgnoreCase(String code);
    List<Credential> findCredentialByCodeContainingIgnoreCase(String code);
    List<Credential> findCredentialByNameContainingIgnoreCase(String name);
    @Query(value = "SELECT c.id, c.code, c.name FROM m4d_credential c left join m4d_registry_m4d_credential mrc on mrc.credential_id=c.id where mrc.mrpa_registry_id=:mrpaId", nativeQuery = true)
    List<Credential> findCredentialByMrpaId(Long mrpaId);
}
