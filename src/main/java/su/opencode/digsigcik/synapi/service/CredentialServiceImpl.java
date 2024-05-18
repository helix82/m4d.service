package su.opencode.digsigcik.synapi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import su.opencode.digsigcik.synapi.model.Credential;
import su.opencode.digsigcik.synapi.repository.CredentialRepository;

import java.util.List;

@Service
@Primary
public class CredentialServiceImpl implements CredentialService {
    private final CredentialRepository repository;

    @Autowired
    public CredentialServiceImpl(CredentialRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Credential> findAllCredential() {
        return this.repository.findAll();
    }

    @Override
    public Credential saveCredential(Credential credential) {
        return this.repository.save(credential);
    }

    @Override
    public Credential findByCredentialId(Long id) {
        return this.repository.findCredentialById(id);
    }

    @Override
    public Credential updateCredential(Credential credential) {
        return this.repository.save(credential);
    }

    @Override
    public void deleteCredential(Long id) {
        this.repository.deleteById(id);
    }
}
