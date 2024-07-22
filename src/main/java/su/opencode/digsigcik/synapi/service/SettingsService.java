package su.opencode.digsigcik.synapi.service;

import su.opencode.digsigcik.synapi.model.SettingsEntity;

import java.util.List;
import java.util.Optional;

public interface SettingsService {

    List<SettingsEntity> getAllSettings();

    Optional<SettingsEntity> getSettingsById(Long id);
    Optional<SettingsEntity> getSettingsByKey(String settingKey);
    SettingsEntity saveSettings(SettingsEntity settingsEntity);

    void deleteSettings(Long id);
}
