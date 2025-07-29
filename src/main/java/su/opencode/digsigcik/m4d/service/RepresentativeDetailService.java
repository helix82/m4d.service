package su.opencode.digsigcik.m4d.service;

import su.opencode.digsigcik.m4d.model.RepresentativeDetail;

import javax.validation.constraints.NotNull;

public interface RepresentativeDetailService {
    RepresentativeDetail saveRepresentativeDetail(@NotNull RepresentativeDetail representativePassport);
    RepresentativeDetail findByRepresentativeDetailId(@NotNull Long id);
    RepresentativeDetail findRepresentativeDetailByRepresentativeId(@NotNull Long representativeId);
    void deleteRepresentativeDetailByRepresId(@NotNull Long id);
}
