package su.opencode.digsigcik.synapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import su.opencode.digsigcik.synapi.model.Representative;

@Repository
public interface RepresentativeRepository extends JpaRepository<Representative, Long> {
    void deleteById(Long id);
    Representative findRepresentativeById(Long id);
}
