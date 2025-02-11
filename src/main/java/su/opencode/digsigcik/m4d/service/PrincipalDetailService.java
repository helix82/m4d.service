package su.opencode.digsigcik.m4d.service;

import su.opencode.digsigcik.m4d.model.PrincipalDetail;

import javax.validation.constraints.NotNull;

public interface PrincipalDetailService {
    PrincipalDetail savePrincipalDetail(@NotNull PrincipalDetail principalDetail);
    PrincipalDetail findByPrincipalDetailId(@NotNull Long id);
}
