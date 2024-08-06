package su.opencode.digsigcik.synapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import su.opencode.digsigcik.synapi.model.UserProjection;

@Repository
public interface UserProjectionRepository extends JpaRepository<UserProjection, Long> {
    UserProjection getUserProjectionByLogin(String login);
    UserProjection getUserProjectionById(Long id);

}
