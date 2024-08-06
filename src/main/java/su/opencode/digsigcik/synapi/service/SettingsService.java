package su.opencode.digsigcik.synapi.service;

import su.opencode.digsigcik.synapi.dto.SettingsEntityRequestDto;
import su.opencode.digsigcik.synapi.dto.SettingsEntityResponseDto;
import su.opencode.digsigcik.synapi.model.SettingsEntity;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

public interface SettingsService {

    List<SettingsEntity> getAllSettings();

    Optional<SettingsEntity> getSettingsByKey(String settingKey);

    SettingsEntity saveSettings(SettingsEntity settingsEntity);

    void deleteSettingsByKey(String settingKey);

    List<SettingsEntityResponseDto> findAllByLogin(String login);

    SettingsEntityResponseDto createForUser(@NotNull SettingsEntityRequestDto dto, String login);

    boolean deletePublicKeyStatementForUserByKey(String key, String login);

    SettingsEntityResponseDto findSettingsByKey(String key, String login);
}
