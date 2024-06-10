package su.opencode.digsigcik.synapi.service;

import su.opencode.digsigcik.synapi.dto.MrpaRegistryFullDTO;
import su.opencode.digsigcik.synapi.model.MrpaRegistry;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

public interface MrpaRegistryService {
    List<MrpaRegistry> findAllMrpaRegistry();
    MrpaRegistry saveMrpaRegistry(@NotNull MrpaRegistry mrpaRegistry);
    List<Map<String, Object>> findByMrpaRegistryId(@NotNull Long id);
    List<MrpaRegistryFullDTO> findByMrpaRegistryUserId(@NotNull Long userId);
    MrpaRegistry updateMrpaRegistry(@NotNull MrpaRegistry mrpaRegistry);
    void deleteMrpaRegistry(@NotNull Long id);

}
