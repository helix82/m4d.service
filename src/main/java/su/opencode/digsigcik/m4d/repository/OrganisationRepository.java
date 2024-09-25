package su.opencode.digsigcik.m4d.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import su.opencode.digsigcik.m4d.model.Organisation;

@Repository
public interface OrganisationRepository extends JpaRepository<Organisation, Long> {
    Organisation findOrganisationById(Long id);
    Organisation findOrganisationByInn(String inn);
}
