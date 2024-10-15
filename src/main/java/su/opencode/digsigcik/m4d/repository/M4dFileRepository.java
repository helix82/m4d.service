package su.opencode.digsigcik.m4d.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import su.opencode.digsigcik.m4d.model.M4dFile;

@Repository
public interface M4dFileRepository extends JpaRepository<M4dFile, Long> {
    M4dFile findM4dFileById(Long id);
}
