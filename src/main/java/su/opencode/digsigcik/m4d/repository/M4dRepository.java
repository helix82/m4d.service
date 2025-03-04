package su.opencode.digsigcik.m4d.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import su.opencode.digsigcik.m4d.dto.M4dResponseDto;
import su.opencode.digsigcik.m4d.model.M4d;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface M4dRepository extends JpaRepository<M4d, Long> {
    M4d findM4dById(Long id);
    List<M4d> findByPrincipalId(Long id);
    List<M4d> findByRepresentativeId(Long id);

    List<M4d> findAll();

    Integer countByGuid(String guid);

    M4d findByGuid(String guid);

    @Query(value = "SELECT new su.opencode.digsigcik.m4d.dto.M4dResponseDto(m4d.id, m4d.guid, "
            + " m4d.startDate, m4d.endDate, m4d.createDate, "
            + " p.id, p.inn, p.snils, pd.id, pd.jobTitle, pd.name, pd.surname, pd.patronymic, r.id, r.inn, r.snils, r.birthDate,"
            + " rd.name, rd.surname, rd.patronymic)"
            + " from M4d m4d"
            + " join Principal p on p.id = m4d.principalId"
            + " join PrincipalDetail pd on pd.id = m4d.principalDetailId"
            + " join Representative r on r.id = m4d.representativeId "
            + " join RepresentativeDetail rd on rd.id = m4d.representativeDetailId"
            + " where (p.inn IN (:innList) or r.inn IN (:innList))")
    List<M4dResponseDto> findListM4dBySetInn(@Param("innList") Set<String> innList);

    @Query(value = "SELECT mr from M4d mr left join Representative r on r.id=mr.representativeId where r.inn=?1 ")
    List<M4d> findM4dByRepresentativeInn(String inn);

    @Query("SELECT mr FROM M4d mr JOIN Representative r on r.id= mr.representativeId " +
            "left join M4dRecall rc on rc.m4d.id=mr.id " +
            "WHERE r.inn = :inn " +
            "AND mr.id = :m4dId " +
            "AND mr.sigFile IS NOT NULL " +
            "AND rc.m4d IS NULL " +
            "AND CURRENT_TIMESTAMP BETWEEN mr.startDate AND mr.endDate")
    Optional<M4d> findValidM4dByIdAndRepresentativeInn(@Param("inn") String inn, @Param("m4dId") Long m4dId);
}
