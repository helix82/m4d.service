package su.opencode.digsigcik.m4d.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import su.opencode.digsigcik.m4d.model.Principal;
import su.opencode.digsigcik.m4d.repository.PrincipalRepository;
import su.opencode.digsigcik.m4d.service.PrincipalService;

import javax.validation.constraints.NotNull;

@Service
@Primary
@RequiredArgsConstructor
public class PrincipalServiceImpl implements PrincipalService {
    private final PrincipalRepository repository;

    @Override
    public Principal savePrincipal(@NotNull Principal principal) {
        return this.repository.save(principal);
    }

    @Override
    public Principal findByPrincipalId(@NotNull Long id) {
        return this.repository.findPrincipalById(id);
    }

    @Override
    public void deletePrincipal(@NotNull Long id) {
        this.repository.deleteById(id);
    }
}
