package su.opencode.digsigcik.m4d.service;

import su.opencode.digsigcik.m4d.model.Principal;
import javax.validation.constraints.NotNull;
import java.util.List;

public interface PrincipalService {
    List<Principal> findAllPrincipal();
    Principal savePrincipal(@NotNull Principal principal);
    Principal findByPrincipalId(@NotNull Long id);
    Principal findPrincipalByInn(@NotNull String inn);
    Principal updatePrincipal(@NotNull Principal principal);
    void deletePrincipal(@NotNull Long id);
}
