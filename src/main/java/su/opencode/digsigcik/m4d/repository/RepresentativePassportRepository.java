package su.opencode.digsigcik.m4d.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import su.opencode.digsigcik.m4d.model.RepresentativePassport;

@Repository
public interface RepresentativePassportRepository extends JpaRepository<RepresentativePassport, Long> {
    RepresentativePassport findRepresentativePassportById(Long id);
    RepresentativePassport findRepresentativePassportBySeriesNumber(String seriesNumber);
    RepresentativePassport findRepresentativePassportByRepresentativeId(Long representativeId);
}
