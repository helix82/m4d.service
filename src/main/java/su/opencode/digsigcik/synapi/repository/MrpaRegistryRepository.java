package su.opencode.digsigcik.synapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import su.opencode.digsigcik.synapi.dto.MrpaRegistryFullDTO;
import su.opencode.digsigcik.synapi.dto.MrpaRegistryWithFioDTO;
import su.opencode.digsigcik.synapi.dto.MrpaRegistryWithFioListDto;
import su.opencode.digsigcik.synapi.model.MrpaRegistry;

import java.util.List;

@Repository
public interface MrpaRegistryRepository extends JpaRepository<MrpaRegistry, Long> {
    @Query(value = "SELECT new su.opencode.digsigcik.synapi.dto.MrpaRegistryWithFioDTO(mr.id, mr.guid, " +
            "mr.internalNumber, mr.startDate, mr.endDate, mr.status, mr.sigPath, mr.createDate, mr.mrpaXmlFilename, mr.recalledXmlPath, " +
            "mr.organisationId, mr.principalId, mr.representativeId,"
            + " p.surname, p.name, p.patronymic, r.surname, r.name, r.patronymic)"
            + " from MrpaRegistry mr"
            + " left join Principal p on p.id=mr.principalId"
            + " left join Representative r on r.id=mr.representativeId "
            + " where mr.id=?1 ")
    MrpaRegistryWithFioDTO findMrpaRegistryById(Long id);

    @Query(value = "SELECT new su.opencode.digsigcik.synapi.dto.MrpaRegistryWithFioListDto(mr.id, mr.guid, " +
            "mr.internalNumber, mr.startDate, mr.endDate, mr.status, mr.mrpaXmlFilename, mr.organisationId,"
            + " p.id, p.inn, p.snils, p.jobTitle, p.name, p.surname, p.patronymic, r.id, r.inn, r.snils, r.birthDate, r.name, r.surname, r.patronymic)"
            + " from MrpaRegistry mr"
            + " left join Principal p on p.id=mr.principalId"
            + " left join Representative r on r.id=mr.representativeId ")
    List<MrpaRegistryWithFioListDto> findAllMrpaRegistries();

    @Query(value = "SELECT new su.opencode.digsigcik.synapi.dto.MrpaRegistryWithFioDTO(mr.id, mr.guid, " +
            "mr.internalNumber, mr.startDate, mr.endDate, mr.status, mr.sigPath, mr.createDate, mr.mrpaXmlFilename, mr.recalledXmlPath, " +
            "mr.organisationId, mr.principalId, mr.representativeId,"
            + "p.surname, p.name, p.patronymic, r.surname, r.name, r.patronymic)"
            + " from MrpaRegistry mr"
            + " left join Principal p on p.id=mr.principalId"
            + " left join Representative r on r.id=mr.representativeId "
            + " where p.inn=?1 ")
    MrpaRegistryWithFioDTO findMrpaRegistriesByPrincipalInn(String inn);

    @Query(value = "SELECT new su.opencode.digsigcik.synapi.dto.MrpaRegistryFullDTO(mr.id, mr.guid, mr.internalNumber, mr.startDate, mr.endDate, mr.status, mr.sigPath, mr.createDate, mr.mrpaXmlFilename, mr.recalledXmlPath, " +
            "p.id, p.inn, p.snils, p.jobTitle, p.name, p.surname, p.patronymic, r.id, r.inn, r.snils, r.birthDate, r.name, r.surname, r.patronymic," +
            "rp.id, rp.dateIssue, rp.whoIssue, rp.seriesNumber, rp.departmentCode, o.id, o.inn, o.kpp, o.ogrn, o.name, o.regionCode, o.legalAddress) "
            + " from MrpaRegistry mr"
            + " left join Principal p on p.id=mr.principalId"
            + " left join Organisation o on o.id=mr.organisationId"
            + " left join Representative r on r.id=mr.representativeId "
            + " left join RepresentativePassport rp on rp.representativeId=r.id"
            + " where mr.status<>'NOT_SIGNED' and p.inn=?1 ")
    List<MrpaRegistryFullDTO> findMrpaRegistriesFullByPrincipalInn(String inn);

    @Query(value = "SELECT DISTINCT new su.opencode.digsigcik.synapi.dto.MrpaRegistryWithFioListDto(mr.id, mr.guid, " +
            " mr.internalNumber, mr.startDate, mr.endDate, mr.status, mr.mrpaXmlFilename, mr.organisationId,"
            + " p.id, p.inn, p.snils, p.jobTitle, p.name, p.surname, p.patronymic, r.id, r.inn, r.snils, r.birthDate, r.name, r.surname, r.patronymic)"
            + " from MrpaRegistry mr"
            + " left join Principal p on p.id=mr.principalId"
            + " left join Representative r on r.id=mr.representativeId "
            + " where (p.inn=?1 or r.inn=?1)")
    List<MrpaRegistryWithFioListDto> findMrpaRegistriesByPrincipalInnLikePrincipalAndRepresentative(String inn);

    @Query("SELECT mr from MrpaRegistry mr where mr.id=?1 ")
    MrpaRegistry findMrpaById(Long id);

    @Modifying
    @Transactional
    @Query("update MrpaRegistry mr set mr.status = 'WITHDRAWN' where mr.id=?1")
    void recallMrpaRegistryById(Long id);

    @Query(value = "SELECT new su.opencode.digsigcik.synapi.dto.MrpaRegistryFullDTO(mr.id, mr.guid, mr.internalNumber, mr.startDate, mr.endDate, mr.status, mr.sigPath, mr.createDate, mr.mrpaXmlFilename, mr.recalledXmlPath, " +
            "p.id, p.inn, p.snils, p.jobTitle, p.name, p.surname, p.patronymic, r.id, r.inn, r.snils, r.birthDate, r.name, r.surname, r.patronymic," +
            "rp.id, rp.dateIssue, rp.whoIssue, rp.seriesNumber, rp.departmentCode, o.id, o.inn, o.kpp, o.ogrn, o.name, o.regionCode, o.legalAddress) "
            + " from MrpaRegistry mr"
            + " left join Principal p on p.id=mr.principalId"
            + " left join Organisation o on o.id=mr.organisationId"
            + " left join Representative r on r.id=mr.representativeId "
            + " left join RepresentativePassport rp on rp.representativeId=r.id"
            + " where mr.id=?1 ")
    MrpaRegistryFullDTO findMrpaRegistryFullById(Long id);

    @Query(value = "SELECT count(mr) from MrpaRegistry mr where mr.guid=?1 ")
    Integer findMrpaRegistryByGuid(String guid);
}
