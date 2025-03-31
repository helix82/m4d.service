package su.opencode.digsigcik.m4d.service;

import su.opencode.digsigcik.m4d.dto.OrganizationDto;

import javax.validation.constraints.NotNull;

public interface RefBookClientService {
    OrganizationDto saveOrganization(@NotNull OrganizationDto organizationDto, String bearerToken);
    OrganizationDto findByOrganisationInn(String inn, String bearerToken);
}
