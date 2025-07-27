package su.opencode.digsigcik.m4d.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import su.opencode.digsigcik.m4d.model.RepresentativeDetail;
import su.opencode.digsigcik.m4d.repository.RepresentativeDetailRepository;
import su.opencode.digsigcik.m4d.service.RepresentativeDetailService;

import javax.validation.constraints.NotNull;

@Service
@Primary
@RequiredArgsConstructor
public class RepresentativeDetailServiceImpl implements RepresentativeDetailService {
    private final RepresentativeDetailRepository representativeDetailRepository;

    @Override
    public RepresentativeDetail saveRepresentativeDetail(@NotNull RepresentativeDetail representativeDetail) {
        return this.representativeDetailRepository.save(representativeDetail);
    }

    @Override
    public RepresentativeDetail findByRepresentativeDetailId(@NotNull Long id) {
        return this.representativeDetailRepository.findRepresentativeDetailById(id);
    }

    @Override
    public RepresentativeDetail findRepresentativeDetailByRepresentativeId(@NotNull Long representativeId) {
        return this.representativeDetailRepository.findRepresentativeDetailByRepresentativeId(representativeId);
    }

    @Override
    public void deleteRepresentativeDetailByRepresId(@NotNull Long id) {
        this.representativeDetailRepository.deleteByRepresentativeId(id);
    }
}
