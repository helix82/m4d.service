package su.opencode.digsigcik.synapi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import su.opencode.digsigcik.synapi.dto.SettingsEntityRequestDto;
import su.opencode.digsigcik.synapi.dto.SettingsEntityResponseDto;
import su.opencode.digsigcik.synapi.mapper.SettingsEntityMapper;
import su.opencode.digsigcik.synapi.model.SettingsEntity;
import su.opencode.digsigcik.synapi.repository.SettingsRepository;

import javax.persistence.EntityNotFoundException;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static su.opencode.digsigcik.synapi.util.Constants.SETTINGS_ENTITY_FAILED_TO_FOUND;
import static su.opencode.digsigcik.synapi.util.Constants.SETTINGS_ENTITY_FAILED_TO_SAVE;

@Transactional
@RequiredArgsConstructor
@Service
public class SettingsServiceImpl implements SettingsService {
    private final SettingsRepository repository;
    private final SettingsEntityMapper mapper;
    private final static String UPDATE_FAIL = "Can`t update. SettingsEntity with key=%s %s";

    @Override
    public List<SettingsEntity> getAllSettings() {
        return repository.findAll();
    }

    @Override
    public Optional<SettingsEntity> getSettingsByKey(String settingKey) {
        return repository.findByKey(settingKey);
    }

    @Override
    public SettingsEntity saveSettings(SettingsEntity settingsEntity) {
        if (!StringUtils.hasText(settingsEntity.getKey()) || !StringUtils.hasText(settingsEntity.getValue())) {
            throw new IllegalArgumentException("Setting key and value cannot be null or empty");
        }
        return repository.save(settingsEntity);
    }

    @Override
    public void deleteSettingsByKey(String settingKey) {
        repository.deleteByKey(settingKey);
    }

    @Override
    public List<SettingsEntityResponseDto> findAllByLogin(String login) {
        return repository.findAllByLogin(login)
                .stream()
                .map(mapper::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public SettingsEntityResponseDto createForUser(@NotNull SettingsEntityRequestDto dto, String login) {
        if (repository.findByKey(dto.getKey()).isPresent()) {
            throw new DataIntegrityViolationException(SETTINGS_ENTITY_FAILED_TO_SAVE);
        }

        return Optional.of(dto)
                .map(dto1 -> mapper.map(dto1, login))
                .map(repository::save)
                .map(mapper::mapToResponseDto)
                .orElseThrow(() -> new DataIntegrityViolationException(SETTINGS_ENTITY_FAILED_TO_SAVE));
    }

    @Override
    public boolean deletePublicKeyStatementForUserByKey(String key, String login) {
        return repository.findByKeyAndLogin(key, login)
                .map(entity -> {
                    repository.delete(entity);
                    repository.flush();
                    return true;
                }).orElse(false);
    }

    @Override
    public SettingsEntityResponseDto findSettingsByKey(String key, String login) {
        return repository.findByKeyAndLogin(key, login)
                .map(mapper::mapToResponseDto)
                .orElseThrow(() -> new EntityNotFoundException(String.format(SETTINGS_ENTITY_FAILED_TO_FOUND)));
    }
}
