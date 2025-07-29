package su.opencode.digsigcik.m4d.service;

import su.opencode.digsigcik.m4d.model.Principal;

import javax.validation.constraints.NotNull;

public interface PrincipalService {
    Principal savePrincipal(@NotNull Principal principal);
    Principal findByPrincipalId(@NotNull Long id);
    void deletePrincipal(@NotNull Long id);
}
