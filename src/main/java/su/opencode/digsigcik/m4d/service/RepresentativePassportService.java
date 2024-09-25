package su.opencode.digsigcik.m4d.service;

import su.opencode.digsigcik.m4d.model.RepresentativePassport;
import javax.validation.constraints.NotNull;
import java.util.List;

public interface RepresentativePassportService {
    List<RepresentativePassport> findAllRepresentativePassport();
    RepresentativePassport saveRepresentativePassport(@NotNull RepresentativePassport representativePassport);
    RepresentativePassport findByRepresentativePassportId(@NotNull Long id);
    RepresentativePassport findRepresentativePassportBySeriesNumber(@NotNull String seriesNumber);
    RepresentativePassport findRepresentativePassportyByRepresentativeId(@NotNull Long representativeId);
    RepresentativePassport updateRepresentativePassport(@NotNull RepresentativePassport representativePassport);
    void deleteRepresentativePassport(@NotNull Long id);
}
