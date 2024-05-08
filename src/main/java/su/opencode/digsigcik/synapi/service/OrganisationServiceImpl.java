package su.opencode.digsigcik.synapi.service;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import su.opencode.digsigcik.synapi.model.Organisation;
import su.opencode.digsigcik.synapi.repository.OrganisationRepository;

import java.util.List;

@Service
@AllArgsConstructor
@Primary
public class OrganisationServiceImpl implements OrganisationService{
    private final OrganisationRepository repository;


    @Override
    public List<Organisation> findAllOrganisation() {
        return repository.findAll();
    }

    @Override
    public Organisation saveOrganisation(Organisation organisation) {
        return repository.save(organisation);
    }

    @Override
    public Organisation findByOrganisationId(Long id) {
        return repository.findOrganisationById(id);
    }

    @Override
    public Organisation updateOrganisation(Organisation organisation) {
        return repository.save(organisation);
    }

    @Override
    public void deleteOrganisation(Long id) {
        repository.deleteById(id);
    }
}
