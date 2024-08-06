package su.opencode.digsigcik.synapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import su.opencode.digsigcik.synapi.model.Principal;

import java.util.List;

@Repository
public interface PrincipalRepository extends JpaRepository<Principal, Long> {
    Principal findPrincipalById(Long id);
    Principal findPrincipalByInn(String inn);
    List<Principal> findAllByInn(String inn);
}
