package su.opencode.digsigcik.m4d.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import su.opencode.digsigcik.m4d.exception.BadRequestException;
import su.opencode.digsigcik.m4d.exception.M4dException;
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
        String principalSnils = principal.getSnils();
        if (principalSnils == null || principalSnils.isEmpty()) {
            throw new BadRequestException("Can`t create principal because snils  null or empty! ");
        }
        return this.repository.save(principal);
    }

    @Override
    public Principal findByPrincipalId(@NotNull Long id) {
        Principal principal = this.repository.findPrincipalById(id);
        if (principal == null) {
            throw new M4dException("Principal not found!");
        }
        return principal;
    }

    @Override
    public Principal findPrincipalByInn(@NotNull String inn) {
        Principal principal = this.repository.findPrincipalByInn(inn);
        if (principal == null) {
            return null;
        }
        return principal;
    }

    @Override
    public Principal updatePrincipal(@NotNull Principal principal) {
        Long principalId = principal.getId();
        if (principalId == null) {
            throw new BadRequestException("Can`t update principal because id null!");
        }

        Principal existPrincipal = this.repository.findPrincipalById(principalId);
        if (existPrincipal == null) {
            throw new M4dException(String.format("Can`t update. Principal with id=%d not found!", principalId));
        }
        return this.repository.save(principal);
    }

    @Override
    public void deletePrincipal(@NotNull Long id) {
        Principal principal = this.repository.findPrincipalById(id);
        if (principal == null) {
            throw new M4dException("Can`t delete principal because principal not found!");
        }
        this.repository.deleteById(id);
    }
}
