package su.opencode.digsigcik.synapi.service;

import org.jetbrains.annotations.NotNull;
import su.opencode.digsigcik.synapi.model.Credential;

import java.util.List;

public interface CredentialService {
    List<Credential> findAllCredential();
    Credential saveCredential(@NotNull Credential credential);
    Credential findByCredentialId(@NotNull Long id);
    List<Credential> findCredentialByCodeOrName(String codeOrName);
    Credential updateCredential(@NotNull Credential credential);
    void deleteCredential(@NotNull Long id);
}
