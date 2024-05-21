package su.opencode.digsigcik.synapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import su.opencode.digsigcik.synapi.model.CA;

@Repository
public interface CARepository extends JpaRepository<CA, Long> {
    CA findCAById(long id);
}
