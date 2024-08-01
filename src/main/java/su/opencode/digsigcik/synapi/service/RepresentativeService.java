package su.opencode.digsigcik.synapi.service;

import su.opencode.digsigcik.synapi.model.Representative;

import javax.validation.constraints.NotNull;
import java.util.List;

public interface RepresentativeService {
    List<Representative> findAllRepresentative();

    Representative saveRepresentative(@NotNull Representative representative);

    Representative findByRepresentativeId(@NotNull Long id);

    Representative findRepresentativeByInn(@NotNull String inn);

    Representative updateRepresentative(@NotNull Representative representative);

    void deleteRepresentative(@NotNull Long id);
}
