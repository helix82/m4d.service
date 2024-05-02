package su.opencode.digsigcik.synapi.service;

import su.opencode.digsigcik.synapi.model.MrpaRegistry;

import java.util.List;

public interface MrpaRegistryService {
    List<MrpaRegistry> findAllMrpaRegistry();
    MrpaRegistry saveMrpaRegistry(MrpaRegistry mrpaRegistry);
    MrpaRegistry findByMrpaRegistryId(Long id);
    MrpaRegistry updateMrpaRegistry(MrpaRegistry mrpaRegistry);
    void deleteMrpaRegistry(Long id);
}
