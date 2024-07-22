package su.opencode.digsigcik.synapi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import su.opencode.digsigcik.synapi.model.SettingsEntity;
import su.opencode.digsigcik.synapi.repository.SettingsRepository;

import java.util.List;
import java.util.Optional;

@Service
public class SettingsServiceImpl implements SettingsService {

    @Autowired
    private SettingsRepository repository;

    @Override
    public List<SettingsEntity> getAllSettings() {
        return repository.findAll();
    }

    @Override
    public Optional<SettingsEntity> getSettingsById(Long id) {
        return repository.findById(id);
    }

    @Override
    public Optional<SettingsEntity> getSettingsByKey(String settingKey) {
        return repository.findBySettingKey(settingKey);
    }
    @Override
    public SettingsEntity saveSettings(SettingsEntity settingsEntity) {
        if (!StringUtils.hasText(settingsEntity.getKey()) || !StringUtils.hasText(settingsEntity.getValue())) {
            throw new IllegalArgumentException("Setting key and value cannot be null or empty");
        }
        return repository.save(settingsEntity);
    }

    @Override
    public void deleteSettings(Long id) {
        repository.deleteById(id);
    }
}
