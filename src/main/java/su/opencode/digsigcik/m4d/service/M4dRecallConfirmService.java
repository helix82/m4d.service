package su.opencode.digsigcik.m4d.service;

import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;
import su.opencode.digsigcik.m4d.model.M4dRecallConfirm;

import java.io.IOException;

public interface M4dRecallConfirmService {
    M4dRecallConfirm saveM4dRecallConfirm(M4dRecallConfirm m4dConfirm);
    M4dRecallConfirm findM4dRecallConfirmByM4dId(Long m4dId);
    M4dRecallConfirm updateM4dRecallConfirm(M4dRecallConfirm m4dConfirm);
    void deleteM4dRecallConfirmById(Long id);
    Long uploadSigRegisteredPep(Authentication authentication, Long m4dId, MultipartFile container, boolean verify) throws IOException;
    Long uploadSigRegisteredRr(Authentication authentication, Long m4dId, MultipartFile container, boolean verify) throws IOException;
    Long uploadXmlRegisteredRr(Authentication authentication, Long m4dId, String xmlContent) throws IOException;
}
