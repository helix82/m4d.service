package su.opencode.digsigcik.synapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import su.opencode.digsigcik.synapi.dto.MrpaRegistryFullDTO;
import su.opencode.digsigcik.synapi.dto.MrpaRegistryWithFioDTO;
import su.opencode.digsigcik.synapi.model.MrpaRegistry;

import java.util.List;

@Repository
public interface MrpaRegistryRepository extends JpaRepository<MrpaRegistry, Long> {
    @Query(value = "SELECT new su.opencode.digsigcik.synapi.dto.MrpaRegistryWithFioDTO(mr.id, mr.userId, mr.guid, mr.internalNumber, mr.startDate, mr.endDate, mr.status, mr.ftxRegistration, mr.sigPath, mr.mrpaXmlPath, mr.recalledXmlPath, mr.organisationId, mr.principalId, mr.representativeId,"
            + " p.surname, p.name, p.patronymic, r.surname, r.name, r.patronymic)"
            + " from MrpaRegistry mr"
            + " left join Principal p on p.id=mr.principalId"
            + " left join Representative r on r.id=mr.representativeId "
            + " where mr.id=?1 ")
    List<MrpaRegistryWithFioDTO> findMrpaRegistryById(Long id);

    @Query(value = "SELECT new su.opencode.digsigcik.synapi.dto.MrpaRegistryFullDTO(mr.id, mr.userId, mr.guid, mr.internalNumber, mr.startDate, mr.endDate, mr.status, mr.ftxRegistration, mr.sigPath, mr.mrpaXmlPath, mr.recalledXmlPath, "
            + "p.id, p.inn, p.snils, p.jobTitle, p.name, p.surname, p.patronymic, r.id, r.inn, r.snils, r.birthDate, r.name, r.surname, r.patronymic, rp.id, rp.dateIssue, rp.whoIssue, rp.seriesNumber, rp.departmentCode, o.id, o.inn, o.kpp, o.ogrn, o.name, o.regionCode, o.legalAddress, "
            + " c.id, c.code, c.name)"
            + " from MrpaRegistry mr"
            + " left join Principal p on p.id=mr.principalId"
            + " left join Organisation o on o.id=mr.organisationId"
            + " left join Representative r on r.id=mr.representativeId "
            + " left join RepresentativePassport rp on rp.representativeId=r.id"
            + " left join MrpaRegistryCredential mrc on mrc.mrpaRegistryId=mr.id"
            + " left join Credential c on c.id=mrc.credentialId"
            + " where mr.userId=?1 ")
    List<MrpaRegistryFullDTO> findMrpaRegistriesByUserId(Long userId);

    @Query(value = "SELECT new su.opencode.digsigcik.synapi.dto.MrpaRegistryWithFioDTO(mr.id, mr.userId, mr.guid, mr.internalNumber, mr.startDate, mr.endDate, mr.status, mr.ftxRegistration, mr.sigPath, mr.mrpaXmlPath, mr.recalledXmlPath, mr.organisationId, mr.principalId, mr.representativeId,"
            + "p.surname, p.name, p.patronymic, r.surname, r.name, r.patronymic)"
            + " from MrpaRegistry mr"
            + " left join Principal p on p.id=mr.principalId"
            + " left join Representative r on r.id=mr.representativeId "
            + " where mr.userId=?1 ")
    List<MrpaRegistryWithFioDTO> findAllMrpaRegistriesByUserId(Long userId);

//    @Query(value = "SELECT new su.opencode.digsigcik.synapi.dto.MrpaRegistryWithFioDTO(" +
//            "mr.id, mr.userId, mr.guid, mr.internalNumber, mr.startDate, mr.endDate, mr.status, " +
//            "mr.ftxRegistration, mr.sigPath, mr.mrpaXmlPath, mr.recalledXmlPath, mr.organisationId, " +
//            "mr.principalId, mr.representativeId,"
//            + "p.surname, p.name, p.patronymic, r.surname, r.name, r.patronymic)"
//            + " from MrpaRegistry mr"
//            + " left join Principal p on p.id=mr.principalId"
//            + " left join Representative r on r.id=mr.representativeId ")
//    List<MrpaRegistryWithFioDTO> findAllMrpaRegistries();

    @Query(value = "SELECT new su.opencode.digsigcik.synapi.dto.MrpaRegistryWithFioDTO(mr.id, mr.userId, mr.guid, mr.internalNumber, mr.startDate, mr.endDate, mr.status, mr.ftxRegistration, mr.sigPath, mr.mrpaXmlPath, mr.recalledXmlPath, mr.organisationId, mr.principalId, mr.representativeId,"
            + "p.surname, p.name, p.patronymic, r.surname, r.name, r.patronymic)"
            + " from MrpaRegistry mr"
            + " left join Principal p on p.id=mr.principalId"
            + " left join Representative r on r.id=mr.representativeId ")
    List<MrpaRegistryWithFioDTO> findAllMrpaRegistries();

    @Query(value = "SELECT new su.opencode.digsigcik.synapi.dto.MrpaRegistryWithFioDTO(mr.id, mr.userId, mr.guid, mr.internalNumber, mr.startDate, mr.endDate, mr.status, mr.ftxRegistration, mr.sigPath, mr.mrpaXmlPath, mr.recalledXmlPath, mr.organisationId, mr.principalId, mr.representativeId,"
            + "p.surname, p.name, p.patronymic, r.surname, r.name, r.patronymic)"
            + " from MrpaRegistry mr"
            + " left join Principal p on p.id=mr.principalId"
            + " left join Representative r on r.id=mr.representativeId "
            + " where p.inn=?1 ")
    List<MrpaRegistryWithFioDTO> findMrpaRegistriesByPrincipalInn(String inn);

    @Query(value = "SELECT mr.* from mrpa_registry mr where mr.id=?1 ", nativeQuery = true)
    MrpaRegistry findMrpaById(Long id);
}
