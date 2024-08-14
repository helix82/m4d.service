package su.opencode.digsigcik.m4d.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import su.opencode.digsigcik.m4d.model.M4d;

import java.util.List;
import java.util.Map;

@Repository
public interface M4dRepository extends JpaRepository<M4d, Long> {
    @Transactional(readOnly = true)
    @Query(value = "SELECT cast(mr.id as text), mr.guid, mr.internal_number, mr.start_date, mr.end_date, mr.status, mr.create_date, "
            + " cast(p.id as text) AS p_id, p.inn AS p_inn, p.snils AS p_snils, p.job_title AS p_job_title, p.name AS p_name, p.surname AS p_surname, p.patronymic AS p_patronymic,"
            + " cast(r.id as text) AS r_id, r.inn AS r_inn, r.snils AS r_snils, r.birth_date AS r_birth_date, r.name AS r_name, r.surname AS r_surname, r.patronymic AS r_patronymic"
            + " from mrpa_registry mr"
            + " left join principal p on p.id=mr.principal_id"
            + " left join representative r on r.id=mr.representative_id ", nativeQuery = true)
    List<Map<String, Object>> findAllMrpaRegistries();

    @Query(value = "SELECT cast(mr.id as text), mr.guid, mr.internal_number, mr.start_date, mr.end_date, mr.status, mr.create_date, "
            + " cast(p.id as text) AS p_id, p.inn AS p_inn, p.snils AS p_snils, p.job_title AS p_job_title, p.name AS p_name, p.surname AS p_surname, p.patronymic AS p_patronymic,"
            + " cast(r.id as text) AS r_id, r.inn AS r_inn, r.snils AS r_snils, r.birth_date AS r_birth_date, r.name AS r_name, r.surname AS r_surname, r.patronymic AS r_patronymic"
            + " from mrpa_registry mr"
            + " left join principal p on p.id=mr.principal_id"
            + " left join representative r on r.id=mr.representative_id "
            + " where p.inn=:inn ", nativeQuery = true)
    Map<String, Object> findMrpaRegistriesByPrincipalInn(String inn);

//    @Query(value = "SELECT new su.opencode.digsigcik.m4d.dto.M4dFullDTO(mr.id, mr.guid, mr.internalNumber, mr.startDate, mr.endDate, mr.status, mr.sigPath, mr.mrpaXml, mr.mrpaXmlFilename, mr.recalledXmlPath, " +
//            "p.id, p.inn, p.snils, p.jobTitle, p.name, p.surname, p.patronymic, r.id, r.inn, r.snils, r.birthDate, r.name, r.surname, r.patronymic," +
//            "rp.id, rp.dateIssue, rp.whoIssue, rp.seriesNumber, rp.departmentCode, o.id, o.inn, o.kpp, o.ogrn, o.name, o.regionCode, o.legalAddress) "
//            + " from mrpa_registry mr"
//            + " left join principal p on p.id=mr.principalId"
//            + " left join organisation o on o.id=mr.organisationId"
//            + " left join representative r on r.id=mr.representativeId "
//            + " left join representative_passport rp on rp.representativeId=r.id"
//            + " where mr.status<>'NOT_SIGNED' and p.inn=?1 ", nativeQuery = true)
//    List<M4dFullDto> findMrpaRegistriesFullByPrincipalInn(String inn); //Используется в AboutMe

    @Query(value = "SELECT cast(mr.id as text), mr.guid, mr.internal_number, mr.start_date, mr.end_date, mr.status, mr.create_date, "
            + " cast(p.id as text) AS p_id, p.inn AS p_inn, p.snils AS p_snils, p.job_title AS p_job_title, p.name AS p_name, p.surname AS p_surname, p.patronymic AS p_patronymic,"
            + " cast(r.id as text) AS r_id, r.inn AS r_inn, r.snils AS r_snils, r.birth_date AS r_birth_date, r.name AS r_name, r.surname AS r_surname, r.patronymic AS r_patronymic"
            + " from mrpa_registry mr"
            + " left join principal p on p.id=mr.principal_id"
            + " left join representative r on r.id=mr.representative_id "
            + " where (p.inn=:inn or r.inn=:inn)", nativeQuery = true)
    List<Map<String, Object>> findMrpaRegistriesByPrincipalInnLikePrincipalAndRepresentative(String inn);

    @Query(value = "SELECT * from mrpa_registry where id=:id ", nativeQuery = true)
    M4d findMrpaById(Long id);

    @Modifying
    @Transactional
    @Query(value = "update mrpa_registry mr set status = 'WITHDRAWN' where mr.id=:id", nativeQuery = true)
    void recallMrpaRegistryById(Long id);

    @Query(value = "SELECT cast(mr.id as text), mr.guid, mr.internal_number, mr.start_date, mr.end_date, mr.status, mr.sig_path, mr.create_date, mr.recalled_xml_path, "
            + " cast(p.id as text) AS p_id, p.inn AS p_inn, p.snils AS p_snils, p.job_title AS p_job_title, p.name AS p_name, p.surname AS p_surname,"
            + " p.patronymic AS p_patronymic, cast(r.id as text) AS r_id, r.inn AS r_inn, r.snils AS r_snils, r.birth_date AS r_birth_date,"
            + " r.name AS r_name, r.surname AS r_surname, r.patronymic AS r_patronymic,"
            + " cast(rp.id as text) AS rp_id, rp.date_issue AS rp_date_issue, rp.who_issue AS rp_who_issue, rp.series_number AS rp_series_number, rp.department_code AS rp_department_code,"
            + " cast(o.id as text) AS org_id, o.inn AS org_inn, o.kpp AS org_kpp, o.ogrn AS org_ogrn, o.name AS org_name, o.region_code AS org_region_code, o.legal_address AS org_legal_address "
            + " from mrpa_registry mr"
            + " left join principal p on p.id=mr.principal_id"
            + " left join organisation o on o.id=mr.organisation_id"
            + " left join representative r on r.id=mr.representative_id "
            + " left join representative_passport rp on rp.representative_id=r.id"
            + " where mr.id=:id ", nativeQuery = true)
    Map<String, Object> findMrpaRegistryFullById(Long id);

    @Query(value = "SELECT count(mr) from mrpa_registry mr where mr.guid=:guid ", nativeQuery = true)
    Integer findMrpaRegistryByGuid(String guid);
}
