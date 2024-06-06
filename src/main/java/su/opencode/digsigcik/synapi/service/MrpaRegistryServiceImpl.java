package su.opencode.digsigcik.synapi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import su.opencode.digsigcik.synapi.dto.MrpaRegistryFullDTO;
import su.opencode.digsigcik.synapi.model.MrpaRegistry;
import su.opencode.digsigcik.synapi.repository.MrpaRegistryRepository;

import java.util.List;
import java.util.Map;

@Service
@Primary
public class MrpaRegistryServiceImpl implements MrpaRegistryService{

    private final MrpaRegistryRepository repository;

    @Autowired
    public MrpaRegistryServiceImpl(MrpaRegistryRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Map<String, Object>>  findAllMrpaRegistry() {
        return this.repository.findAllMrpaRegistries();
    }

    @Override
    public MrpaRegistry saveMrpaRegistry(MrpaRegistry mrpaRegistry) {
        return this.repository.save(mrpaRegistry);
    }

    @Override
    public List<Map<String, Object>> findByMrpaRegistryId(Long id) {
        return this.repository.findMrpaRegistryById(id);
    }

    @Override
    public List<Map<String, Object>> findAllByMrpaRegistryUserId(Long userId) {
        return this.repository.findAllMrpaRegistriesByUserId(userId);
    }

    @Override
    public List<MrpaRegistryFullDTO> findByMrpaRegistryUserId(Long userId) { return this.repository.findMrpaRegistriesByUserId(userId); }

    @Override
    public MrpaRegistry updateMrpaRegistry(MrpaRegistry mrpaRegistry) {
        return this.repository.save(mrpaRegistry);
    }

    @Override
    public void deleteMrpaRegistry(Long id) { this.repository.deleteById(id);
    }

}
