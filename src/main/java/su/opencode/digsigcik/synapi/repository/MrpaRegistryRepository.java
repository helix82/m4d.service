package su.opencode.digsigcik.synapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import su.opencode.digsigcik.synapi.dto.MrpaRegistryWithFioDTO;
import su.opencode.digsigcik.synapi.model.MrpaRegistry;

import java.util.List;

@Repository
public interface MrpaRegistryRepository extends JpaRepository<MrpaRegistry, Long> {
    @Query(value = "SELECT new su.opencode.digsigcik.synapi.dto.MrpaRegistryWithFioDTO(mr.id, mr.guid, " +
            "mr.internalNumber, mr.startDate, mr.endDate, mr.status, mr.sigPath, mr.mrpaXmlPath, mr.recalledXmlPath, " +
            "mr.organisationId, mr.principalId, mr.representativeId,"
            + " p.surname, p.name, p.patronymic, r.surname, r.name, r.patronymic)"
            + " from MrpaRegistry mr"
            + " left join Principal p on p.id=mr.principalId"
            + " left join Representative r on r.id=mr.representativeId "
            + " where mr.id=?1 ")
    List<MrpaRegistryWithFioDTO> findMrpaRegistryById(Long id);

    @Query(value = "SELECT new su.opencode.digsigcik.synapi.dto.MrpaRegistryWithFioDTO(mr.id, mr.guid, " +
            "mr.internalNumber, mr.startDate, mr.endDate, mr.status, mr.sigPath, mr.mrpaXmlPath, " +
            "mr.recalledXmlPath, mr.organisationId, mr.principalId, mr.representativeId,"
            + "p.surname, p.name, p.patronymic, r.surname, r.name, r.patronymic)"
            + " from MrpaRegistry mr"
            + " left join Principal p on p.id=mr.principalId"
            + " left join Representative r on r.id=mr.representativeId ")
    List<MrpaRegistryWithFioDTO> findAllMrpaRegistries();

    @Query(value = "SELECT new su.opencode.digsigcik.synapi.dto.MrpaRegistryWithFioDTO(mr.id, mr.guid, " +
            "mr.internalNumber, mr.startDate, mr.endDate, mr.status, mr.sigPath, mr.mrpaXmlPath, mr.recalledXmlPath, " +
            "mr.organisationId, mr.principalId, mr.representativeId,"
            + "p.surname, p.name, p.patronymic, r.surname, r.name, r.patronymic)"
            + " from MrpaRegistry mr"
            + " left join Principal p on p.id=mr.principalId"
            + " left join Representative r on r.id=mr.representativeId "
            + " where p.inn=?1 ")
    List<MrpaRegistryWithFioDTO> findMrpaRegistriesByPrincipalInn(String inn);

    @Query(value = "SELECT mr.* from mrpa_registry mr where mr.id=?1 ", nativeQuery = true)
    MrpaRegistry findMrpaById(Long id);
}
