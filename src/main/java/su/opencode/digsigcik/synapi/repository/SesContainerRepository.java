package su.opencode.digsigcik.synapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import su.opencode.digsigcik.synapi.model.SesContainer;

import java.util.UUID;

@Repository
public interface SesContainerRepository extends JpaRepository<SesContainer, Long> {
    SesContainer findSesContainerById(Long id);
    SesContainer findSesContainerByUuid(UUID uuid);
}
