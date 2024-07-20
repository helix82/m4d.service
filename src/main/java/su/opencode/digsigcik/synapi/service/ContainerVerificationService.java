package su.opencode.digsigcik.synapi.service;

import org.jetbrains.annotations.NotNull;
import org.springframework.web.multipart.MultipartFile;
import su.opencode.digsigcik.synapi.dto.ReportContainerDTO;
import su.opencode.digsigcik.synapi.dto.ReportDTO;

import java.io.IOException;
import java.net.URISyntaxException;

public interface ContainerVerificationService {
    ReportDTO verifyContainer(@NotNull MultipartFile container);

}
