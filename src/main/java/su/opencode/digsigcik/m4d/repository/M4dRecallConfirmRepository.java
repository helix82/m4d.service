package su.opencode.digsigcik.m4d.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;
import su.opencode.digsigcik.m4d.model.M4dRecallConfirm;

public interface M4dRecallConfirmRepository extends JpaRepository<M4dRecallConfirm, Long> {
    M4dRecallConfirm findM4dRecallConfirmByM4dRecallId(Long m4dRecallId);
    @Modifying
    @Transactional
    void deleteM4dRecallConfirmByM4dRecallId(Long m4dRecallId);
}
