package su.opencode.digsigcik.synapi.service;

import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import su.opencode.digsigcik.synapi.dto.ContainerInputInfoDto;
import su.opencode.digsigcik.synapi.model.UserProjection;

import java.io.IOException;
import java.security.cert.CertificateEncodingException;


/**
 * Simple electronic signature service
 */
public interface SimpleSignatureService {
    Resource createSimpleSignatureResource(@NotNull MultipartFile file, @NotNull ContainerInputInfoDto containerInputInfo) throws IOException;
    void deleteKeyEntity(@NotNull UserProjection userProjection);
}
