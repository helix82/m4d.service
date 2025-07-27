package su.opencode.digsigcik.m4d.service;

import su.opencode.digsigcik.m4d.dto.OrganizationDto;

public interface RefBookClientService {
    OrganizationDto findByOrganisationInn(String inn, String bearerToken);
}
