package su.opencode.digsigcik.m4d.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import su.opencode.digsigcik.m4d.exception.BadRequestException;
import su.opencode.digsigcik.m4d.exception.M4dException;
import su.opencode.digsigcik.m4d.model.RepresentativePassport;
import su.opencode.digsigcik.m4d.repository.RepresentativePassportRepository;
import su.opencode.digsigcik.m4d.repository.RepresentativeRepository;
import su.opencode.digsigcik.m4d.service.RepresentativePassportService;

import javax.validation.constraints.NotNull;
import java.util.List;

@Service
@Primary
@RequiredArgsConstructor
public class RepresentativePassportServiceImpl implements RepresentativePassportService {
    private final RepresentativePassportRepository representativePassportRepository;
    private final RepresentativeRepository representativeRepository;

    @Override
    public List<RepresentativePassport> findAllRepresentativePassport() {
        return this.representativePassportRepository.findAll();
    }

    @Override
    public RepresentativePassport saveRepresentativePassport(@NotNull RepresentativePassport representativePassport) {
        return this.representativePassportRepository.save(representativePassport);
    }

    @Override
    public RepresentativePassport findByRepresentativePassportId(@NotNull Long id) {
        return this.representativePassportRepository.findRepresentativePassportById(id);
    }

    @Override
    public RepresentativePassport findRepresentativePassportBySeriesNumber(@NotNull String seriesNumber) {
        return this.representativePassportRepository.findRepresentativePassportBySeriesNumber(seriesNumber);
    }

    @Override
    public RepresentativePassport findRepresentativePassportyByRepresentativeId(@NotNull Long representativeId) {
        return this.representativePassportRepository.findRepresentativePassportByRepresentativeId(representativeId);
    }

    @Override
    public RepresentativePassport updateRepresentativePassport(@NotNull RepresentativePassport representativePassport) {
        return this.representativePassportRepository.save(representativePassport);
    }

    @Override
    public void deleteRepresentativePassport(@NotNull Long id) {
        this.representativePassportRepository.deleteById(id);
    }
}
