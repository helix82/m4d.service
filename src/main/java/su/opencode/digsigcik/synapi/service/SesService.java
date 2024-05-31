package su.opencode.digsigcik.synapi.service;

import org.springframework.web.multipart.MultipartFile;
import su.opencode.digsigcik.synapi.dto.SesDto;
import su.opencode.digsigcik.synapi.dto.SesResponseDto;
import su.opencode.digsigcik.synapi.dto.SesVerifyJsonDto;
import su.opencode.digsigcik.synapi.dto.SesVerifyResponseDto;
import su.opencode.digsigcik.synapi.model.Ses;

/**
 * Simple electronic signature service
 */
public interface SesService {
    SesResponseDto getContainerPath(String pinCode, MultipartFile file, String token);
    SesVerifyResponseDto verifySes(String operationId, MultipartFile file);
    boolean createSes(SesDto ses);
    Ses findSesById(Long id);
    boolean updateSes(SesDto ses);
    void deleteSes(Long id);
}
