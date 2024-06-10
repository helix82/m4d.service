package su.opencode.digsigcik.synapi.service;

import su.opencode.digsigcik.synapi.model.Principal;

import javax.validation.constraints.NotNull;
import java.util.List;

public interface PrincipalService {
    List<Principal> findAllPrincipal();

    Principal savePrincipal(@NotNull Principal principal);

    Principal findByPrincipalId(@NotNull Long id);

    Principal updatePrincipal(@NotNull Principal principal);

    void deletePrincipal(@NotNull Long id);
}
