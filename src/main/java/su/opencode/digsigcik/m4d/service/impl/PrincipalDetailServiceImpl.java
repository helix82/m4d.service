package su.opencode.digsigcik.m4d.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import su.opencode.digsigcik.m4d.model.PrincipalDetail;
import su.opencode.digsigcik.m4d.repository.PrincipalDetailRepository;
import su.opencode.digsigcik.m4d.service.PrincipalDetailService;

import javax.validation.constraints.NotNull;

@Service
@Primary
@RequiredArgsConstructor
public class PrincipalDetailServiceImpl implements PrincipalDetailService {
    private final PrincipalDetailRepository repository;

    @Override
    public PrincipalDetail savePrincipalDetail(@NotNull PrincipalDetail principalDetail) {
        return this.repository.save(principalDetail);
    }

    @Override
    public PrincipalDetail findByPrincipalDetailId(@NotNull Long id) {
        return this.repository.findPrincipalDetailById(id);
    }

    @Override
    public void deletePrincipalDetailByPrincipalId(@NotNull Long id) {
        this.repository.deleteByPrincipalId(id);
    }

}
