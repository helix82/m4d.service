package su.opencode.digsigcik.m4d.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import su.opencode.digsigcik.m4d.model.M4dRecall;

import java.util.List;
import java.util.Set;

public interface M4dRecallRepository extends JpaRepository<M4dRecall, Long> {
    M4dRecall findM4dRecallById(Long id);

    @Query(value = "SELECT m4drc"
            + " from M4dRecall m4drc"
            + " join M4d m4d on m4d.mrpaRecall.id=m4drc.id"
            + " join Principal p on p.id=m4d.principalId"
            + " join Representative r on r.id=m4d.representativeId "
            + " where (p.inn IN (:innList) or r.inn IN (:innList))")
    List<M4dRecall> findListM4dRevokeBySetInn(@Param("innList") Set<String> innList);
}
