package su.opencode.digsigcik.synapi.service;

import su.opencode.digsigcik.synapi.model.Organisation;

import javax.validation.constraints.NotNull;
import java.util.List;

public interface OrganisationService {
    List<Organisation> findAllOrganisation();
    Organisation saveOrganisation(@NotNull Organisation organisation);
    Organisation findByOrganisationId(@NotNull Long id);
    Organisation updateOrganisation(@NotNull Organisation organisation);
    void deleteOrganisation(@NotNull Long id);
}
