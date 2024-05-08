package su.opencode.digsigcik.synapi.service;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import su.opencode.digsigcik.synapi.model.MrpaRegistry;
import su.opencode.digsigcik.synapi.repository.MrpaRegistryRepository;

import java.util.List;

@Service
@AllArgsConstructor
@Primary
public class MrpaRegistryServiceImpl implements MrpaRegistryService{
    private final MrpaRegistryRepository repository;

    @Override
    public List<MrpaRegistry> findAllMrpaRegistry() {
        return repository.findAll();
    }

    @Override
    public MrpaRegistry saveMrpaRegistry(MrpaRegistry mrpaRegistry) {
        return repository.save(mrpaRegistry);
    }

    @Override
    public MrpaRegistry findByMrpaRegistryId(Long id) {
        return repository.findMrpaRegistryById(id);
    }

    @Override
    public MrpaRegistry updateMrpaRegistry(MrpaRegistry mrpaRegistry) {
        return repository.save(mrpaRegistry);
    }

    @Override
    public void deleteMrpaRegistry(Long id) {
        repository.deleteById(id);
    }
}
