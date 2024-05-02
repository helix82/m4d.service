package su.opencode.digsigcik.synapi.service;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import su.opencode.digsigcik.synapi.model.Principal;
import su.opencode.digsigcik.synapi.repository.PrincipalRepository;

import java.util.List;

@Service
@AllArgsConstructor
@Primary
@Transactional
public class PrincipalServiceImpl implements PrincipalService{
    private final PrincipalRepository repository;

    @Override
    public List<Principal> findAllPrincipal() {
        return repository.findAll();
    }

    @Override
    public Principal savePrincipal(Principal principal) {
        return repository.save(principal);
    }

    @Override
    public Principal findByPrincipalId(Long id) {
        return repository.findPrincipalById(id);
    }

    @Override
    public Principal updatePrincipal(Principal principal) {
        return repository.save(principal);
    }

    @Override
    @Transactional
    public void deletePrincipal(Long id) {
        repository.deleteById(id);
    }
}
