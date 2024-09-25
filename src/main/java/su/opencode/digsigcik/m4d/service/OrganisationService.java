package su.opencode.digsigcik.m4d.service;

import su.opencode.digsigcik.m4d.model.Organisation;

import javax.validation.constraints.NotNull;
import java.util.List;

public interface OrganisationService {
    List<Organisation> findAllOrganisation();
    Organisation saveOrganisation(@NotNull Organisation organisation);
    Organisation findByOrganisationId(@NotNull Long id);
    Organisation findByOrganisationInn(String inn);
    Organisation updateOrganisation(@NotNull Organisation organisation);
    void deleteOrganisation(@NotNull Long id);
}
