package su.opencode.digsigcik.m4d.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import su.opencode.digsigcik.m4d.model.M4dConfirm;

public interface M4dConfirmRepository extends JpaRepository<M4dConfirm, Long> {
    M4dConfirm findM4dConfirmByM4dId(Long m4dId);
}
