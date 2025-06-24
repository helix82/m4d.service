package su.opencode.digsigcik.m4d.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import su.opencode.digsigcik.m4d.model.Principal;
import su.opencode.digsigcik.m4d.repository.PrincipalRepository;
import su.opencode.digsigcik.m4d.service.PrincipalService;

import javax.validation.constraints.NotNull;
import java.util.List;

@Service
@Primary
@RequiredArgsConstructor
public class PrincipalServiceImpl implements PrincipalService {
    private final PrincipalRepository repository;

    @Override
    public List<Principal> findAllPrincipal() {
        return this.repository.findAll();
    }

    @Override
    public Principal savePrincipal(@NotNull Principal principal) {
        return this.repository.save(principal);
    }

    @Override
    public Principal findByPrincipalId(@NotNull Long id) {
        return this.repository.findPrincipalById(id);
    }

    @Override
    public Principal findPrincipalByInn(@NotNull String inn) {
        return this.repository.findPrincipalByInn(inn);
    }

    @Override
    public Principal updatePrincipal(@NotNull Principal principal) {
        return this.repository.save(principal);
    }

    @Override
    public void deletePrincipal(@NotNull Long id) {
        this.repository.deleteById(id);
    }
}
