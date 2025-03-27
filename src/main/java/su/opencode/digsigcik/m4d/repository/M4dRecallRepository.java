package su.opencode.digsigcik.m4d.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import su.opencode.digsigcik.m4d.dto.M4dRegistryResponseDto;
import su.opencode.digsigcik.m4d.model.M4dRecall;

import java.util.List;
import java.util.Set;

public interface M4dRecallRepository extends JpaRepository<M4dRecall, Long> {
    M4dRecall findM4dRecallById(Long id);
    M4dRecall findM4dRecallByM4dId(Long m4dId);

    @Query("SELECT new su.opencode.digsigcik.m4d.dto.M4dRegistryResponseDto("
            + "m4d.id, m4d.guid, p.id, p.inn, p.snils, pd.id, pd.jobTitle, pd.name, pd.surname, pd.patronymic, "
            + "r.id, r.inn, r.snils, r.birthDate, rd.name, rd.surname, rd.patronymic, "
            + "m4d.startDate, m4d.endDate, m4d.createDate, m4drc.id) "
            + "from M4dRecall m4drc "
            + "join M4d m4d on m4d.id = m4drc.m4d.id "
            + "join Principal p on p.id = m4d.principalId "
            + "join PrincipalDetail pd on pd.id = m4d.principalDetailId "
            + "join Representative r on r.id = m4d.representativeId "
            + "join RepresentativeDetail rd on rd.id = m4d.representativeDetailId "
            + "where (p.inn IN (:innList) or r.inn IN (:innList))")
    List<M4dRegistryResponseDto> findListM4dRevokeBySetInn(@Param("innList") Set<String> innList);

    @Modifying
    @Transactional
    @Query(value = "UPDATE m4d_revoke SET sig_file_id = :fileId WHERE id = :id", nativeQuery = true)
    void updateMrpaRecallById(@Param("id") Long id, @Param("fileId") Long fileId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE m4d_revoke SET status = :status WHERE id = :id", nativeQuery = true)
    void updateStatusMrpaRecallById(@Param("id") Long id, @Param("status")  String status);
}
