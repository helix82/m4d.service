package su.opencode.digsigcik.synapi.service;

import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import su.opencode.digsigcik.synapi.dto.ContainerInputInfoDto;

import java.io.IOException;


/**
 * Signature service
 */
public interface SignatureService {
    Resource createSignatureResource(@NotNull MultipartFile file, @NotNull MultipartFile filePkcs12, @NotNull String passwordPkcs12, @NotNull ContainerInputInfoDto containerInputInfo) throws IOException;
}
