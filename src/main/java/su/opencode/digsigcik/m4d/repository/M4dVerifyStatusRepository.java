package su.opencode.digsigcik.m4d.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import su.opencode.digsigcik.m4d.model.M4dVerifyStatus;

@Repository
public interface M4dVerifyStatusRepository extends JpaRepository<M4dVerifyStatus, Long> {
    M4dVerifyStatus findM4dVerifyStatusByGuidIgnoreCase(String guid);
}
