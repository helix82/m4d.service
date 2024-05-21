package su.opencode.digsigcik.synapi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import su.opencode.digsigcik.synapi.model.Organisation;
import su.opencode.digsigcik.synapi.repository.OrganisationRepository;

import java.util.List;

@Service
@Primary
public class OrganisationServiceImpl implements OrganisationService{
    private final OrganisationRepository repository;

    @Autowired
    public OrganisationServiceImpl(OrganisationRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Organisation> findAllOrganisation() {
        return this.repository.findAll();
    }

    @Override
    public Organisation saveOrganisation(Organisation organisation) {
        return this.repository.save(organisation);
    }

    @Override
    public Organisation findByOrganisationId(Long id) {
        return this.repository.findOrganisationById(id);
    }

    @Override
    public Organisation updateOrganisation(Organisation organisation) {
        return this.repository.save(organisation);
    }

    @Override
    public void deleteOrganisation(Long id) {
        this.repository.deleteById(id);
    }
}
