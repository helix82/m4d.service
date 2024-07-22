package su.opencode.digsigcik.synapi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import su.opencode.digsigcik.synapi.model.SettingsEntity;
import su.opencode.digsigcik.synapi.service.SettingsService;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/settings")
public class SettingsController {

    @Autowired
    private SettingsService settingsService;

    @GetMapping
    public ResponseEntity<List<SettingsEntity>> getAllSettings() {
        List<SettingsEntity> settings = settingsService.getAllSettings();
        return ResponseEntity.ok(settings);
    }

    @GetMapping("/key/{settingKey}")
    public ResponseEntity<SettingsEntity> getSettingsByKey(@PathVariable("settingKey") String settingKey) {
        Optional<SettingsEntity> settings = settingsService.getSettingsByKey(settingKey);
        return settings.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
    @PostMapping
    public ResponseEntity<SettingsEntity> createOrUpdateSettings(@Valid @RequestBody SettingsEntity settingsEntity) {
        SettingsEntity savedEntity = settingsService.saveSettings(settingsEntity);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedEntity);
    }

    @DeleteMapping("/{key}")
    public ResponseEntity<Void> deleteSettingsByKey(@PathVariable("key") String key) {
        settingsService.deleteSettingsByKey(key);
        return ResponseEntity.noContent().build();
    }
}
