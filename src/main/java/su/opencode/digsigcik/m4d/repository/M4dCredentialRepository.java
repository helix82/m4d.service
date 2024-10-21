package su.opencode.digsigcik.m4d.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import su.opencode.digsigcik.m4d.model.M4dCredential;

@Repository
public interface M4dCredentialRepository extends JpaRepository<M4dCredential, Long> {
    @Modifying
    @Transactional
    @Query(value = "delete from m4d_registry_m4d_credential mrc where mrc.mrpa_registry_id=?1", nativeQuery = true)
    void deleteAllByMrpaRegistryId(Long mrpaRegistryId);
}
