package su.opencode.digsigcik.synapi.repository;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import su.opencode.digsigcik.synapi.model.PublicKeyUserCertificate;

import java.util.List;

@Repository
public interface PublicKeyUserCertificateRepository extends JpaRepository<PublicKeyUserCertificate, Long> {
    PublicKeyUserCertificate findPublicKeyUserCertificateById(Long id);

    @Override
    @Query("SELECT uc from PublicKeyUserCertificate uc where uc.deleted=false")
    @NotNull
    List<PublicKeyUserCertificate> findAll();

    List<PublicKeyUserCertificate> findAllByDeletedFalse();
}
