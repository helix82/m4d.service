package su.opencode.digsigcik.synapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import su.opencode.digsigcik.synapi.model.MrpaRegistryCredential;

@Repository
public interface MrpaRegistryCredentialRepository extends JpaRepository<MrpaRegistryCredential, Long> {
    @Modifying
    @Transactional
    @Query("delete from MrpaRegistryCredential mrc where mrc.mrpaRegistryId=?1")
    void deleteAllByMrpaRegistryId(Long mrpaRegistryId);
}
