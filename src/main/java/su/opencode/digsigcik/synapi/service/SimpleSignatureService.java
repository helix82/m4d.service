package su.opencode.digsigcik.synapi.service;

import org.springframework.web.multipart.MultipartFile;
import su.opencode.digsigcik.synapi.dto.JwtPayloadDto;
import su.opencode.digsigcik.synapi.dto.SesResponseDto;
import su.opencode.digsigcik.synapi.dto.SesVerifyJsonDto;
import su.opencode.digsigcik.synapi.enums.HashAlgorithm;

/**
 * Simple electronic signature service
 */
public interface SimpleSignatureService {
    SesResponseDto createSimpleSignature(String pinCode, MultipartFile srcFile, JwtPayloadDto jwtPayloadDto);

    SesVerifyJsonDto verifySimpleSignature(MultipartFile file);

}
