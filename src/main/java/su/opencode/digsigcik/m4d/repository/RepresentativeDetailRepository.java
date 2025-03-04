package su.opencode.digsigcik.m4d.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import su.opencode.digsigcik.m4d.model.RepresentativeDetail;

@Repository
public interface RepresentativeDetailRepository extends JpaRepository<RepresentativeDetail, Long> {
    RepresentativeDetail findRepresentativeDetailById(Long id);
    RepresentativeDetail findRepresentativeDetailBySeriesNumber(String seriesNumber);
    @Query(value = "SELECT rd from RepresentativeDetail rd where rd.representativeId=?1 and rd.isActive=true")
    RepresentativeDetail findRepresentativeDetailByRepresentativeId(Long representativeId);
    void deleteByRepresentativeId(Long id);
}
