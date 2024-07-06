package su.opencode.digsigcik.synapi.service;

import su.opencode.digsigcik.synapi.model.PublicKeyUserCertificate;

import javax.validation.constraints.NotNull;
import java.util.List;

public interface PublicKeyUserCertificateService {
    List<PublicKeyUserCertificate> findAllPublicKeyUserCertificate();
    PublicKeyUserCertificate savePublicKeyUserCertificate(@NotNull PublicKeyUserCertificate publicKeyUserCertificate);
    PublicKeyUserCertificate findByPublicKeyUserCertificateId(@NotNull Long id);
    PublicKeyUserCertificate updatePublicKeyUserCertificate(@NotNull PublicKeyUserCertificate publicKeyUserCertificate);
    Integer countPublicKeyUserCertificateByImprint(String imprint);
}
