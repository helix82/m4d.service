package su.opencode.digsigcik.synapi.service;

import org.springframework.web.multipart.MultipartFile;
import su.opencode.digsigcik.synapi.dto.SesResponseDto;
import su.opencode.digsigcik.synapi.dto.SesVerifyDto;

/**
 * Simple electronic signature service
 */
public interface SesService {
    SesResponseDto generateSesXmlFile(String pinCode, MultipartFile file, String token);

    SesVerifyDto verifySes(String operationId, MultipartFile file);
}
