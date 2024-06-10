package su.opencode.digsigcik.synapi.service;

import su.opencode.digsigcik.synapi.model.RepresentativePassport;

import javax.validation.constraints.NotNull;
import java.util.List;

public interface RepresentativePassportService {
    List<RepresentativePassport> findAllRepresentativePassport();
    RepresentativePassport saveRepresentativePassport(@NotNull RepresentativePassport representativePassport);
    RepresentativePassport findByRepresentativePassportId(@NotNull Long id);
    RepresentativePassport updateRepresentativePassport(@NotNull RepresentativePassport representativePassport);
    void deleteRepresentativePassport(@NotNull Long id);
}
