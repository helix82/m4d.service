package su.opencode.digsigcik.synapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import su.opencode.digsigcik.synapi.model.RepresentativePassport;

public interface RepresentativePassportRepository extends JpaRepository<RepresentativePassport, Long> {
    RepresentativePassport findRepresentativePassportById(Long id);
}
