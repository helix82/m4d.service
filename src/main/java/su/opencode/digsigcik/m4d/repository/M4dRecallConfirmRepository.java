package su.opencode.digsigcik.m4d.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import su.opencode.digsigcik.m4d.model.M4dRecallConfirm;

public interface M4dRecallConfirmRepository extends JpaRepository<M4dRecallConfirm, Long> {
    M4dRecallConfirm findM4dRecallConfirmByM4dId(Long m4dId);
}
