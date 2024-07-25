package su.opencode.digsigcik.synapi.service;

import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import su.opencode.digsigcik.synapi.dto.ContainerInputInfoDto;

import java.io.IOException;


/**
 * Simple electronic signature service
 */
public interface SimpleSignatureService {
    Resource createSimpleSignatureResource(MultipartFile file, @NotNull ContainerInputInfoDto containerInputInfo) throws IOException;

}
