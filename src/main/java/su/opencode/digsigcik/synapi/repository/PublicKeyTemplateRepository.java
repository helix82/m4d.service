package su.opencode.digsigcik.synapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import su.opencode.digsigcik.synapi.model.PublicKeyTemplate;

@Repository
public interface PublicKeyTemplateRepository extends JpaRepository<PublicKeyTemplate, Long> {
    PublicKeyTemplate findPublicKeyTemplateById(Long id);
}
