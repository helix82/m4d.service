package su.opencode.digsigcik.m4d.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import su.opencode.digsigcik.m4d.model.RepresentativeDetail;
import su.opencode.digsigcik.m4d.repository.RepresentativeDetailRepository;
import su.opencode.digsigcik.m4d.service.RepresentativeDetailService;

import javax.validation.constraints.NotNull;
import java.util.List;

@Service
@Primary
@RequiredArgsConstructor
public class RepresentativeDetailServiceImpl implements RepresentativeDetailService {
    private final RepresentativeDetailRepository representativeDetailRepository;

    @Override
    public List<RepresentativeDetail> findAllRepresentativeDetail() {
        return this.representativeDetailRepository.findAll();
    }

    @Override
    public RepresentativeDetail saveRepresentativeDetail(@NotNull RepresentativeDetail representativeDetail) {
        return this.representativeDetailRepository.save(representativeDetail);
    }

    @Override
    public RepresentativeDetail findByRepresentativeDetailId(@NotNull Long id) {
        return this.representativeDetailRepository.findRepresentativeDetailById(id);
    }

    @Override
    public RepresentativeDetail findRepresentativeDetailBySeriesNumber(@NotNull String seriesNumber) {
        return this.representativeDetailRepository.findRepresentativeDetailBySeriesNumber(seriesNumber);
    }

    @Override
    public RepresentativeDetail findRepresentativeDetailByRepresentativeId(@NotNull Long representativeId) {
        return this.representativeDetailRepository.findRepresentativeDetailByRepresentativeId(representativeId);
    }

    @Override
    public RepresentativeDetail updateRepresentativeDetail(@NotNull RepresentativeDetail representativeDetail) {
        return this.representativeDetailRepository.save(representativeDetail);
    }

    @Override
    public void deleteRepresentativeDetail(@NotNull Long id) {
        this.representativeDetailRepository.deleteById(id);
    }
}
