package su.opencode.digsigcik.m4d.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import su.opencode.digsigcik.m4d.model.PrincipalDetail;

public interface PrincipalDetailRepository extends JpaRepository<PrincipalDetail, Integer> {
    PrincipalDetail findPrincipalDetailById(Long id);
    void deleteByPrincipalId(Long id);
}
