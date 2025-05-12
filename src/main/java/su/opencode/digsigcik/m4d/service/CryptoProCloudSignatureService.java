package su.opencode.digsigcik.m4d.service;

import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;
import su.opencode.digsigcik.m4d.enums.HashAlgorithm;

import java.io.IOException;

public interface CryptoProCloudSignatureService {
    String initializeSignature(Authentication authentication,
                                          MultipartFile file,
                                          Long certificateId,
                                          String userLogin,
                                          String pinCode,
                                          boolean detached,
                                          String sigType,
                                          HashAlgorithm hashAlgorithm) throws IOException;
}