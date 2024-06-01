package su.opencode.digsigcik.synapi.service;

import org.springframework.web.multipart.MultipartFile;
import su.opencode.digsigcik.synapi.dto.SesDto;
import su.opencode.digsigcik.synapi.dto.SesResponseDto;
import su.opencode.digsigcik.synapi.dto.SesVerifyResponseDto;

/**
 * Simple electronic signature service
 */
public interface SesService {
    SesResponseDto getContainerPath(String pinCode, MultipartFile file, String token);

    SesVerifyResponseDto verifySes(String operationId, MultipartFile file);

    boolean createSes(SesDto ses);

    SesDto findSesById(Long id);

    SesDto findSesByUserLogin(String login);

    boolean updateSes(SesDto ses);

    void deleteSes(Long id);
}
