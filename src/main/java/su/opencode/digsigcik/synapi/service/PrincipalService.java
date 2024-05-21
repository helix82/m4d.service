package su.opencode.digsigcik.synapi.service;

import su.opencode.digsigcik.synapi.model.Principal;

import java.util.List;

public interface PrincipalService {
    List<Principal> findAllPrincipal();
    Principal savePrincipal(Principal principal);
    Principal findByPrincipalId(Long id);
    Principal updatePrincipal(Principal principal);
    void deletePrincipal(Long id);
}
