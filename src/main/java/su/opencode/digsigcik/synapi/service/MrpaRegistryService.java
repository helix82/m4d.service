package su.opencode.digsigcik.synapi.service;

import su.opencode.digsigcik.synapi.dto.MrpaRegistryWithFioDTO;
import su.opencode.digsigcik.synapi.model.MrpaRegistry;

import javax.validation.constraints.NotNull;
import java.util.List;

public interface MrpaRegistryService {
    List<MrpaRegistryWithFioDTO> findAllMrpaRegistry();

    MrpaRegistry saveMrpaRegistry(@NotNull MrpaRegistry mrpaRegistry);

    List<MrpaRegistryWithFioDTO> findByMrpaRegistryId(@NotNull Long id);

    MrpaRegistry findById(@NotNull Long id);

    MrpaRegistry updateMrpaRegistry(@NotNull MrpaRegistry mrpaRegistry);

    void deleteMrpaRegistry(@NotNull Long id);

    List<MrpaRegistryWithFioDTO> findByMrpaRegistryPrincipalInn(String inn);

    void recallMrpaRegistryById(@NotNull Long id);
}
