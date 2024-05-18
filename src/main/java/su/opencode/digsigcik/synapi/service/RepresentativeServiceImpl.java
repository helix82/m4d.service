package su.opencode.digsigcik.synapi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import su.opencode.digsigcik.synapi.model.Representative;
import su.opencode.digsigcik.synapi.repository.RepresentativeRepository;

import java.util.List;

@Service
@Primary
public class RepresentativeServiceImpl implements RepresentativeService {
    private final RepresentativeRepository repository;

    @Autowired
    public RepresentativeServiceImpl(RepresentativeRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Representative> findAllRepresentative() {
        return this.repository.findAll();
    }

    @Override
    public Representative saveRepresentative(Representative representative) {
        return this.repository.save(representative);
    }

    @Override
    public Representative findByRepresentativeId(Long id) {
        return this.repository.findRepresentativeById(id);
    }

    @Override
    public Representative updateRepresentative(Representative representative) {
        return this.repository.save(representative);
    }

    @Override
    public void deleteRepresentative(Long id) {
        this.repository.deleteById(id);
    }
}
