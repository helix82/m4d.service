package su.opencode.digsigcik.m4d.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import su.opencode.digsigcik.m4d.exception.BadRequestException;
import su.opencode.digsigcik.m4d.exception.M4dException;
import su.opencode.digsigcik.m4d.model.Organisation;
import su.opencode.digsigcik.m4d.model.Principal;
import su.opencode.digsigcik.m4d.repository.OrganisationRepository;
import su.opencode.digsigcik.m4d.service.OrganisationService;

import javax.validation.constraints.NotNull;
import java.util.List;

@Service
@Primary
@RequiredArgsConstructor
public class OrganisationServiceImpl implements OrganisationService {
    private final OrganisationRepository repository;

    @Override
    public List<Organisation> findAllOrganisation() {
        return this.repository.findAll();
    }

    @Override
    public Organisation saveOrganisation(@NotNull Organisation organisation) {
        String organisationInn = organisation.getInn();
        if (organisationInn == null || organisationInn.isEmpty()){
            throw new BadRequestException("Can`t create organisation because inn null or empty! ");
        }
        return this.repository.save(organisation);
    }

    @Override
    public Organisation findByOrganisationId(@NotNull Long id) {
        Organisation organisation = this.repository.findOrganisationById(id);
        if (organisation == null) {
            throw new M4dException("Organisation not found!");
        }
        return organisation;
    }

    @Override
    public Organisation findByOrganisationInn(String inn) {
        Organisation organisation = this.repository.findOrganisationByInn(inn);
        if (organisation == null) {
            return null;
        }
        return organisation;
    }

    @Override
    public Organisation updateOrganisation(@NotNull Organisation organisation) {
        Long organisationId = organisation.getId();
        if (organisationId == null) {
            throw new BadRequestException("Can`t update organisation because id null!" );
        }

        Organisation existOrganisation = this.repository.findOrganisationById(organisationId);
        if (existOrganisation == null) {
            throw new M4dException(String.format("Can`t update. Organisation with id=%d not found!", organisationId));
        }
        return this.repository.save(organisation);
    }

    @Override
    public void deleteOrganisation(@NotNull Long id) {
        Organisation organisation = this.repository.findOrganisationById(id);
        if (organisation == null) {
            throw new M4dException("Can`t delete organisation, because organisation not found!");
        }
        this.repository.deleteById(id);
    }
}
