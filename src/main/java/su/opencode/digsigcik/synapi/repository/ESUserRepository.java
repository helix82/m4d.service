package su.opencode.digsigcik.synapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import su.opencode.digsigcik.synapi.model.ESSUser;

@Repository
public interface ESUserRepository extends JpaRepository<ESSUser, Long> {
    ESSUser findESSUserByLogin(String login);

    ESSUser findESSUserById(Long id);

    ESSUser findESSUserByPinCodeHash(String pinCodeHash);
}
