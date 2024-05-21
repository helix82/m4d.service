package su.opencode.digsigcik.synapi.service;

import org.springframework.web.multipart.MultipartFile;

public interface SimpleElectronicSigService {
    Long getOperationId(String pinCode, MultipartFile file);

    String verifySes(String userLogin);
}
