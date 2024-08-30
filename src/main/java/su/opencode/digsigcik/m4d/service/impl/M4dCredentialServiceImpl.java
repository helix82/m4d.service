package su.opencode.digsigcik.m4d.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import su.opencode.digsigcik.m4d.model.M4dCredential;
import su.opencode.digsigcik.m4d.repository.M4dCredentialRepository;
import su.opencode.digsigcik.m4d.service.M4dCredentialService;

@Service
@Primary
public class M4dCredentialServiceImpl implements M4dCredentialService {
    private final M4dCredentialRepository repository;

    @Autowired
    public M4dCredentialServiceImpl(M4dCredentialRepository repository) {
        this.repository = repository;
    }

    @Override
    public M4dCredential saveMrpaRegistryCredential(M4dCredential m4dCredential) {
        return this.repository.save(m4dCredential);
    }
}
