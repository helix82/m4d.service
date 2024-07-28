package su.opencode.digsigcik.synapi.service;

import org.jetbrains.annotations.NotNull;
import org.springframework.web.multipart.MultipartFile;
import su.opencode.digsigcik.synapi.dto.ReportContainerDTO;
import su.opencode.digsigcik.synapi.dto.ReportDTO;

import java.io.IOException;
import java.net.URISyntaxException;

public interface SignatureVerificationService {
    ReportContainerDTO verifyDetachedGetPdfReport(byte @NotNull [] sourceFile,
                                                  byte @NotNull [] detachedSignFile, String sourceFileName,
                                                  String detachedSignFileName) throws IOException,
            URISyntaxException;

    ReportContainerDTO verifyDetachedGetPdfReport(@NotNull MultipartFile sourceFile,
                                                  @NotNull MultipartFile detachedSignFile) throws IOException,
            URISyntaxException;

    ReportContainerDTO verifyAttachedGetPdfReport(byte @NotNull [] attachedSign, String attachedSignFileName) throws IOException,
            URISyntaxException;

    ReportContainerDTO verifyAttachedGetPdfReport(@NotNull MultipartFile attachedSign) throws Exception;

    ReportDTO verifyAttached(byte[] attachedSign, String attachedSignFileName) throws Exception;

    ReportDTO verifyDetached(byte[] sourceFile, byte[] detachedSign, String sourceFileFullName,
                             String detachedSignFileName) throws Exception;
}
