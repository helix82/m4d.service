package su.opencode.digsigcik.synapi.service;

import org.jetbrains.annotations.NotNull;
import org.springframework.web.multipart.MultipartFile;
import su.opencode.digsigcik.synapi.dto.ReportContainerDTO;
import su.opencode.digsigcik.synapi.dto.ReportDTO;

import java.io.IOException;
import java.net.URISyntaxException;

public interface SignatureVerificationService {

    ReportContainerDTO verifyDetached(@NotNull MultipartFile sourceFile,
                                      @NotNull MultipartFile detachedSign) throws Exception;
    ReportContainerDTO verifyAttached(@NotNull MultipartFile attachedSign) throws Exception;
    ReportDTO verifyDetached(byte[] srcFile, byte[] detachedSign, String sourceFileFullName) throws Exception;
    ReportDTO verifyAttached(byte[] attachedSign, String sourceFileFullName) throws Exception;
}
