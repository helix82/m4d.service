package su.opencode.digsigcik.m4d.service;

import su.opencode.digsigcik.m4d.model.RepresentativeDetail;

import javax.validation.constraints.NotNull;
import java.util.List;

public interface RepresentativeDetailService {
    List<RepresentativeDetail> findAllRepresentativeDetail();
    RepresentativeDetail saveRepresentativeDetail(@NotNull RepresentativeDetail representativePassport);
    RepresentativeDetail findByRepresentativeDetailId(@NotNull Long id);
    RepresentativeDetail findRepresentativeDetailBySeriesNumber(@NotNull String seriesNumber);
    RepresentativeDetail findRepresentativeDetailByRepresentativeId(@NotNull Long representativeId);
    RepresentativeDetail updateRepresentativeDetail(@NotNull RepresentativeDetail representativePassport);
    void deleteRepresentativeDetailByRepresId(@NotNull Long id);
}
