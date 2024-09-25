package su.opencode.digsigcik.m4d.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import su.opencode.digsigcik.m4d.model.Representative;

@Repository
public interface RepresentativeRepository extends JpaRepository<Representative, Long> {
    Representative findRepresentativeById(Long id);
    Representative findRepresentativeByInn(String inn);
}
