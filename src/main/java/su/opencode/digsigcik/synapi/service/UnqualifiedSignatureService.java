package su.opencode.digsigcik.synapi.service;

import org.springframework.web.multipart.MultipartFile;
import su.opencode.digsigcik.synapi.dto.JwtPayloadDto;
import su.opencode.digsigcik.synapi.dto.SesResponseDto;
import su.opencode.digsigcik.synapi.dto.SesVerifyJsonDto;
import su.opencode.digsigcik.synapi.enums.HashAlgorithm;
import su.opencode.digsigcik.synapi.model.UserProjection;

import java.io.IOException;

/**
 * Simple electronic signature service
 */
public interface UnqualifiedSignatureService {
    SesResponseDto createUnqualifiedSignature(MultipartFile file, JwtPayloadDto jwtPayloadDto,
                                              HashAlgorithm hashAlgorithm) throws IOException;

    SesVerifyJsonDto verifyUnqualifiedSignature(MultipartFile file);

}
