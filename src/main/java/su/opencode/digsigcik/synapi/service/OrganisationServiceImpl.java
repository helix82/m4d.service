package su.opencode.digsigcik.synapi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import su.opencode.digsigcik.synapi.exception.BadRequestException;
import su.opencode.digsigcik.synapi.exception.MchdException;
import su.opencode.digsigcik.synapi.model.Organisation;
import su.opencode.digsigcik.synapi.repository.OrganisationRepository;

import javax.validation.constraints.NotNull;
import java.util.List;

import static su.opencode.digsigcik.synapi.util.Constant.NOT_FOUND;
import static su.opencode.digsigcik.synapi.util.Constant.NULL_OR_EMPTY_ERROR_MESSAGE;

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
            throw new BadRequestException(String.format("Can`t create organisation because inn %s ",
                    NULL_OR_EMPTY_ERROR_MESSAGE)
            );
        }
        return this.repository.save(organisation);
    }

    @Override
    public Organisation findByOrganisationId(@NotNull Long id) {
        Organisation organisation = this.repository.findOrganisationById(id);
        if (organisation == null) {
            throw new MchdException(String.format("Organisation %s", NOT_FOUND));
        }
        return organisation;
    }

    @Override
    public Organisation findByOrganisationInn(String inn) {
        return this.repository.findOrganisationByInn(inn);
    }

    @Override
    public Organisation updateOrganisation(@NotNull Organisation organisation) {
        Long organisationId = organisation.getId();
        if (organisationId == null) {
            throw new BadRequestException("Can`t update organisation because id null!" );
        }

        Organisation existOrganisation = this.repository.findOrganisationById(organisationId);
        if (existOrganisation == null) {
            throw new MchdException(String.format("Can`t update. Organisation with id=%d %s", organisationId, NOT_FOUND));
        }
        return this.repository.save(organisation);
    }

    @Override
    public void deleteOrganisation(@NotNull Long id) {
        Organisation organisation = this.repository.findOrganisationById(id);
        if (organisation == null) {
            throw new MchdException(String.format("Can`t delete organisation, because organisation %s", NOT_FOUND));
        }
        this.repository.deleteById(id);
    }
}
