package su.opencode.digsigcik.m4d.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import su.opencode.digsigcik.m4d.model.M4d;
import su.opencode.digsigcik.m4d.model.M4dRecall;

import java.util.List;
import java.util.Map;

public interface M4dRecallRepository extends JpaRepository<M4dRecall, Long> {
    M4dRecall findM4dRecallById(Long id);

    @Query(value = "SELECT cast(mr.id as text), mr.guid, mr.internal_number, mr.start_date, mr.end_date, mr.status, mr.create_date, "
            + " cast(p.id as text) AS p_id, p.inn AS p_inn, p.snils AS p_snils, p.job_title AS p_job_title, p.name AS p_name, p.surname AS p_surname, p.patronymic AS p_patronymic,"
            + " cast(r.id as text) AS r_id, r.inn AS r_inn, r.snils AS r_snils, r.birth_date AS r_birth_date, r.name AS r_name, r.surname AS r_surname, r.patronymic AS r_patronymic, "
            + " cast(mc.id) as mc_id, mc.argument as mc_argument, mc.author as mc_author, mc.signature as mc_signature"
            + " from mrpa_registry mr"
            + " left join principal p on p.id=mr.principal_id"
            + " left join representative r on r.id=mr.representative_id "
            + " left join mrpa_recall mc on mc.mrpa_registry_id=mr.id"
            + " where (p.inn=:inn or r.inn=:inn)", nativeQuery = true)
    List<Map<String, Object>> findM4dByPrincipalInnLikePrincipalAndRepresentative(String inn);
}
