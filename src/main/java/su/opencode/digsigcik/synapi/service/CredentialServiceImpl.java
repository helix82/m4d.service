package su.opencode.digsigcik.synapi.service;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import su.opencode.digsigcik.synapi.exception.MchdException;
import su.opencode.digsigcik.synapi.model.Credential;
import su.opencode.digsigcik.synapi.repository.CredentialRepository;

import java.util.List;

import static su.opencode.digsigcik.synapi.util.Constant.NOT_FOUND;

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
    public Credential saveCredential(@NotNull Credential credential) {
        return this.repository.save(credential);
    }

    @Override
    public Credential findByCredentialId(@NotNull Long id) {
        Credential credential = this.repository.findCredentialById(id);
        if (credential == null) {
            throw new MchdException(String.format("Credential %s", NOT_FOUND));
        }
        return credential;
    }

    @Override
    public Credential updateCredential(@NotNull Credential credential) {
        Long credentialId = credential.getId();
        if (credentialId == null) {
            throw new MchdException("Can`t update credential because id null!" );
        }

        Credential existCredential = this.repository.findCredentialById(credentialId);
        if (existCredential == null) {
            throw new MchdException(String.format("Can`t update. Credential with id=%d %s", credentialId, NOT_FOUND));
        }
        return this.repository.save(credential);
    }

    @Override
    public void deleteCredential(@NotNull Long id) {
        Credential credential = this.repository.findCredentialById(id);
        if (credential == null) {
            throw new MchdException(String.format("Can`t delete credential because credential %s", NOT_FOUND));
        }
        this.repository.deleteById(id);
    }
}
