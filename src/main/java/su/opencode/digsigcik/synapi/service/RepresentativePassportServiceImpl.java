package su.opencode.digsigcik.synapi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import su.opencode.digsigcik.synapi.exception.BadRequestException;
import su.opencode.digsigcik.synapi.exception.MchdException;
import su.opencode.digsigcik.synapi.model.RepresentativePassport;
import su.opencode.digsigcik.synapi.repository.RepresentativePassportRepository;
import su.opencode.digsigcik.synapi.repository.RepresentativeRepository;

import javax.validation.constraints.NotNull;
import java.util.List;

import static su.opencode.digsigcik.synapi.util.Constant.NOT_FOUND;
import static su.opencode.digsigcik.synapi.util.Constant.NULL_OR_EMPTY_ERROR_MESSAGE;

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
            throw new MchdException(String.format("Can`t create representative passport because representative " +
                    "with id=%d %s", representativeId, NOT_FOUND)
            );
        }
        String seriesNumber = representativePassport.getSeriesNumber();
        if (seriesNumber == null || seriesNumber.isEmpty()) {
            throw new BadRequestException(String.format("Can`t create representative passport because seriesNumber %s",
                    NULL_OR_EMPTY_ERROR_MESSAGE)
            );
        }
        return this.representativePassportRepository.save(representativePassport);
    }

    @Override
    public RepresentativePassport findByRepresentativePassportId(@NotNull Long id) {
        RepresentativePassport representativePassport = this.representativePassportRepository.findRepresentativePassportById(id);
        if (representativePassport == null) {
            throw new MchdException(String.format("Representative passport %s", NOT_FOUND));
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
            throw new MchdException(String.format("Can`t update. Representative passport with id=%d %s",
                    representativePassportId, NOT_FOUND)
            );
        }

        Long representativeId = representativePassport.getRepresentativeId();
        if (representativeRepository.findRepresentativeById(representativeId) == null) {
            throw new MchdException(String.format("Can`t update representative passport because representative " +
                    "with id=%d %s", representativeId, NOT_FOUND)
            );
        }
        return this.representativePassportRepository.save(representativePassport);
    }

    @Override
    public void deleteRepresentativePassport(@NotNull Long id) {
        RepresentativePassport representativePassport = this.representativePassportRepository.findRepresentativePassportById(id);
        if (representativePassport == null) {
            throw new MchdException(String.format("Can`t delete representative passport " +
                    "because representative passport %s", NOT_FOUND));
        }
        this.representativePassportRepository.deleteById(id);
    }
}
