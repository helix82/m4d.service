package su.opencode.digsigcik.m4d.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import su.opencode.digsigcik.m4d.dto.M4dResponseDto;
import su.opencode.digsigcik.m4d.model.M4dRecall;

import java.util.List;
import java.util.Set;

public interface M4dRecallRepository extends JpaRepository<M4dRecall, Long> {
    M4dRecall findM4dRecallById(Long id);

    @Query("SELECT new su.opencode.digsigcik.m4d.dto.M4dResponseDto("
            + "m4d.id, m4d.guid, m4d.internalNumber, "
            + "p.id, p.inn, p.snils, p.jobTitle, p.name, p.surname, p.patronymic, "
            + "r.id, r.inn, r.snils, r.birthDate, r.name, r.surname, r.patronymic, "
            + "m4d.startDate, m4d.endDate, m4d.createDate, "
            + "m4drc.id, m4drc.author, m4drc.argument) "
            + "from M4dRecall m4drc "
            + "join M4d m4d on m4d.mrpaRecall.id = m4drc.id "
            + "join Principal p on p.id = m4d.principalId "
            + "join Representative r on r.id = m4d.representativeId "
            + "where (p.inn IN (:innList) or r.inn IN (:innList))")
    List<M4dResponseDto> findListM4dRevokeBySetInn(@Param("innList") Set<String> innList);

    @Modifying
    @Transactional
    @Query(value = "UPDATE m4d_revoke SET sig_file_id = :fileId WHERE id = :id", nativeQuery = true)
    void updateMrpaRecallById(@Param("id") Long id, @Param("fileId") Long fileId);
}
