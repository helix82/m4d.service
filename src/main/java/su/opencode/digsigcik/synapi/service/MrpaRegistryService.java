package su.opencode.digsigcik.synapi.service;

import su.opencode.digsigcik.synapi.dto.MrpaRegistryFullDTO;
import su.opencode.digsigcik.synapi.dto.MrpaRegistryWithFioDTO;
import su.opencode.digsigcik.synapi.model.MrpaRegistry;

import javax.validation.constraints.NotNull;
import java.util.List;

public interface MrpaRegistryService {
    List<MrpaRegistryWithFioDTO>  findAllMrpaRegistry();
    MrpaRegistry saveMrpaRegistry(MrpaRegistry mrpaRegistry);
    List<MrpaRegistryWithFioDTO> findByMrpaRegistryId(Long id);
    List<MrpaRegistryWithFioDTO> findAllByMrpaRegistryUserId(Long userId);
    List<MrpaRegistryFullDTO> findByMrpaRegistryUserId(Long userId);
    MrpaRegistry updateMrpaRegistry(MrpaRegistry mrpaRegistry);
    void deleteMrpaRegistry(Long id);
    List<MrpaRegistryWithFioDTO> findByMrpaRegistryPrincipalInn(String inn);
    List<MrpaRegistry> findAllMrpaRegistry();
    MrpaRegistry saveMrpaRegistry(@NotNull MrpaRegistry mrpaRegistry);
    List<Map<String, Object>> findByMrpaRegistryId(@NotNull Long id);
    List<MrpaRegistryFullDTO> findByMrpaRegistryUserId(@NotNull Long userId);
    MrpaRegistry updateMrpaRegistry(@NotNull MrpaRegistry mrpaRegistry);
    void deleteMrpaRegistry(@NotNull Long id);

}
