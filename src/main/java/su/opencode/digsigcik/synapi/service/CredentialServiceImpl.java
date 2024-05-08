package su.opencode.digsigcik.synapi.service;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import su.opencode.digsigcik.synapi.model.Credential;
import su.opencode.digsigcik.synapi.repository.CredentialRepository;

import java.util.List;

@Service
@AllArgsConstructor
@Primary
public class CredentialServiceImpl implements CredentialService {
    private final CredentialRepository repository;

    @Override
    public List<Credential> findAllCredential() {
        return repository.findAll();
    }

    @Override
    public Credential saveCredential(Credential credential) {
        return repository.save(credential);
    }

    @Override
    public Credential findByCredentialId(Long id) {
        return repository.findCredentialById(id);
    }

    @Override
    public Credential updateCredential(Credential credential) {
        return repository.save(credential);
    }

    @Override
    public void deleteCredential(Long id) {
        repository.deleteById(id);
    }
}
