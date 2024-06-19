package su.opencode.digsigcik.synapi.service;

import org.springframework.web.multipart.MultipartFile;
import su.opencode.digsigcik.synapi.dto.SesDto;
import su.opencode.digsigcik.synapi.dto.SesResponseDto;
import su.opencode.digsigcik.synapi.dto.SesVerifyJsonDto;
import su.opencode.digsigcik.synapi.model.Ses;

/**
 * Simple electronic signature service
 */
public interface SesService {
    SesResponseDto getContainerPath(String pinCode, MultipartFile file, String token);

    SesVerifyJsonDto verifySes(MultipartFile file);

    boolean createSes(SesDto ses);

    SesDto findSesById(Long id);

    SesDto findSesByUserLogin(String login);

    Ses updateSes(Ses ses);

    void deleteSes(Long id);
}
