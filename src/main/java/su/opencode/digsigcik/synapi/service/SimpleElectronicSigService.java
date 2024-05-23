package su.opencode.digsigcik.synapi.service;

import org.springframework.web.multipart.MultipartFile;
import su.opencode.digsigcik.synapi.dto.SesVerifyDto;

public interface SimpleElectronicSigService {
    Long getOperationId(String pinCode, MultipartFile file, String token);

    SesVerifyDto verifySes(String operationId, MultipartFile file);
}
