package su.opencode.digsigcik.synapi.service;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import su.opencode.digsigcik.synapi.model.Representative;
import su.opencode.digsigcik.synapi.repository.RepresentativeRepository;

import java.util.List;

@Service
@AllArgsConstructor
@Primary
public class RepresentativeServiceImpl implements RepresentativeService {
    private final RepresentativeRepository repository;

    @Override
    public List<Representative> findAllRepresentative() {
        return repository.findAll();
    }

    @Override
    public Representative saveRepresentative(Representative representative) {
        return repository.save(representative);
    }

    @Override
    public Representative findByRepresentativeId(Long id) {
        return repository.findRepresentativeById(id);
    }

    @Override
    public Representative updateRepresentative(Representative representative) {
        return repository.save(representative);
    }

    @Override
    public void deleteRepresentative(Long id) {
        repository.deleteById(id);
    }
}
