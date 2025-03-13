package su.opencode.digsigcik.m4d.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import su.opencode.digsigcik.m4d.model.M4dRequestSearchDetail;

public interface M4dRequestDetailRepository extends JpaRepository<M4dRequestSearchDetail, Long> {
    @Query(value = "SELECT rsd FROM M4dRequestSearchDetail rsd WHERE rsd.requestSearchId=?1 and rsd.isActive=true")
    M4dRequestSearchDetail findByRequestSearchId(Long id);
}
