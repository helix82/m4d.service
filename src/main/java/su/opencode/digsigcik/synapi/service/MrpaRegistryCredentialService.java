package su.opencode.digsigcik.synapi.service;

import su.opencode.digsigcik.synapi.model.MrpaRegistryCredential;

public interface MrpaRegistryCredentialService {
    MrpaRegistryCredential saveMrpaRegistryCredential(MrpaRegistryCredential mrpaRegistryCredential);
    MrpaRegistryCredential updateMrpaRegistryCredential(MrpaRegistryCredential mrpaRegistryCredential);
    void deleteMrpaRegistryCredential(Long id);
}
