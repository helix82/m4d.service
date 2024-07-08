package su.opencode.digsigcik.synapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import su.opencode.digsigcik.synapi.model.ContainerFile;

@Repository
public interface ContainerFileRepository extends JpaRepository<ContainerFile, Long> {
    ContainerFile getContainerFileByUuid(String uuid);
}
