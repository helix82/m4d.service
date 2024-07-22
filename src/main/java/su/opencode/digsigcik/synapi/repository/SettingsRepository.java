package su.opencode.digsigcik.synapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import su.opencode.digsigcik.synapi.model.SettingsEntity;

import java.util.Optional;

@Repository
public interface SettingsRepository extends JpaRepository<SettingsEntity, Long> {
    Optional<SettingsEntity> findByKey(String settingKey);
    void deleteByKey(String settingKey);
}