package su.opencode.digsigcik.synapi.service;

import su.opencode.digsigcik.synapi.model.PublicKeyUserCertificate;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.List;

public interface PublicKeyUserCertificateService {
    List<PublicKeyUserCertificate> findAllPublicKeyUserCertificate();
    List<PublicKeyUserCertificate> findAllPublicKeyUserCertificateByLogin(String login);
    PublicKeyUserCertificate savePublicKeyUserCertificate(@NotNull PublicKeyUserCertificate publicKeyUserCertificate);
    PublicKeyUserCertificate findByPublicKeyUserCertificateId(@NotNull Long id);
    PublicKeyUserCertificate updatePublicKeyUserCertificate(@NotNull PublicKeyUserCertificate publicKeyUserCertificate);
    Integer countPublicKeyUserCertificateByImprint(String imprint);
    void checkQualifications() throws IOException;
}
