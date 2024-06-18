package su.opencode.digsigcik.synapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import su.opencode.digsigcik.synapi.model.SesUser;

public interface SesUserRepository extends JpaRepository<SesUser, Long> {
}
