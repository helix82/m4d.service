package su.opencode.digsigcik.m4d.service;

import su.opencode.digsigcik.m4d.model.Representative;

import javax.validation.constraints.NotNull;

public interface RepresentativeService {
    Representative saveRepresentative(@NotNull Representative representative);
    Representative findByRepresentativeId(@NotNull Long id);
    void deleteRepresentative(@NotNull Long id);
}
