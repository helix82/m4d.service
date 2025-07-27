package su.opencode.digsigcik.m4d.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import su.opencode.digsigcik.m4d.model.Representative;
import su.opencode.digsigcik.m4d.repository.RepresentativeRepository;
import su.opencode.digsigcik.m4d.service.RepresentativeService;

import javax.validation.constraints.NotNull;

@Service
@Primary
@RequiredArgsConstructor
public class RepresentativeServiceImpl implements RepresentativeService {
    private final RepresentativeRepository repository;

    @Override
    public Representative saveRepresentative(@NotNull Representative representative) {
        return this.repository.save(representative);
    }

    @Override
    public Representative findByRepresentativeId(@NotNull Long id) {
        return this.repository.findRepresentativeById(id);
    }

    @Override
    public void deleteRepresentative(@NotNull Long id) {
        this.repository.deleteById(id);
    }
}
