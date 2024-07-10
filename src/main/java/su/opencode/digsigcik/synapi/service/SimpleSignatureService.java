package su.opencode.digsigcik.synapi.service;

import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import su.opencode.digsigcik.synapi.dto.ContainerUserInfoDto;
import su.opencode.digsigcik.synapi.dto.SesVerifyJsonDto;

import java.io.IOException;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * Simple electronic signature service
 */
public interface SimpleSignatureService {
    Resource createSimpleCustomSignatureResource(@NotNull MultipartFile srcFile,
                                                 @NotNull ContainerUserInfoDto containerUserInfo);

    SesVerifyJsonDto verifyCustomSimpleSignature(@NotNull MultipartFile file);

    Resource createSimpleSignatureResource(MultipartFile file, @NotNull ContainerUserInfoDto containerUserInfo) throws IOException;

    SesVerifyJsonDto verifySimpleSignature(MultipartFile file);

    PrivateKey getPrivateKey(byte[] privateKeyBytes, String pinCode, String algorithm);

    PrivateKey getPrivateKey(byte[] privateKeyBytes, String algorithm);

    PublicKey getPublicKey(byte[] publicKeyBytes, String algorithm);


}
