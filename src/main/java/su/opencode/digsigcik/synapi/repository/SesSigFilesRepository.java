package su.opencode.digsigcik.synapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import su.opencode.digsigcik.synapi.model.SesSig;

@Repository
public interface SesSigFilesRepository extends JpaRepository<SesSig, Long> {
}
