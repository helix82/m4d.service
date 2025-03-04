package su.opencode.digsigcik.m4d.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import su.opencode.digsigcik.m4d.model.PrincipalDetail;

public interface PrincipalDetailRepository extends JpaRepository<PrincipalDetail, Integer> {
    @Query(value = "SELECT pd from PrincipalDetail pd where pd.id=?1 and pd.isActive=true")
    PrincipalDetail findPrincipalDetailById(Long id);
    void deleteByPrincipalId(Long id);
}
