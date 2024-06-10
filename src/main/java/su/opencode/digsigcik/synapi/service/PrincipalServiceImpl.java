package su.opencode.digsigcik.synapi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import su.opencode.digsigcik.synapi.exception.MchdException;
import su.opencode.digsigcik.synapi.model.Principal;
import su.opencode.digsigcik.synapi.repository.PrincipalRepository;

import javax.validation.constraints.NotNull;
import java.util.List;

import static su.opencode.digsigcik.synapi.util.Constant.NOT_FOUND;

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
        Principal principal = this.repository.findPrincipalById(id);
        if (principal == null) {
            throw new MchdException(String.format("Principal %s", NOT_FOUND));
        }
        return principal;
    }

    @Override
    public Principal updatePrincipal(@NotNull Principal principal) {
        Long principalId = principal.getId();
        if (principalId == null) {
            throw new MchdException("Can`t update principal because id null!" );
        }

        Principal existPrincipal = this.repository.findPrincipalById(principalId);
        if (existPrincipal == null) {
            throw new MchdException(String.format("Can`t update. Principal with id=%d %s", principalId, NOT_FOUND));
        }
        return this.repository.save(principal);
    }

    @Override
    public void deletePrincipal(@NotNull Long id) {
        Principal principal = this.repository.findPrincipalById(id);
        if (principal == null) {
            throw new MchdException(String.format("Can`t delete principal because principal %s", NOT_FOUND));
        }
        this.repository.deleteById(id);
    }
}
