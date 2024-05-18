package su.opencode.digsigcik.synapi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import su.opencode.digsigcik.synapi.model.Principal;
import su.opencode.digsigcik.synapi.repository.PrincipalRepository;

import java.util.List;

@Service
@Primary
public class PrincipalServiceImpl implements PrincipalService{
    private final PrincipalRepository repository;

    @Autowired
    public PrincipalServiceImpl(PrincipalRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Principal> findAllPrincipal() {
        return this.repository.findAll();
    }

    @Override
    public Principal savePrincipal(Principal principal) {
        return this.repository.save(principal);
    }

    @Override
    public Principal findByPrincipalId(Long id) {
        return this.repository.findPrincipalById(id);
    }

    @Override
    public Principal updatePrincipal(Principal principal) {
        return this.repository.save(principal);
    }

    @Override
    public void deletePrincipal(Long id) {
        this.repository.deleteById(id);
    }
}
