package su.opencode.digsigcik.synapi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import su.opencode.digsigcik.synapi.exception.BadRequestException;
import su.opencode.digsigcik.synapi.exception.MchdException;
import su.opencode.digsigcik.synapi.model.Principal;
import su.opencode.digsigcik.synapi.repository.PrincipalRepository;

import javax.validation.constraints.NotNull;
import java.util.List;

import static su.opencode.digsigcik.synapi.util.Constants.NOT_FOUND;
import static su.opencode.digsigcik.synapi.util.Constants.NULL;
import static su.opencode.digsigcik.synapi.util.Constants.NULL_OR_EMPTY_ERROR_MESSAGE;

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
            throw new BadRequestException(String.format("Can`t create principal because snils %s ",
                    NULL_OR_EMPTY_ERROR_MESSAGE)
            );
        }
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
    public Principal findPrincipalByInn(@NotNull String inn) {
        Principal principal = this.repository.findPrincipalByInn(inn);
        if (principal == null) {
            throw new MchdException(String.format("Principal %s", NOT_FOUND));
        }
        return principal;
    }

    @Override
    public Principal updatePrincipal(@NotNull Principal principal) {
        Long principalId = principal.getId();
        if (principalId == null) {
            throw new BadRequestException(String.format("Can`t update principal because id %s!", NULL));
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
