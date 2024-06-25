package su.opencode.digsigcik.synapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import su.opencode.digsigcik.synapi.model.PinCode;
import su.opencode.digsigcik.synapi.model.UserProjection;

@Repository
public interface PinCodeRepository extends JpaRepository<PinCode, Long> {
    PinCode getPinCodeByUserProjection(UserProjection userProjection);
    boolean existsByUserProjection(UserProjection userProjection);
    boolean deleteByUserProjection(UserProjection userProjection);
}
