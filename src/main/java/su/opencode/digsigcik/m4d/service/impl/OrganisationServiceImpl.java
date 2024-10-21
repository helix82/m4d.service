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
        return this.repository.save(organisation);
    }

    @Override
    public Organisation findByOrganisationId(@NotNull Long id) {
        return this.repository.findOrganisationById(id);
    }

    @Override
    public Organisation findByOrganisationInn(String inn) {
        return this.repository.findOrganisationByInn(inn);
    }

    @Override
    public Organisation updateOrganisation(@NotNull Organisation organisation) {
        return this.repository.save(organisation);
    }

    @Override
    public void deleteOrganisation(@NotNull Long id) {
        this.repository.deleteById(id);
    }
}
