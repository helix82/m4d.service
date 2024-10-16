package su.opencode.digsigcik.m4d.service;

import org.jetbrains.annotations.NotNull;
import su.opencode.digsigcik.m4d.model.Credential;

import java.util.List;

public interface CredentialService {
    List<Credential> findAllCredential();
    Credential saveCredential(@NotNull Credential credential);
    Credential findByCredentialId(@NotNull Long id);
    List<Credential> findCredentialByCodeOrName(@NotNull String codeOrName);
    List<Credential> findCredentialByCode(@NotNull String code);
    List<Credential> findCredentialByMrpaId(@NotNull Long mrpaId);
    Credential updateCredential(@NotNull Credential credential);
    void deleteCredential(@NotNull Long id);
}
