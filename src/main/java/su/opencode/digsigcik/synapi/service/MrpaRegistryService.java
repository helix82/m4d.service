package su.opencode.digsigcik.synapi.service;

import su.opencode.digsigcik.synapi.dto.MrpaRegistryFullDTO;
import su.opencode.digsigcik.synapi.model.MrpaRegistry;

import java.util.List;
import java.util.Map;

public interface MrpaRegistryService {
    List<MrpaRegistry> findAllMrpaRegistry();
    MrpaRegistry saveMrpaRegistry(MrpaRegistry mrpaRegistry);
    List<Map<String, Object>> findByMrpaRegistryId(Long id);
    List<Map<String, Object>> findAllByMrpaRegistryUserId(Long userId);
    List<MrpaRegistryFullDTO> findByMrpaRegistryUserId(Long userId);
    MrpaRegistry updateMrpaRegistry(MrpaRegistry mrpaRegistry);
    void deleteMrpaRegistry(Long id);

}
