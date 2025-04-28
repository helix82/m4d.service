package su.opencode.digsigcik.m4d.service;

import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;
import su.opencode.digsigcik.m4d.model.M4dConfirm;

import java.io.IOException;

public interface M4dConfirmService {
    M4dConfirm saveM4dConfirm(M4dConfirm m4dConfirm);
    M4dConfirm findM4dConfirmByM4dId(Long m4dId);
    M4dConfirm updateM4dConfirm(M4dConfirm m4dConfirm);
    void deleteM4dConfirmById(Long id);
    Long uploadSigRegisteredPep(Authentication authentication, Long m4dId, MultipartFile container, boolean verify) throws IOException;
    Long uploadSigRegisteredRr(Authentication authentication, Long m4dId, MultipartFile container, boolean verify) throws IOException;
    Long uploadXmlRegisteredRr(Authentication authentication, Long m4dId, String xmlContent) throws IOException;
}
