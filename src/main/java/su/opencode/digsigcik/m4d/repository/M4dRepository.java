package su.opencode.digsigcik.m4d.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import su.opencode.digsigcik.m4d.model.M4d;

import java.util.List;
import java.util.Set;

@Repository
public interface M4dRepository extends JpaRepository<M4d, Long> {
    M4d findM4dByMrpaRecallId(Long m4dRecallId);

    M4d findM4dById(Long id);

    List<M4d> findAll();

    Integer countByGuid(String guid);

    @Query(value = "SELECT m4d"
            + " from M4d m4d"
            + " join Principal p on p.id=m4d.principalId"
            + " join Representative r on r.id=m4d.representativeId "
            + " where (p.inn IN (:innList) or r.inn IN (:innList))")
    List<M4d> findListM4dBySetInn(@Param("innList") Set<String> innList);

}
