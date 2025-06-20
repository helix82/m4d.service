package su.opencode.digsigcik.m4d.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import su.opencode.digsigcik.m4d.model.UserPermissions;

import java.util.Optional;

@Repository
public interface UserPermissionsRepository extends JpaRepository<UserPermissions, Long> {
    Optional<UserPermissions> findByLogin(String login);
}
