package su.opencode.digsigcik.m4d.service;

import org.jetbrains.annotations.NotNull;
import org.springframework.web.multipart.MultipartFile;
import su.opencode.digsigcik.m4d.model.Credential;
import su.opencode.digsigcik.specification.SpecificationRequest;
import su.opencode.digsigcik.specification.SpecificationResponse;

import java.io.IOException;
import java.util.List;

public interface CredentialService {
    Iterable<Credential> findAllCredential();
    SpecificationResponse<Credential> findAllCredential(SpecificationRequest request);
    Credential saveCredential(@NotNull Credential credential);
    Credential findByCredentialId(@NotNull Long id);
    List<Credential> findCredentialByCodeOrName(@NotNull String codeOrName);
    List<Credential> findCredentialByCode(@NotNull String code);
    List<Credential> findCredentialByMrpaId(@NotNull Long mrpaId);
    Credential updateCredential(@NotNull Credential credential);
    void deleteCredential(@NotNull Long id);
    Integer uploadFromJson(MultipartFile sourceFile) throws IOException;
}
