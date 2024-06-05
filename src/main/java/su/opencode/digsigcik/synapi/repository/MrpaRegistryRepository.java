package su.opencode.digsigcik.synapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import su.opencode.digsigcik.synapi.dto.MrpaRegistryFullDTO;
import su.opencode.digsigcik.synapi.model.MrpaRegistry;

import java.util.List;
import java.util.Map;

@Repository
public interface MrpaRegistryRepository extends JpaRepository<MrpaRegistry, Long> {
    @Query(value = "SELECT mr.*, concat(p.surname, ' ', p.name, ' ', p.patronymic) as principal_fio,  concat(r.surname, ' ', r.name, ' ', r.patronymic) as representative_fio "
            + " from mrpa_registry mr, principal p , representative r "
            + " where p.id=mr.principal_id "
            + " and r.id=mr.representative_id "
            + " and mr.id=?1 "
            , nativeQuery = true)
    List<Map<String, Object>> findMrpaRegistryById(Long id);

    //TODO @helix82 Дополнить Паспортными данными представителя и Полномочиями
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
}
