package su.opencode.digsigcik.synapi.service;

import su.opencode.digsigcik.synapi.dto.PublicKeyUserCertificateAllResponseDto;
import su.opencode.digsigcik.synapi.dto.PublicKeyUserCertificateRequestDto;
import su.opencode.digsigcik.synapi.dto.PublicKeyUserCertificateResponseDto;
import su.opencode.digsigcik.synapi.dto.PublicKeyUserCertificateUpdateRequestDto;
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
    PublicKeyUserCertificateResponseDto createForUser(PublicKeyUserCertificateRequestDto dto, String login);
    PublicKeyUserCertificateResponseDto updatePublicKeyUserCertificateForUserById(@NotNull PublicKeyUserCertificateUpdateRequestDto dto, Long id, String login);
    boolean deletePublicKeyUserCertificateForUserById (Long id, String login);
    PublicKeyUserCertificateResponseDto findPublicKeyUserCertificateForUserById(Long id, String login);
    List<PublicKeyUserCertificateAllResponseDto> findAllPublicKeyUserCertificateAllDtoByLogin(String login);
}
