package su.opencode.digsigcik.synapi.service;

import org.jetbrains.annotations.NotNull;
import org.springframework.web.multipart.MultipartFile;
import su.opencode.digsigcik.synapi.dto.VerifyDto;

public interface VerificationService {
    VerifyDto verify(@NotNull MultipartFile container);

}
