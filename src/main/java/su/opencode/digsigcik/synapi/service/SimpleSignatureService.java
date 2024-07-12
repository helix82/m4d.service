package su.opencode.digsigcik.synapi.service;

import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import su.opencode.digsigcik.synapi.dto.ContainerInputInfoDto;

import java.io.IOException;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * Simple electronic signature service
 */
public interface SimpleSignatureService {
    Resource createSimpleCustomSignatureResource(@NotNull MultipartFile srcFile,
                                                 @NotNull ContainerInputInfoDto containerInputInfo);

    Resource createSimpleSignatureResource(MultipartFile file, @NotNull ContainerInputInfoDto containerInputInfo) throws IOException;


    PrivateKey getPrivateKey(byte[] privateKeyBytes, String pinCode, String algorithm);

    PrivateKey getPrivateKey(byte[] privateKeyBytes, String algorithm);

    PublicKey getPublicKey(byte[] publicKeyBytes, String algorithm);


}
