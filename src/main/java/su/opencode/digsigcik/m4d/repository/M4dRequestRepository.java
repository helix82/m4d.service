package su.opencode.digsigcik.m4d.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import su.opencode.digsigcik.m4d.model.M4dRequestSearch;

public interface M4dRequestRepository extends JpaRepository<M4dRequestSearch, Long> {
    M4dRequestSearch findByGuidAndAndHashXml(String guid, String hash);
}
