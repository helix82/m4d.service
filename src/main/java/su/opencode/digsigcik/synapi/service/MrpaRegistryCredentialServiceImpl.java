package su.opencode.digsigcik.synapi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import su.opencode.digsigcik.synapi.model.MrpaRegistryCredential;
import su.opencode.digsigcik.synapi.repository.MrpaRegistryCredentialRepository;

@Service
@Primary
public class MrpaRegistryCredentialServiceImpl implements MrpaRegistryCredentialService{
    private final MrpaRegistryCredentialRepository repository;

    @Autowired
    public MrpaRegistryCredentialServiceImpl(MrpaRegistryCredentialRepository repository) {
        this.repository = repository;
    }

    @Override
    public MrpaRegistryCredential saveMrpaRegistryCredential(MrpaRegistryCredential mrpaRegistryCredential) {
        return this.repository.save(mrpaRegistryCredential);
    }

    @Override
    public MrpaRegistryCredential updateMrpaRegistryCredential(MrpaRegistryCredential mrpaRegistryCredential) {
        return this.repository.save(mrpaRegistryCredential);
    }

    @Override
    public void deleteMrpaRegistryCredential(Long id) {
        this.repository.deleteById(id);
    }
}
