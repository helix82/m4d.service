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
        Long representativeId = representativePassport.getRepresentativeId();
        if (representativeId == null) {
            throw new BadRequestException("Can`t create representative passport because representative id null!");
        }
        if (representativeRepository.findRepresentativeById(representativeId) == null) {
            throw new M4dException(String.format("Can`t create representative passport because representative " +
                    "with id=%d not found!", representativeId)
            );
        }
        String seriesNumber = representativePassport.getSeriesNumber();
        if (seriesNumber == null || seriesNumber.isEmpty()) {
            throw new BadRequestException("Can`t create representative passport because seriesNumber null or empty!");
        }
        return this.representativePassportRepository.save(representativePassport);
    }

    @Override
    public RepresentativePassport findByRepresentativePassportId(@NotNull Long id) {
        RepresentativePassport representativePassport = this.representativePassportRepository.findRepresentativePassportById(id);
        if (representativePassport == null) {
            throw new M4dException("Representative passport not found!");
        }
        return representativePassport;
    }

    @Override
    public RepresentativePassport findRepresentativePassportBySeriesNumber(@NotNull String seriesNumber) {
        RepresentativePassport representativePassport = this.representativePassportRepository.findRepresentativePassportBySeriesNumber(seriesNumber);
        if (representativePassport == null) {
            throw new M4dException("Representative passport not found!");
        }
        return representativePassport;
    }

    @Override
    public RepresentativePassport findRepresentativePassportyByRepresentativeId(@NotNull Long representativeId) {
        RepresentativePassport representativePassport = this.representativePassportRepository.findRepresentativePassportByRepresentativeId(representativeId);
        if (representativePassport == null) {
            throw new M4dException("Representative passport not found!");
        }
        return representativePassport;
    }

    @Override
    public RepresentativePassport updateRepresentativePassport(@NotNull RepresentativePassport representativePassport) {
        Long representativePassportId = representativePassport.getId();
        if (representativePassportId == null) {
            throw new BadRequestException("Can`t update representative passport because id null!");
        }

        RepresentativePassport existRepresentativePassport =
                this.representativePassportRepository.findRepresentativePassportById(representativePassportId);
        if (existRepresentativePassport == null) {
            throw new M4dException(String.format("Can`t update. Representative passport with id=%d not found!",
                    representativePassportId)
            );
        }

        Long representativeId = representativePassport.getRepresentativeId();
        if (representativeRepository.findRepresentativeById(representativeId) == null) {
            throw new M4dException(String.format("Can`t update representative passport because representative " +
                    "with id=%d not found!", representativeId)
            );
        }
        return this.representativePassportRepository.save(representativePassport);
    }

    @Override
    public void deleteRepresentativePassport(@NotNull Long id) {
        RepresentativePassport representativePassport = this.representativePassportRepository.findRepresentativePassportById(id);
        if (representativePassport == null) {
            throw new M4dException("Can`t delete representative passport because representative passport not found!");
        }
        this.representativePassportRepository.deleteById(id);
    }
}
