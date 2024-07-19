package su.opencode.digsigcik.synapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import su.opencode.digsigcik.synapi.model.StorageObject;

import java.util.List;

@Repository
public interface StorageObjectRepository extends JpaRepository<StorageObject, Long> {
    List<StorageObject> findByProviderId(Long providerId);
}
