package su.opencode.digsigcik.synapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import su.opencode.digsigcik.synapi.model.ESSUser;
import su.opencode.digsigcik.synapi.model.EncryptionKeyEntity;
import su.opencode.digsigcik.synapi.model.PinCode;

@Repository
public interface PinCodeRepository extends JpaRepository<PinCode, Long> {
    PinCode getPinCodeByUser(ESSUser essUser);
}
