package su.opencode.digsigcik.m4d.service.impl;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import su.opencode.digsigcik.audit.AuditLogEventEnum;
import su.opencode.digsigcik.audit.AuditLogThis;
import su.opencode.digsigcik.m4d.model.Credential;
import su.opencode.digsigcik.m4d.repository.CredentialRepository;
import su.opencode.digsigcik.m4d.service.CredentialService;

import java.util.ArrayList;
import java.util.List;

@Service
@Primary
@RequiredArgsConstructor
public class CredentialServiceImpl implements CredentialService {
    private final CredentialRepository repository;

    @Override
    public List<Credential> findAllCredential() {
        return this.repository.findAll();
    }

    @Override
    @AuditLogThis(event = AuditLogEventEnum.M4D_CREDENTIAL_CREATE)
    public Credential saveCredential(@NotNull Credential credential) {
        return this.repository.save(credential);
    }

    @Override
    public Credential findByCredentialId(@NotNull Long id) {
        Credential credential = this.repository.findCredentialById(id);
        return credential;
    }

    @Override
    public List<Credential> findCredentialByCodeOrName(@NotNull String codeOrName) {
        List<Credential> credentialList = new ArrayList<>();
        List<Credential> credential = this.repository.findCredentialByCodeContainingIgnoreCase(codeOrName);
        List<Credential> credential1 = this.repository.findCredentialByNameContainingIgnoreCase(codeOrName);
        if (credential != null){ credentialList.addAll(credential); }
        if (credential1 != null){ credentialList.addAll(credential1); }
        return credentialList;
    }

    @Override
    public List<Credential> findCredentialByCode(@NotNull String code) {
        return this.repository.findCredentialByCodeIgnoreCase(code);
    }

    @Override
    public List<Credential> findCredentialByMrpaId(@NotNull Long mrpaId) {
        return this.repository.findCredentialByMrpaId(mrpaId);
    }

    @Override
    @AuditLogThis(event = AuditLogEventEnum.M4D_CREDENTIAL_UPDATE)
    public Credential updateCredential(@NotNull Credential credential) {
        return this.repository.save(credential);
    }

    @Override
    @AuditLogThis(event = AuditLogEventEnum.M4D_CREDENTIAL_DELETE, deleteByIdRepository = CredentialRepository.class)
    public void deleteCredential(@NotNull Long id) {
        this.repository.deleteById(id);
    }
}
