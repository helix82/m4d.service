package su.opencode.digsigcik.synapi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import su.opencode.digsigcik.synapi.model.Credential;
import su.opencode.digsigcik.synapi.model.MrpaRegistry;
import su.opencode.digsigcik.synapi.repository.CredentialRepository;
import su.opencode.digsigcik.synapi.repository.MrpaRegistryRepository;

import java.util.List;
import java.util.Set;

@Service
@Primary
public class MrpaRegistryServiceImpl implements MrpaRegistryService{

    private final MrpaRegistryRepository repository;
    private final CredentialRepository credentialRepository;

    @Autowired
    public MrpaRegistryServiceImpl(CredentialRepository credentialRepository, MrpaRegistryRepository repository) {
        this.repository = repository;
        this.credentialRepository = credentialRepository;
    }

    @Override
    public List<MrpaRegistry> findAllMrpaRegistry() {
        return this.repository.findAll();
    }

    @Override
    public MrpaRegistry saveMrpaRegistry(MrpaRegistry mrpaRegistry) {
        return this.repository.save(mrpaRegistry);
    }

    @Override
    public MrpaRegistry findByMrpaRegistryId(Long id) {
        return this.repository.findMrpaRegistryById(id);
    }

    @Override
    public MrpaRegistry updateMrpaRegistry(MrpaRegistry mrpaRegistry) {
        return this.repository.save(mrpaRegistry);
    }

    @Override
    public void deleteMrpaRegistry(Long id) { this.repository.deleteById(id);
    }

    @Override
    public MrpaRegistry assignCredentialToMrpaRegistry(Long mrpaId, Long credentialId) {
        Set<Credential> credentialSet;
        MrpaRegistry mrpaRegistry = this.repository.findMrpaRegistryById(mrpaId);
        Credential credential = this.credentialRepository.findCredentialById(credentialId);
        credentialSet = mrpaRegistry.getAssignedCredentials();
        credentialSet.add(credential);
        mrpaRegistry.setAssignedCredentials(credentialSet);
        return this.repository.save(mrpaRegistry);
    }
}
