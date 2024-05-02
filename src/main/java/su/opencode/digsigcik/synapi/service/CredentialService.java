package su.opencode.digsigcik.synapi.service;

import su.opencode.digsigcik.synapi.model.Credential;

import java.util.List;

public interface CredentialService {
    List<Credential> findAllCredential();
    Credential saveCredential(Credential credential);
    Credential findByCredentialId(Long id);
    Credential updateCredential(Credential credential);
    void deleteCredential(Long id);
}
